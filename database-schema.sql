/* 
 * WorkTracker Database Schema
 * PostgreSQL Database Structure
 */

-- =====================================================
-- 1. ActivityLog Table (Main activity tracking)
-- =====================================================
CREATE TABLE activity_log (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    application_name VARCHAR(255) NOT NULL,
    window_title TEXT,
    timestamp TIMESTAMP NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_activity_username ON activity_log(username);
CREATE INDEX idx_activity_timestamp ON activity_log(timestamp);
CREATE INDEX idx_activity_username_timestamp ON activity_log(username, timestamp);

-- =====================================================
-- 2. TeamMember Table (User information)
-- =====================================================
CREATE TABLE team_member (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    last_seen TIMESTAMP
);

-- =====================================================
-- 3. WorkSession Table (Session tracking)
-- =====================================================
CREATE TABLE work_session (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    application_name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Index for active sessions lookup
CREATE INDEX idx_session_active ON work_session(is_active, username);

-- =====================================================
-- Sample Queries for Common Operations
-- =====================================================

-- Get all activity for a specific user on a date
SELECT * FROM activity_log 
WHERE username = 'john.doe' 
  AND DATE(timestamp) = '2026-02-02'
ORDER BY timestamp;

-- Get top applications for a user on a date
SELECT 
    application_name,
    COUNT(*) as activity_count,
    MIN(timestamp) as first_activity,
    MAX(timestamp) as last_activity
FROM activity_log
WHERE username = 'john.doe'
  AND DATE(timestamp) = '2026-02-02'
GROUP BY application_name
ORDER BY activity_count DESC
LIMIT 10;

-- Get hourly activity breakdown
SELECT 
    EXTRACT(HOUR FROM timestamp) as hour,
    COUNT(*) as activity_count
FROM activity_log
WHERE username = 'john.doe'
  AND DATE(timestamp) = '2026-02-02'
GROUP BY hour
ORDER BY hour;

-- Get team summary for today
SELECT 
    username,
    COUNT(DISTINCT application_name) as apps_used,
    COUNT(*) as total_activities,
    MAX(timestamp) as last_activity
FROM activity_log
WHERE DATE(timestamp) = CURRENT_DATE
GROUP BY username
ORDER BY total_activities DESC;

-- Get all historical dates with data
SELECT DISTINCT DATE(timestamp) as activity_date
FROM activity_log
ORDER BY activity_date DESC;

-- =====================================================
-- Data Retention & Cleanup (Optional)
-- =====================================================

-- Archive data older than 6 months
-- (Only if needed for space management)
CREATE TABLE activity_log_archive AS
SELECT * FROM activity_log
WHERE timestamp < NOW() - INTERVAL '6 months';

-- Clean up archived data
-- DELETE FROM activity_log
-- WHERE timestamp < NOW() - INTERVAL '6 months';

-- =====================================================
-- Database Statistics
-- =====================================================

-- Check table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Count records by table
SELECT 'activity_log' as table_name, COUNT(*) as record_count FROM activity_log
UNION ALL
SELECT 'team_member', COUNT(*) FROM team_member
UNION ALL
SELECT 'work_session', COUNT(*) FROM work_session;

-- Get date range of stored data
SELECT 
    MIN(DATE(timestamp)) as earliest_date,
    MAX(DATE(timestamp)) as latest_date,
    COUNT(DISTINCT DATE(timestamp)) as total_days
FROM activity_log;
