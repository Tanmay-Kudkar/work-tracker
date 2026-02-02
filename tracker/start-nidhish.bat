@echo off
REM WorkTracker - Nidhish Vartak (PRODUCTION)
cd /d "%~dp0"
set TRACKER_USER=nidhish_vartak
set TRACKER_ENV=production
python tracker.py
pause
