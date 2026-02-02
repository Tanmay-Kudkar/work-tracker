# WorkTracker Setup Guide

## 📋 Requirements

| OS | Dependencies | Install Command |
|----|--------------|-----------------|
| **Windows** | requests, psutil, pywin32 | `pip install requests psutil pywin32` |
| **Mac** | requests, psutil, pyobjc | `pip install requests psutil pyobjc-framework-Cocoa pyobjc-framework-Quartz` |

---

## 🪟 Windows Setup

1. **Install Python 3** from https://www.python.org/downloads/
   - ✅ Check **"Add Python to PATH"** during installation

2. **Install dependencies:**
   ```
   pip install requests psutil pywin32
   ```

3. **Run your tracker:**
   - Double-click your `.bat` file (e.g., `start-yash.bat`)

---

## 🍎 Mac Setup

1. **Install Python 3** (comes with macOS or via Homebrew):
   ```
   brew install python
   ```

2. **Install dependencies:**
   ```
   pip3 install requests psutil pyobjc-framework-Cocoa pyobjc-framework-Quartz
   ```

3. **Make script executable:**
   ```
   chmod +x start-yash.sh
   ```

4. **Run your tracker:**
   ```
   ./start-yash.sh
   ```

---

## 👥 Team Members

| Member | Windows | Mac |
|--------|---------|-----|
| Tanmay Kudkar | `start-tanmay.bat` | `./start-tanmay.sh` |
| Yash Thakur | `start-yash.bat` | `./start-yash.sh` |
| Nidhish Vartak | `start-nidhish.bat` | `./start-nidhish.sh` |
| Atharva Raut | `start-atharva.bat` | `./start-atharva.sh` |
| Parth Waghe | `start-parth.bat` | `./start-parth.sh` |

---

## 🌐 Dashboard

View activity at: https://work-tracker-webapp.onrender.com

---

## ❓ Troubleshooting

### "ModuleNotFoundError: No module named 'requests'"
Run the install command for your OS (see table above).

### Mac: "Permission denied"
Run: `chmod +x start-*.sh`

### "Cannot reach server"
Check your internet connection. The server URL is:
`https://work-tracker-backend-3pts.onrender.com`
