@echo off
REM WorkTracker - Quick Start Script for Windows
REM This script helps you start the tracker with proper configuration

echo ========================================
echo   WorkTracker - Setup Helper
echo ========================================
echo.

REM Check if username is set
if "%TRACKER_USER%"=="" (
    echo [ERROR] TRACKER_USER is not set!
    echo.
    echo Please set your username first:
    echo   PowerShell: $env:TRACKER_USER = "your_username"
    echo   CMD:        set TRACKER_USER=your_username
    echo.
    echo Valid usernames:
    echo   - tanmay_kudkar
    echo   - yash_thakur
    echo   - nidhish_vartak
    echo   - atharva_raut
    echo   - parth_waghe
    echo.
    pause
    exit /b 1
)

echo Username: %TRACKER_USER%
echo.

REM Ask for environment
echo Select environment:
echo   1. Development (local backend on http://localhost:8080)
echo   2. Production (deployed on Render.com)
echo.
set /p ENV_CHOICE="Enter choice (1 or 2): "

if "%ENV_CHOICE%"=="2" (
    set TRACKER_ENV=production
    set TRACKER_SERVER=https://worktracker-api.onrender.com/api
    echo.
    echo [Production Mode]
    echo Server: %TRACKER_SERVER%
    echo.
    echo NOTE: Update TRACKER_SERVER if your URL is different!
) else (
    set TRACKER_ENV=development
    set TRACKER_SERVER=http://localhost:8080/api
    echo.
    echo [Development Mode]
    echo Server: %TRACKER_SERVER%
    echo.
    echo Make sure backend is running: cd backend ^&^& mvn spring-boot:run
)

echo.
echo ========================================
echo   Starting WorkTracker...
echo ========================================
echo.

REM Check if dependencies are installed
python -c "import requests, psutil" 2>nul
if errorlevel 1 (
    echo Installing dependencies...
    pip install -r requirements.txt
    echo.
)

REM Start tracker
python tracker.py

pause
