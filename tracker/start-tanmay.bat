@echo off
REM WorkTracker - Tanmay Kudkar (PRODUCTION)
cd /d "%~dp0"
set TRACKER_USER=tanmay_kudkar
set TRACKER_ENV=production
python tracker.py
pause
