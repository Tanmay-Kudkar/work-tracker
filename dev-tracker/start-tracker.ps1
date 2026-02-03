# WorkTracker - Quick Start Script for PowerShell
# This script helps you start the tracker with proper configuration

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  WorkTracker - Setup Helper" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if username is set
if (-not $env:TRACKER_USER) {
    Write-Host "[ERROR] TRACKER_USER is not set!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please set your username first:" -ForegroundColor Yellow
    Write-Host '  $env:TRACKER_USER = "your_username"' -ForegroundColor Gray
    Write-Host ""
    Write-Host "Valid usernames:" -ForegroundColor Yellow
    Write-Host "  - tanmay_kudkar" -ForegroundColor Gray
    Write-Host "  - yash_thakur" -ForegroundColor Gray
    Write-Host "  - nidhish_vartak" -ForegroundColor Gray
    Write-Host "  - atharva_raut" -ForegroundColor Gray
    Write-Host "  - parth_waghe" -ForegroundColor Gray
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Username: $env:TRACKER_USER" -ForegroundColor Green
Write-Host ""

# Ask for environment
Write-Host "Select environment:" -ForegroundColor Yellow
Write-Host "  1. Development (local backend on http://localhost:8080)"
Write-Host "  2. Production (deployed on Render.com)"
Write-Host ""
$envChoice = Read-Host "Enter choice (1 or 2)"

if ($envChoice -eq "2") {
    $env:TRACKER_ENV = "production"
    $env:TRACKER_SERVER = "https://worktracker-api.onrender.com/api"
    Write-Host ""
    Write-Host "[Production Mode]" -ForegroundColor Magenta
    Write-Host "Server: $env:TRACKER_SERVER" -ForegroundColor Gray
    Write-Host ""
    Write-Host "NOTE: Update TRACKER_SERVER if your URL is different!" -ForegroundColor Yellow
} else {
    $env:TRACKER_ENV = "development"
    $env:TRACKER_SERVER = "http://localhost:8080/api"
    Write-Host ""
    Write-Host "[Development Mode]" -ForegroundColor Cyan
    Write-Host "Server: $env:TRACKER_SERVER" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Make sure backend is running: cd backend && mvn spring-boot:run" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting WorkTracker..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if dependencies are installed
try {
    python -c "import requests, psutil" 2>$null
} catch {
    Write-Host "Installing dependencies..." -ForegroundColor Yellow
    pip install -r requirements.txt
    Write-Host ""
}

# Start tracker
python tracker.py
