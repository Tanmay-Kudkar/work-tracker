# Database Storage & Deployment - Quick Reference

## 📊 How Data is Stored for All Days

### Database Tables

**ActivityLog** - Main activity tracking table
```
├── id (Primary Key)
├── username (Team member)
├── applicationName (App used)
├── windowTitle (Context)
└── timestamp (Date & Time)
```

**Indexes for fast queries:**
- By username: Fast lookup of user activities
- By timestamp: Fast lookup by date
- Combined: Optimized for date-filtered user queries

**✅ ALL DATA IS KEPT FOREVER** - You can query any past date!

### Storage Capacity

**Free Tier:** 1GB PostgreSQL database
- ~10 million activity records
- Perfect for small teams (5-20 people)
- 6-12 months of detailed history

**Upgrade Options:**
- Starter ($7/mo): Unlimited storage
- Professional ($90/mo): High performance

---

## 🚀 Deploy to Render.com (5 Minutes)

### Quick Steps:

1. **Push to GitHub**
   ```bash
   git init
   git add .
   git commit -m "Deploy WorkTracker"
   git remote add origin https://github.com/YOUR_USERNAME/work-tracker.git
   git push -u origin main
   ```

2. **Deploy on Render**
   - Go to https://render.com
   - New + → Blueprint
   - Connect GitHub repo
   - Click "Apply"
   - ✅ Done! (Wait 5-10 min)

3. **What Gets Deployed**
   - PostgreSQL Database (Free)
   - Backend API (Free)
   - Frontend (Free)
   - Total: **$0/month**

### Your URLs:
- Frontend: `https://worktracker-frontend.onrender.com`
- Backend: `https://worktracker-api.onrender.com`
- Database: Internal (auto-configured)

---

## 🔧 Local Development

**Backend (Port 8080):**
```bash
cd backend
mvn spring-boot:run
```

**Frontend (Port 5173):**
```bash
cd frontend
npm install
npm run dev
```

**Tracker Script:**
```bash
cd tracker
pip install -r requirements.txt
python tracker.py
```

---

## 📖 Full Guide

See **DEPLOYMENT.md** for:
- Detailed deployment steps
- Environment variables
- Troubleshooting
- Database management
- Performance optimization

---

## ✨ Features After Deployment

✅ Historical data preserved forever
✅ Query any past date
✅ Team analytics & timeline
✅ Real-time activity tracking
✅ Beautiful glassmorphism UI
✅ Automatic HTTPS
✅ Global CDN
