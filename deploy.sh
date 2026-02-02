#!/bin/bash

# WorkTracker - Quick Deployment Script
# This script helps you deploy to Render.com

echo "🚀 WorkTracker Deployment Helper"
echo "================================"
echo ""

# Check if git is initialized
if [ ! -d .git ]; then
    echo "📦 Initializing Git repository..."
    git init
    git add .
    git commit -m "Initial commit - WorkTracker application"
    echo "✅ Git repository initialized"
else
    echo "✅ Git repository already exists"
fi

echo ""
echo "📋 Next Steps:"
echo ""
echo "1. Create a GitHub repository"
echo "   Go to: https://github.com/new"
echo ""
echo "2. Push your code to GitHub:"
echo "   git remote add origin https://github.com/YOUR_USERNAME/work-tracker.git"
echo "   git branch -M main"
echo "   git push -u origin main"
echo ""
echo "3. Deploy on Render.com:"
echo "   a. Go to: https://render.com"
echo "   b. Sign in with GitHub"
echo "   c. Click 'New +' → 'Blueprint'"
echo "   d. Select your work-tracker repository"
echo "   e. Click 'Apply' to deploy!"
echo ""
echo "4. Your services will be created:"
echo "   ✅ PostgreSQL Database (worktracker-db)"
echo "   ✅ Backend API (worktracker-api)"
echo "   ✅ Frontend (worktracker-frontend)"
echo ""
echo "5. Wait 5-10 minutes for deployment to complete"
echo ""
echo "📖 For detailed instructions, see DEPLOYMENT.md"
echo ""
echo "💡 Tip: The render.yaml file configures everything automatically!"
echo ""
