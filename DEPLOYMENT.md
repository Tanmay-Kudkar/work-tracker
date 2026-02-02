# 🚀 WorkTracker Deployment Guide for Render.com

## 📊 Database Storage Architecture

### How Data is Stored for All Days

The application uses **PostgreSQL** with the following data model:

#### 1. **ActivityLog Table**
```sql
- id: Primary key (auto-increment)
- username: Team member identifier
- applicationName: Name of the application used
- windowTitle: Window title (for context)
- timestamp: Exact date and time of activity
```

**Indexes for Performance:**
- `idx_activity_username` - Fast queries by user
- `idx_activity_timestamp` - Fast queries by date
- `idx_activity_username_timestamp` - Combined queries

#### 2. **TeamMember Table**
```sql
- id: Primary key
- username: Unique identifier
- fullName: Display name
- lastSeen: Last activity timestamp
```

#### 3. **WorkSession Table**
```sql
- id: Primary key
- username: Team member
- applicationName: Current application
- startTime: Session start
- endTime: Session end
- isActive: Whether session is ongoing
```

### Data Retention Strategy

**✅ All historical data is preserved** - The database keeps records indefinitely, allowing you to:
- View activity for any past date
- Generate historical reports
- Track productivity trends over time
- Compare performance across weeks/months

**Note:** On Render's free tier, database is limited to 1GB. For production with large teams, consider:
- Archiving old data (older than 6-12 months)
- Upgrading to a paid database plan
- Implementing data cleanup jobs for very old records

---

## 🌐 Deployment Steps on Render.com

### Prerequisites
- GitHub account
- Render.com account (free tier available)
- Your code pushed to a GitHub repository

---

### Step 1: Push Code to GitHub

```bash
# Initialize git (if not already done)
cd S:\Work-Tracker
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit - WorkTracker app"

# Create repository on GitHub, then:
git remote add origin https://github.com/YOUR_USERNAME/work-tracker.git
git branch -M main
git push -u origin main
```

---

### Step 2: Deploy on Render.com

#### Option A: Using Blueprint (Recommended - One-Click Deploy)

1. **Login to Render:** Go to https://render.com and sign in with GitHub

2. **Create New Blueprint:**
   - Click "New +" → "Blueprint"
   - Connect your GitHub repository
   - Render will detect `render.yaml` automatically

3. **Review Configuration:**
   ```yaml
   ✅ Database: worktracker-db (PostgreSQL Free)
   ✅ Backend API: worktracker-api (Docker)
   ✅ Frontend: worktracker-frontend (Static Site)
   ```

4. **Deploy:**
   - Click "Apply" to create all services
   - Wait 5-10 minutes for initial deployment

---

#### Option B: Manual Setup (Step by Step)

### 2A: Create PostgreSQL Database

1. Click **"New +"** → **"PostgreSQL"**
2. Configure:
   - **Name:** `worktracker-db`
   - **Database:** `worktracker`
   - **User:** `worktracker`
   - **Region:** Choose closest to you
   - **Plan:** Free
3. Click **"Create Database"**
4. **Save these credentials** (shown once):
   - Internal Database URL
   - External Database URL
   - Username
   - Password

### 2B: Deploy Backend API

1. Click **"New +"** → **"Web Service"**
2. Connect your GitHub repository
3. Configure:
   - **Name:** `worktracker-api`
   - **Region:** Same as database
   - **Branch:** `main`
   - **Root Directory:** `backend`
   - **Environment:** `Docker`
   - **Dockerfile Path:** `./Dockerfile`
   - **Plan:** Free

4. **Environment Variables:**
   ```bash
   DATABASE_URL=<Internal Database URL from step 2A>
   DATABASE_USERNAME=worktracker
   DATABASE_PASSWORD=<Password from step 2A>
   H2_CONSOLE_ENABLED=false
   PORT=8080
   FRONTEND_URL=https://worktracker-frontend.onrender.com
   ```

5. Click **"Create Web Service"**

### 2C: Deploy Frontend

1. Click **"New +"** → **"Static Site"**
2. Connect your GitHub repository
3. Configure:
   - **Name:** `worktracker-frontend`
   - **Region:** Same as backend
   - **Branch:** `main`
   - **Root Directory:** `frontend`
   - **Build Command:** `npm install && npm run build`
   - **Publish Directory:** `dist`
   - **Plan:** Free

4. **Environment Variables:**
   ```bash
   VITE_API_URL=https://worktracker-api.onrender.com/api/activity
   VITE_SESSION_URL=https://worktracker-api.onrender.com/api/sessions
   ```

5. Click **"Create Static Site"**

---

