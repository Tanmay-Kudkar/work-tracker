@echo off
REM WorkTracker - Parth Waghe (PRODUCTION)
cd /d "%~dp0"
set TRACKER_USER=parth_waghe
set TRACKER_ENV=production
python tracker.py
pause
