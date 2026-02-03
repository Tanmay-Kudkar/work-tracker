Below is the **FINAL COMPLETE FILE** — clean, structured, highlighted, and **ready to paste directly** into your repository (README / SETUP.md).
No technical changes made. Only clarity, structure, and emphasis added.

---

# 🚀 Work Tracker – Development Environment Setup Guide

This document explains how **all team members** must configure environment variables for **local development** before running the **backend, frontend, or tracker**.

⚠️ Follow every step carefully to avoid configuration issues or accidental production access.

---

## 👥 Team Member Usernames

Use **one of the following** values for `TRACKER_USER`:

* 🧑‍💻 `nidhish_vartak`
* 🧑‍💻 `atharva_raut`
* 🧑‍💻 `parth_waghe`
* 🧑‍💻 `tanmay_kudkar`
* 🧑‍💻 `yash_thakur`

---

## 🪟 1. Windows Users

*(Tanmay, Yash, Atharva, Parth)*

### 🔹 a. Temporary Environment Variables (Per Session)

Open **PowerShell** and run:

```powershell
$env:JDBC_DATABASE_URL="jdbc:postgresql://localhost:5432/worktracker";
$env:DATABASE_USERNAME="postgres";
$env:DATABASE_PASSWORD="your_password";
$env:FRONTEND_URL="http://localhost:5173";
$env:TRACKER_ENV="development";
$env:TRACKER_SERVER="http://localhost:8080/api";
$env:TRACKER_USER="your_username"
```

⚠️ These variables reset when the terminal is closed.

---

### 🔹 b. Permanent Environment Variables

Run **each command one at a time** in PowerShell:

```powershell
[System.Environment]::SetEnvironmentVariable("JDBC_DATABASE_URL", "jdbc:postgresql://localhost:5432/worktracker", "User")
[System.Environment]::SetEnvironmentVariable("DATABASE_USERNAME", "postgres", "User")
[System.Environment]::SetEnvironmentVariable("DATABASE_PASSWORD", "your_password", "User")
[System.Environment]::SetEnvironmentVariable("FRONTEND_URL", "http://localhost:5173", "User")
[System.Environment]::SetEnvironmentVariable("TRACKER_ENV", "development", "User")
[System.Environment]::SetEnvironmentVariable("TRACKER_SERVER", "http://localhost:8080/api", "User")
[System.Environment]::SetEnvironmentVariable("TRACKER_USER", "your_username", "User")
```

🔁 **Restart your terminal or system** for changes to apply.

---

## 🍎🐧 2. Mac / Linux Users

*(Special note for Nidhish Vartak)*

### 🔹 a. Temporary Environment Variables

Open **Terminal** and run:

```bash
export JDBC_DATABASE_URL="jdbc:postgresql://localhost:5432/worktracker"
export DATABASE_USERNAME="postgres"
export DATABASE_PASSWORD="your_password"
export FRONTEND_URL="http://localhost:5173"
export TRACKER_ENV="development"
export TRACKER_SERVER="http://localhost:8080/api"
export TRACKER_USER="your_username"
```

---

### 🔹 b. Permanent Environment Variables

Add the following lines to **one** of these files:

* `~/.bashrc`
* `~/.zshrc`
* `~/.profile`

```bash
export JDBC_DATABASE_URL="jdbc:postgresql://localhost:5432/worktracker"
export DATABASE_USERNAME="postgres"
export DATABASE_PASSWORD="your_password"
export FRONTEND_URL="http://localhost:5173"
export TRACKER_ENV="development"
export TRACKER_SERVER="http://localhost:8080/api"
export TRACKER_USER="your_username"
```

Apply changes:

```bash
source ~/.bashrc   # or source ~/.zshrc
```

---

## 🎨 3. Frontend Setup (All Members)

Inside the `frontend/` directory:

📄 Create a file named **`.env.local`**

```env
VITE_API_URL=http://localhost:8080/api
```

---

## ▶️ 4. Running the Applications

### 🧩 Backend

```bash
cd backend
mvn spring-boot:run
```

### 🌐 Frontend

```bash
cd frontend
npm install
npm run dev
```

### 🐍 Tracker

```bash
cd tracker
python tracker.py
```

---

## 🚨🚨🚨 CRITICAL DEVELOPMENT WARNING 🚨🚨🚨

> ⚠️ **THIS RULE IS MANDATORY FOR ALL TEAM MEMBERS**

### ✅ ALWAYS DO THIS

* ✔️ Use the **`dev-tracker`** folder for **ALL local development**
* ✔️ Ensure **every script, config, and environment variable** points to **`dev-tracker`**

### ❌ NEVER DO THIS

* ❌ **DO NOT** run the **production tracker** locally
* ❌ **DO NOT** send test logs, debug data, or local activity to **production**

### 🔥 WHY THIS IS IMPORTANT

* 🛡️ Prevents pollution of **production logs**
* 🧨 Avoids **production database corruption**
* 🔐 Protects **live user data**
* 🧠 Keeps debugging **safe and isolated**

---

### 🛑 FINAL CHECK BEFORE RUNNING

Before executing **any tracker-related script**, confirm:

* ✅ Folder in use: `dev-tracker`
* ✅ Environment variables verified
* ✅ No production URLs or credentials present

> 🚫 **Any local testing outside `dev-tracker` is UNSAFE and NOT ALLOWED.**

---