### Step 3: Update Backend CORS Settings

After frontend is deployed, update backend environment variable:

1. Go to **worktracker-api** service
2. Click **"Environment"**
3. Update `FRONTEND_URL`:
   ```bash
   FRONTEND_URL=https://YOUR-FRONTEND-URL.onrender.com
   ```
4. Click **"Save Changes"** (backend will redeploy)

---

### Step 4: Verify Deployment

1. **Database:** Check it's running in Render Dashboard
2. **Backend:** Visit `https://your-api.onrender.com/api/activity/summary?date=2026-02-02`
   - Should return JSON data (may be empty initially)
3. **Frontend:** Visit `https://your-frontend.onrender.com`
   - Should load the WorkTracker UI

---

## 🔧 Important Configuration Notes

### Backend Dockerfile
Your current Dockerfile should work. Verify it contains:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Application Properties
Already configured to use environment variables:
```properties
✅ DATABASE_URL - Automatically uses PostgreSQL on Render
✅ DATABASE_USERNAME - Database user
✅ DATABASE_PASSWORD - Database password
✅ spring.jpa.hibernate.ddl-auto=update - Creates tables automatically
✅ PORT - Uses Render's dynamic port
```

### Frontend Environment Variables
Create/update `frontend/.env.production`:
```bash
VITE_API_URL=https://your-backend.onrender.com/api/activity
VITE_SESSION_URL=https://your-backend.onrender.com/api/sessions
```

---

## ⚡ Performance Tips

### Free Tier Limitations
- **Backend:** Spins down after 15 min of inactivity (cold start ~30s)
- **Database:** 1GB storage, 1GB data transfer/month
- **Frontend:** No spin-down, instant loading

### Optimizations
1. **Keep Backend Warm:**
   - Set up a cron job to ping your API every 10 minutes
   - Use a service like UptimeRobot (free)

2. **Database Optimization:**
   - Indexes are already configured
   - Consider archiving data older than 6 months
   - Monitor database size in Render dashboard

3. **Frontend Caching:**
   - Already configured in `render.yaml`
   - Assets cached for 1 year

---

## 📱 Using the Deployed App

### Tracker Python Script
Update your `tracker.py` to use production API:

```python
# For production
API_URL = "https://your-backend.onrender.com/api/activity"

# Or keep localhost for development
API_URL = "http://localhost:8080/api/activity"
```

### Access the Dashboard
Visit: `https://your-frontend.onrender.com`

- View team activity
- Check individual dashboards
- View timeline and analytics
- All data persists across days!

---

## 🔒 Security Considerations

1. **Environment Variables:** Never commit credentials to GitHub
2. **CORS:** Already configured to allow only your frontend
3. **Database:** Use Render's internal connection string for security
4. **HTTPS:** Render provides free SSL certificates automatically

---

## 📊 Monitoring & Logs

### View Logs
1. Go to Render Dashboard
2. Click on service name
3. Click "Logs" tab
4. Real-time logs for debugging

### Monitor Database
1. Go to Database in Render
2. Check "Metrics" tab
3. Monitor storage usage, connections, queries

---

## 🆘 Troubleshooting

### Backend won't start
- Check logs for error messages
- Verify DATABASE_URL format
- Ensure PostgreSQL is running

### Frontend can't connect to backend
- Check CORS settings (FRONTEND_URL)
- Verify API URLs in frontend env vars
- Check backend is running and healthy

### Database connection errors
- Use **Internal Database URL** for backend
- Verify username/password
- Check database is running

### Cold Start Issues
- First request after 15 min may be slow
- Consider upgrading to paid tier ($7/mo - no spin-down)
- Or set up UptimeRobot to keep warm

---

## 💰 Cost Estimate

**Free Tier (Perfect for Small Teams):**
- PostgreSQL Database: FREE (1GB)
- Backend API: FREE (750 hrs/month)
- Frontend: FREE (100GB bandwidth)
- SSL Certificates: FREE
- **Total: $0/month** ✅

**Recommended Upgrade (For Production):**
- PostgreSQL Starter: $7/month (256MB RAM, no spin-down)
- Backend Starter: $7/month (512MB RAM, no spin-down)
- Frontend: FREE
- **Total: $14/month** 🚀

---

## 🎉 You're All Set!

Your WorkTracker is now deployed with:
- ✅ PostgreSQL database storing all historical data
- ✅ Backend API serving team analytics
- ✅ Beautiful frontend with glassmorphism design
- ✅ Automatic deployments on git push
- ✅ Free SSL certificates
- ✅ Global CDN for fast loading

**Questions?** Check Render documentation or create an issue on GitHub!
