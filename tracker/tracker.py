import time
import requests
import platform
import os
import sys
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
        print("Missing Windows dependencies. Run: pip install pywin32")
        sys.exit(1)
elif IS_MAC:
    try:
        from AppKit import NSWorkspace
        from Quartz import CGWindowListCopyWindowInfo, kCGWindowListOptionOnScreenOnly, kCGNullWindowID
    except ImportError:
        print("Missing Mac dependencies. Run: pip install pyobjc-framework-Cocoa pyobjc-framework-Quartz")
        sys.exit(1)

# Configuration
ENVIRONMENT = os.environ.get("TRACKER_ENV", "production")

if ENVIRONMENT == "production":
    SERVER_URL = "https://work-tracker-backend-3pts.onrender.com/api"
    print("Running in PRODUCTION mode")
else:
    SERVER_URL = "http://localhost:8080/api"
    print("Running in DEVELOPMENT mode")

ACTIVITY_URL = f"{SERVER_URL}/activity"
USERNAME = os.environ.get("TRACKER_USER", "")
TRACKING_INTERVAL = 30

VALID_MEMBERS = ["tanmay_kudkar", "yash_thakur", "nidhish_vartak", "atharva_raut", "parth_waghe"]

running = True

def get_active_window():
    if IS_WINDOWS:
        try:
            window = win32gui.GetForegroundWindow()
            title = win32gui.GetWindowText(window)
            _, pid = win32process.GetWindowThreadProcessId(window)
            process_name = psutil.Process(pid).name()
            return title, process_name
        except:
            return None, None
    elif IS_MAC:
        try:
            workspace = NSWorkspace.sharedWorkspace()
            active_app = workspace.frontmostApplication()
            if active_app:
                app_name = active_app.localizedName()
                return app_name, app_name
        except:
            pass
        return None, None
    return None, None

def send_activity(app_name, window_title):
    data = {
        "username": USERNAME,
        "applicationName": app_name,
        "windowTitle": window_title or app_name
    }
    try:
        response = requests.post(ACTIVITY_URL, json=data, timeout=5)
        return response.status_code == 200
    except Exception as e:
        print(f"    Error: {e}")
        return False

def shutdown(signum=None, frame=None):
    global running
    running = False
    print("\nShutting down tracker...")

def main():
    global running
    
    print("=" * 60)
    print("  WorkTracker - Activity Monitor")
    print("=" * 60)
    
    if not USERNAME or USERNAME.lower() not in VALID_MEMBERS:
        print(f"ERROR: Invalid username: '{USERNAME}'")
        print(f"Valid usernames: {', '.join(VALID_MEMBERS)}")
        print("Set with: $env:TRACKER_USER = 'your_username'")
        sys.exit(1)

    print(f"User: {USERNAME}")
    print(f"OS: {platform.system()}")
    print(f"Server: {SERVER_URL}")
    print(f"Interval: {TRACKING_INTERVAL} seconds")
    
    print("Testing server...")
    try:
        r = requests.get(f"{SERVER_URL}/activity/summary?date={datetime.now().strftime('%Y-%m-%d')}", timeout=10)
        if r.status_code in [200, 404]:
            print("Server is reachable!")
        else:
            print(f"Server returned: {r.status_code}")
    except Exception as e:
        print(f"Cannot reach server: {e}")

    signal.signal(signal.SIGINT, shutdown)
    signal.signal(signal.SIGTERM, shutdown)

    print("=" * 60)
    print("Tracker started! Press Ctrl+C to stop.")
    print("")

    while running:
        try:
            title, app_name = get_active_window()
            
            if app_name:
                success = send_activity(app_name, title)
                status = "OK" if success else "FAIL"
                print(f"[{time.strftime('%H:%M:%S')}] {status} - {app_name[:40]}")
            else:
                print(f"[{time.strftime('%H:%M:%S')}] No active window")
            
            for _ in range(TRACKING_INTERVAL):
                if not running:
                    break
                time.sleep(1)
                
        except Exception as e:
            print(f"[{time.strftime('%H:%M:%S')}] Error: {e}")
            time.sleep(5)
    
    print("Goodbye!")

if __name__ == "__main__":
    main()
