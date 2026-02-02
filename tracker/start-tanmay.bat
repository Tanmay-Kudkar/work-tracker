@echo off
REM WorkTracker - Tanmay Kudkar (PRODUCTION)
cd /d "%~dp0"
set TRACKER_USER=tanmay_kudkar
set TRACKER_ENV=production
"C:\Users\Tanmay\AppData\Local\Programs\Python\Python312\python.exe" tracker.py
pause
