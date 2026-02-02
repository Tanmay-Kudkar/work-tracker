@echo off
REM WorkTracker - Yash Thakur (PRODUCTION)
cd /d "%~dp0"
set TRACKER_USER=yash_thakur
set TRACKER_ENV=production
python tracker.py
pause
