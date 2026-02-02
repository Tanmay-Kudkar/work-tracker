import time
import requests
import platform
import os
import sys
import atexit
import signal
import psutil
from datetime import datetime

# Detect OS
IS_WINDOWS = platform.system() == "Windows"
IS_MAC = platform.system() == "Darwin"
IS_LINUX = platform.system() == "Linux"

# Import platform-specific modules
if IS_WINDOWS:
    try:
        import win32gui
        import win32process
    except ImportError:
        print("⚠️  Missing Windows dependencies. Run: pip install pywin32")
        win32gui = None
        win32process = None
elif IS_MAC:
    try:
        from AppKit import NSWorkspace
        from Quartz import (
            CGWindowListCopyWindowInfo,
            kCGWindowListOptionOnScreenOnly,
            kCGNullWindowID
        )
    except ImportError:
        print("⚠️  Missing Mac dependencies. Run: pip install pyobjc-framework-Cocoa pyobjc-framework-Quartz")
        NSWorkspace = None

# ============================================
# Configuration
# ============================================

# Environment - set to 'production' to use deployed API
ENVIRONMENT = os.environ.get("TRACKER_ENV", "development")  # 'development' or 'production'

# Server URLs
if ENVIRONMENT == "production":
    # Your Render.com backend URL
    SERVER_URL = os.environ.get("TRACKER_SERVER", "https://work-tracker-backend-3pts.onrender.com/api")
    print("🌐 Running in PRODUCTION mode")
else:
    # Local development
    SERVER_URL = os.environ.get("TRACKER_SERVER", "http://localhost:8080/api")
    print("💻 Running in DEVELOPMENT mode")

ACTIVITY_URL = f"{SERVER_URL}/activity"
SESSION_URL = f"{SERVER_URL}/sessions"

# Team member username - REQUIRED
# Set via: $env:TRACKER_USER = "your_username"
USERNAME = os.environ.get("TRACKER_USER", "")

# Valid team member usernames
VALID_MEMBERS = [
    "tanmay_kudkar",
    "yash_thakur", 
    "nidhish_vartak",
    "atharva_raut",
    "parth_waghe"
]

# Tracking interval (seconds) - 30 sec for optimal storage savings
TRACKING_INTERVAL = int(os.environ.get("TRACKER_INTERVAL", "30"))

# Track active sessions
active_sessions = {}  # {app_name: {"start_time": datetime, "process_name": str}}

def is_dev_app(app_name):
    """Track all apps (filter disabled)"""
    return True  # Track everything

def test_server_connection():
    """Test if server is reachable"""
    try:
        response = requests.get(f"{SERVER_URL}/activity/summary?date={datetime.now().strftime('%Y-%m-%d')}", timeout=10)
        return response.status_code in [200, 404]  # 404 is ok if no data yet
    except requests.exceptions.ConnectionError:
        return False
    except Exception as e:
        print(f"Connection test error: {str(e)}")
        return False

def get_active_window_info():
    """Get the currently active window title and process name (cross-platform)"""
    
    if IS_WINDOWS:
        try:
            if win32gui is None:
                return "Unknown", "Unknown"
            
            window = win32gui.GetForegroundWindow()
            title = win32gui.GetWindowText(window)
            tid, pid = win32process.GetWindowThreadProcessId(window)
            
            try:
                process_name = psutil.Process(pid).name()
            except:
                process_name = "Unknown"
                
            return title, process_name
        except Exception as e:
            return "Error", str(e)
    
    elif IS_MAC:
        try:
            if NSWorkspace is None:
                return "Unknown", "Unknown"
            
            # Get the frontmost application
            workspace = NSWorkspace.sharedWorkspace()
            active_app = workspace.frontmostApplication()
            app_name = active_app.localizedName() if active_app else "Unknown"
            
            # Get window title from Quartz
            title = app_name  # Default to app name
            try:
                window_list = CGWindowListCopyWindowInfo(kCGWindowListOptionOnScreenOnly, kCGNullWindowID)
                for window in window_list:
                    if window.get('kCGWindowOwnerName') == app_name:
                        window_title = window.get('kCGWindowName', '')
                        if window_title:
                            title = window_title
                            break
            except:
                pass
            
            return title, app_name
        except Exception as e:
            return "Error", str(e)
    
    elif IS_LINUX:
        # Linux support using xdotool (if installed)
        try:
            import subprocess
            window_id = subprocess.check_output(['xdotool', 'getactivewindow']).decode().strip()
            title = subprocess.check_output(['xdotool', 'getwindowname', window_id]).decode().strip()
            pid = subprocess.check_output(['xdotool', 'getwindowpid', window_id]).decode().strip()
            process_name = psutil.Process(int(pid)).name()
            return title, process_name
        except:
            return "Unknown", "Unknown"
    
    else:
        return "Unsupported OS", "Unknown"

def get_running_apps():
    """Get list of all running application processes (cross-platform)"""
    apps = set()
    try:
        for proc in psutil.process_iter(['name', 'pid']):
            try:
                name = proc.info['name']
                if name:
                    if IS_WINDOWS and name.endswith('.exe'):
                        apps.add(name)
                    elif IS_MAC and not name.startswith('com.'):
                        # Filter out system processes on Mac
                        apps.add(name)
                    elif IS_LINUX:
                        apps.add(name)
            except (psutil.NoSuchProcess, psutil.AccessDenied):
                pass
    except Exception as e:
        pass
    return apps

def send_session_event(app_name, process_name, event_type, end_reason=None):
    """Send session start/end event to server"""
    data = {
        "username": USERNAME,
        "applicationName": app_name,
        "processName": process_name,
        "eventType": event_type
    }
    
    if end_reason:
        data["endReason"] = end_reason
    
    try:
        response = requests.post(SESSION_URL, json=data, timeout=5)
        return response.status_code == 200
    except Exception as e:
        return False

