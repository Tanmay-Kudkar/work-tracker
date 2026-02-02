# WorkTracker - Quick Deployment Script for Windows
# This script helps you deploy to Render.com

Write-Host "🚀 WorkTracker Deployment Helper" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check if git is initialized
if (-not (Test-Path .git)) {
    Write-Host "📦 Initializing Git repository..." -ForegroundColor Yellow
    git init
    git add .
    git commit -m "Initial commit - WorkTracker application"
    Write-Host "✅ Git repository initialized" -ForegroundColor Green
} else {
    Write-Host "✅ Git repository already exists" -ForegroundColor Green
}

Write-Host ""
Write-Host "📋 Next Steps:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Create a GitHub repository" -ForegroundColor White
Write-Host "   Go to: https://github.com/new" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Push your code to GitHub:" -ForegroundColor White
Write-Host "   git remote add origin https://github.com/YOUR_USERNAME/work-tracker.git" -ForegroundColor Gray
Write-Host "   git branch -M main" -ForegroundColor Gray
Write-Host "   git push -u origin main" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Deploy on Render.com:" -ForegroundColor White
Write-Host "   a. Go to: https://render.com" -ForegroundColor Gray
Write-Host "   b. Sign in with GitHub" -ForegroundColor Gray
Write-Host "   c. Click 'New +' → 'Blueprint'" -ForegroundColor Gray
Write-Host "   d. Select your work-tracker repository" -ForegroundColor Gray
Write-Host "   e. Click 'Apply' to deploy!" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Your services will be created:" -ForegroundColor White
Write-Host "   ✅ PostgreSQL Database (worktracker-db)" -ForegroundColor Green
Write-Host "   ✅ Backend API (worktracker-api)" -ForegroundColor Green
Write-Host "   ✅ Frontend (worktracker-frontend)" -ForegroundColor Green
Write-Host ""
Write-Host "5. Wait 5-10 minutes for deployment to complete" -ForegroundColor White
Write-Host ""
Write-Host "📖 For detailed instructions, see DEPLOYMENT.md" -ForegroundColor Yellow
Write-Host ""
Write-Host "💡 Tip: The render.yaml file configures everything automatically!" -ForegroundColor Cyan
Write-Host ""
