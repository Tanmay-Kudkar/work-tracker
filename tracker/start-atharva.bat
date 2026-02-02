@echo off
REM WorkTracker - Atharva Raut (PRODUCTION)
cd /d "%~dp0"
set TRACKER_USER=atharva_raut
set TRACKER_ENV=production
python tracker.py
pause
