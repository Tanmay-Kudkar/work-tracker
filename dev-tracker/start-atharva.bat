@echo off
REM WorkTracker - Atharva Raut (Development)
cd /d "%~dp0"
set TRACKER_USER=atharva_raut
set TRACKER_ENV=development
python tracker.py
pause
