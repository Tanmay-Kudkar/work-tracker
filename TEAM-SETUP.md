# 👥 Team Member Setup Guide

Quick setup guide for all WorkTracker team members.

---

## 🚀 For Each Team Member

### Step 1: Install Python & Dependencies

**Install Python** (if not already installed):
- Download from: https://www.python.org/downloads/
- Make sure "Add Python to PATH" is checked during installation

**Install Dependencies:**
```powershell
cd S:\Work-Tracker\tracker
pip install -r requirements.txt
```

---

### Step 2: Set Your Username

Each team member must set their own username.

#### **Tanmay Kudkar**
```powershell
$env:TRACKER_USER = "tanmay_kudkar"
```

#### **Yash Thakur**
```powershell
$env:TRACKER_USER = "yash_thakur"
```

#### **Nidhish Vartak**
```powershell
$env:TRACKER_USER = "nidhish_vartak"
```

#### **Atharva Raut**
```powershell
$env:TRACKER_USER = "atharva_raut"
```

#### **Parth Waghe**
```powershell
$env:TRACKER_USER = "parth_waghe"
```

---

### Step 3: Choose Environment

#### **Option A: Local Development**
Use this if running backend locally on your machine:

```powershell
cd S:\Work-Tracker\tracker
$env:TRACKER_USER = "your_username"  # Replace with your actual username
python tracker.py
```

#### **Option B: Production (After Deployment)**
Use this to connect to deployed Render backend:

```powershell
cd S:\Work-Tracker\tracker
$env:TRACKER_USER = "your_username"  # Replace with your actual username
$env:TRACKER_ENV = "production"
$env:TRACKER_SERVER = "https://worktracker-api.onrender.com/api"
python tracker.py
```

---

## 📋 Complete Setup Examples

### Example 1: Tanmay (Local Development)
```powershell
# Start backend first (in another terminal)
cd S:\Work-Tracker\backend
mvn spring-boot:run

# Then start tracker
cd S:\Work-Tracker\tracker
$env:TRACKER_USER = "tanmay_kudkar"
python tracker.py
```

### Example 2: Yash (Production)
```powershell
cd S:\Work-Tracker\tracker
$env:TRACKER_USER = "yash_thakur"
$env:TRACKER_ENV = "production"
$env:TRACKER_SERVER = "https://worktracker-api.onrender.com/api"
python tracker.py
```

### Example 3: Nidhish (Using Easy Launcher)
```powershell
cd S:\Work-Tracker\tracker
$env:TRACKER_USER = "nidhish_vartak"
.\start-tracker.ps1
# Select: 1 for Dev or 2 for Production
```

---

## 🎯 Making It Permanent (Optional)

So you don't have to set username every time:

### PowerShell Profile Method

1. **Open your PowerShell profile:**
```powershell
notepad $PROFILE
```

2. **Add your username:**
```powershell
# WorkTracker Configuration
$env:TRACKER_USER = "your_username"  # Change this to YOUR username

# For production (after deployment):
# $env:TRACKER_ENV = "production"
# $env:TRACKER_SERVER = "https://worktracker-api.onrender.com/api"
```

3. **Save and restart PowerShell**

Now you can just run:
```powershell
cd S:\Work-Tracker\tracker
python tracker.py
```

---

## 📂 Create Personal Shortcuts

### Method 1: Batch File

Create `start-tracker-[yourname].bat`:

**For Tanmay:**
```batch
@echo off
cd /d "S:\Work-Tracker\tracker"
set TRACKER_USER=tanmay_kudkar
python tracker.py
pause
```

**For Yash:**
```batch
@echo off
cd /d "S:\Work-Tracker\tracker"
set TRACKER_USER=yash_thakur
python tracker.py
pause
```

### Method 2: PowerShell Script

Create `start-tracker-[yourname].ps1`:

**For Nidhish:**
```powershell
$env:TRACKER_USER = "nidhish_vartak"
cd S:\Work-Tracker\tracker
python tracker.py
```

**For Atharva:**
```powershell
$env:TRACKER_USER = "atharva_raut"
cd S:\Work-Tracker\tracker
python tracker.py
```

**For Parth:**
```powershell
$env:TRACKER_USER = "parth_waghe"
cd S:\Work-Tracker\tracker
python tracker.py
```

Then just double-click the file to start!

---

## 🌐 Viewing Your Stats

### Local Development:
```
http://localhost:5173
```

### Production:
```
https://worktracker-frontend.onrender.com
```

**On the dashboard:**
1. Click on your name card to see your stats
2. View hourly activity, top apps, and categories
3. Switch to Timeline tab to see team rankings

---

## 🔄 Sharing the Same Computer?

If multiple team members use the same machine:

### Option 1: Use Different Shortcuts
- Create separate `.bat` or `.ps1` files for each member
- Name them clearly: `tanmay-tracker.bat`, `yash-tracker.bat`, etc.

### Option 2: Switch Username
```powershell
# Stop current tracker (Ctrl+C)
# Set new username
$env:TRACKER_USER = "different_member"
# Start tracker
python tracker.py
```

---

## ✅ Checklist for Each Member

- [ ] Python installed
- [ ] Dependencies installed (`pip install -r requirements.txt`)
- [ ] Username set (`$env:TRACKER_USER = "your_username"`)
- [ ] Backend running (for local dev) OR production URL set
- [ ] Tracker started (`python tracker.py`)
- [ ] Can see your name on dashboard

---

## 🆘 Troubleshooting

### "Invalid username" error
- Check spelling of your username
- Use lowercase with underscore
- Valid: `tanmay_kudkar`, `yash_thakur`, etc.

### "Cannot reach server"
- **Local:** Make sure backend is running (`mvn spring-boot:run`)
- **Production:** Check TRACKER_SERVER URL is correct

### "Missing dependencies"
```powershell
pip install requests pywin32 psutil
```

### How to stop tracker
- Press `Ctrl+C` in the terminal
- It will gracefully close all sessions

---

## 📊 What Data Is Tracked?

For each member:
- Active applications and window titles
- Timestamps of activity
- Application start/stop times
- Session durations

**Privacy:**
- No file contents or keystrokes
- Only app names and window titles
- Data only visible to team members
- Stop anytime with Ctrl+C

---

## 🎓 Need Help?

1. Check `tracker/README.md` for detailed guide
2. Check `DEPLOYMENT.md` for deployment info
3. Ask your team lead
4. Test with: `curl http://localhost:8080/api/activity/summary?date=2026-02-02`

---

## 📝 Team Member Reference

| Member | Username | Set Command |
|--------|----------|-------------|
| Tanmay Kudkar | `tanmay_kudkar` | `$env:TRACKER_USER = "tanmay_kudkar"` |
| Yash Thakur | `yash_thakur` | `$env:TRACKER_USER = "yash_thakur"` |
| Nidhish Vartak | `nidhish_vartak` | `$env:TRACKER_USER = "nidhish_vartak"` |
| Atharva Raut | `atharva_raut` | `$env:TRACKER_USER = "atharva_raut"` |
| Parth Waghe | `parth_waghe` | `$env:TRACKER_USER = "parth_waghe"` |

---

Happy tracking! 🚀📊