def send_activity(app_name, window_title):
    """Send activity log to server (only for dev apps)"""
    if not window_title:
        return True
    
    # Only track development-related apps
    if not is_dev_app(app_name):
        return True  # Skip silently
        
    data = {
        "username": USERNAME,
        "applicationName": app_name,
        "windowTitle": window_title
    }
    
    try:
        response = requests.post(ACTIVITY_URL, json=data, timeout=5)
        return response.status_code == 200
    except Exception as e:
        return False

def track_sessions():
    """Track which dev apps have started or stopped"""
    global active_sessions
    
    current_apps = get_running_apps()
    # Filter to only dev apps
    current_dev_apps = {app for app in current_apps if is_dev_app(app)}
    
    # Check for newly started dev apps
    for app in current_dev_apps:
        if app not in active_sessions:
            active_sessions[app] = {
                "start_time": datetime.now(),
                "process_name": app
            }
            success = send_session_event(app, app, "start")
            if success:
                print(f"[{time.strftime('%H:%M:%S')}] 🟢 STARTED: {app}")
    
    # Check for closed dev apps
    closed_apps = [app for app in active_sessions.keys() if app not in current_dev_apps]
    for app in closed_apps:
        session = active_sessions.pop(app)
        duration = (datetime.now() - session["start_time"]).total_seconds()
        
        # Determine end reason
        end_reason = "killed" if duration < 5 else "normal"
        
        success = send_session_event(app, app, "end", end_reason)
        if success:
            print(f"[{time.strftime('%H:%M:%S')}] 🔴 CLOSED: {app} ({end_reason}, {int(duration)}s)")

def cleanup(signum=None, frame=None):
    """Cleanup function called on exit - close all active sessions"""
    print("\n" + "=" * 60)
    print("  Shutting down tracker...")
    print("=" * 60)
    
    # Close all active sessions
    for app, session in active_sessions.items():
        send_session_event(app, session["process_name"], "end", "system_shutdown")
        print(f"  Closed session: {app}")
    
    print("  All sessions closed. Goodbye!")
    sys.exit(0)

def main():
    print("="*70)
    print("  📊 WorkTracker - Activity & Session Monitor")
    print("="*70)
    
    # Validate username
    if not USERNAME:
        print("\n❌ ERROR: Username not set!")
        print("\nPlease set your username:")
        print("  PowerShell:  $env:TRACKER_USER = 'your_username'")
        print("  CMD:         set TRACKER_USER=your_username")
        print("  Linux/Mac:   export TRACKER_USER=your_username")
        print(f"\n✅ Valid usernames: {', '.join(VALID_MEMBERS)}")
        sys.exit(1)
    
    if USERNAME.lower() not in VALID_MEMBERS:
        print(f"\n❌ ERROR: Invalid username '{USERNAME}'")
        print(f"\n✅ Valid usernames: {', '.join(VALID_MEMBERS)}")
        print("\nSet your username with:")
        print("  PowerShell:  $env:TRACKER_USER = 'your_username'")
        sys.exit(1)

    print(f"\n  👤 User: {USERNAME}")
    print(f"  🖥️  OS: {platform.system()}")
    print(f"  🌐 Server: {SERVER_URL}")
    print(f"  ⏱️  Interval: {TRACKING_INTERVAL} seconds")
    print(f"  📱 Tracking: App Activity + Session Start/End")
    
    # Test server connection
    print(f"\n  🔌 Testing server connection...")
    if test_server_connection():
        print("  ✅ Server is reachable!")
    else:
        print("  ⚠️  Warning: Cannot reach server!")
        if ENVIRONMENT == "development":
            print("  💡 Make sure backend is running: cd backend && mvn spring-boot:run")
        else:
            print("  💡 Check your TRACKER_SERVER URL or internet connection")
        print("\n  Press Enter to continue anyway, or Ctrl+C to exit...")
        try:
            input()
        except KeyboardInterrupt:
            print("\n  Exiting...")
            sys.exit(0)

    # Register cleanup handlers
    signal.signal(signal.SIGINT, cleanup)
    signal.signal(signal.SIGTERM, cleanup)
    atexit.register(cleanup)

    print("="*70)
    print("\n🚀 Tracker started! Press Ctrl+C to stop.")
    print("📌 Tracking all apps @ 30 sec interval\n")

    last_logged = None
    error_count = 0
    
    # Initialize with current running apps
    for app in get_running_apps():
        if is_dev_app(app):
            active_sessions[app] = {
                "start_time": datetime.now(),
                "process_name": app
            }
            send_session_event(app, app, "start")
    
    print(f"[{time.strftime('%H:%M:%S')}] Initialized with {len(active_sessions)} dev apps\n")

    while True:
        try:
            # Track session changes (app starts/stops)
            track_sessions()
            
            # Track active window
            title, app_name = get_active_window_info()
            
            if title and app_name and is_dev_app(app_name):
                success = send_activity(app_name, title)
                
                current = f"{app_name}|{title[:30]}"
                if current != last_logged:
                    if success:
                        print(f"[{time.strftime('%H:%M:%S')}] ✓ Active: {app_name}")
                        error_count = 0
                    else:
                        error_count += 1
                        if error_count <= 3:
                            print(f"[{time.strftime('%H:%M:%S')}] ⚠ Connection error (attempt {error_count}/3)")
                    
                    last_logged = current
            
            time.sleep(TRACKING_INTERVAL)
            
        except KeyboardInterrupt:
            cleanup()
        except Exception as e:
            print(f"[{time.strftime('%H:%M:%S')}] Error: {str(e)}")
            time.sleep(TRACKING_INTERVAL)

if __name__ == "__main__":
    main()
