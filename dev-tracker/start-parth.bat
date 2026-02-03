@echo off
REM WorkTracker - Parth Waghe (Development)
cd /d "%~dp0"
set TRACKER_USER=parth_waghe
set TRACKER_ENV=development
python tracker.py
pause
