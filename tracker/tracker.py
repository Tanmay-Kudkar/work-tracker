import time
import requests
import platform
import os
import sys
import atexit
import signal
import psutil
from datetime import datetime

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

# Tracking interval (seconds)
TRACKING_INTERVAL = int(os.environ.get("TRACKER_INTERVAL", "1"))

# Track active sessions
active_sessions = {}  # {app_name: {"start_time": datetime, "process_name": str}}

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
    """Get the currently active window title and process name (Windows only)"""
    if platform.system() == "Windows":
        try:
            import win32gui
            import win32process
            import psutil
            
            window = win32gui.GetForegroundWindow()
            title = win32gui.GetWindowText(window)
            tid, pid = win32process.GetWindowThreadProcessId(window)
            
            try:
                process_name = psutil.Process(pid).name()
            except:
                process_name = "Unknown"
                
            return title, process_name
        except ImportError:
            print("Missing dependencies. Run: pip install pywin32 psutil")
            return "Unknown", "Unknown"
        except Exception as e:
            return "Error", str(e)
    else:
        return "Not supported OS", "Unknown"

def get_running_apps():
    """Get list of all running application processes"""
    apps = set()
    try:
        for proc in psutil.process_iter(['name', 'pid']):
            try:
                name = proc.info['name']
                if name and name.endswith('.exe'):
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
    """Send activity log to server"""
    if not window_title:
        return True
        
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
    """Track which apps have started or stopped"""
    global active_sessions
    
    current_apps = get_running_apps()
    
    # Check for newly started apps
    for app in current_apps:
        if app not in active_sessions:
            active_sessions[app] = {
                "start_time": datetime.now(),
                "process_name": app
            }
            success = send_session_event(app, app, "start")
            if success:
                print(f"[{time.strftime('%H:%M:%S')}] 🟢 STARTED: {app}")
    
    # Check for closed apps
    closed_apps = [app for app in active_sessions.keys() if app not in current_apps]
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
    print("\n🚀 Tracker started! Press Ctrl+C to stop.\n")

    last_logged = None
    error_count = 0
    
    # Initialize with current running apps
    for app in get_running_apps():
        active_sessions[app] = {
            "start_time": datetime.now(),
            "process_name": app
        }
        send_session_event(app, app, "start")
    
    print(f"[{time.strftime('%H:%M:%S')}] Initialized with {len(active_sessions)} running apps\n")

    while True:
        try:
            # Track session changes (app starts/stops)
            track_sessions()
            
            # Track active window
            title, app_name = get_active_window_info()
            
            if title and app_name:
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
