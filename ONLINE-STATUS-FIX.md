# Online Status Fix

## Problem
The tracker.py was sending session event data (start/end) to the backend, but the **online status was not being updated** on the frontend dashboard.

## Root Cause
The `tracker.py` Python script sends POST requests to `/api/sessions` with session event data:
```python
def send_session_event(app_name, process_name, event_type, end_reason=None):
    data = {
        "username": USERNAME,
        "applicationName": app_name,
        "processName": process_name,
        "eventType": event_type  # "start", "end", or "heartbeat"
    }
    response = requests.post(SESSION_URL, json=data, timeout=5)
```

**BUT** the backend `SessionController` had NO endpoint to handle plain POST requests to `/api/sessions`. It only had:
- `/api/sessions/heartbeat` - for heartbeat tracking
- `/api/sessions/logout` - for logout
- GET endpoints for retrieving session data

## Solution

### 1. Added POST endpoint to SessionController
Added a new endpoint to handle session events from tracker.py:

```java
@PostMapping
public ApiResponse<Map<String, String>> handleSessionEvent(@RequestBody SessionEventRequest request) {
    return sessionService.processSessionEvent(request);
}
```

### 2. Implemented processSessionEvent in SessionService
Added the `processSessionEvent` method to handle start/end/heartbeat events:

```java
@Transactional
public ApiResponse<Map<String, String>> processSessionEvent(SessionEventRequest request) {
    // Validates the team member
    // Processes event type: start, end, or heartbeat
    // Updates TeamMember's isCurrentlyWorking and currentApplication fields
    
    switch (eventType) {
        case "start":
            handleSessionStart(member, applicationName);
            break;
        case "end":
            handleSessionEnd(member, applicationName);
            break;
        case "heartbeat":
            handleSessionHeartbeat(member, applicationName);
            break;
    }
}
```

### 3. How it works now

#### When tracker starts tracking an app:
1. Tracker sends: `POST /api/sessions` with `eventType: "start"`
2. Backend calls `handleSessionStart()`:
   - Sets `isCurrentlyWorking = true`
   - Sets `currentApplication = appName`
   - Saves to database

#### When tracker stops tracking an app:
1. Tracker sends: `POST /api/sessions` with `eventType: "end"`
2. Backend calls `handleSessionEnd()`:
   - Sets `isCurrentlyWorking = false`
   - Sets `currentApplication = null`
   - Saves to database

#### When tracker sends activity:
1. Tracker sends: `POST /api/activity` with activity data
2. This keeps the activity log updated (already working)

## What Changed

### Files Modified:
1. **SessionController.java** - Added `@PostMapping` endpoint for session events
2. **SessionService.java** - Added `processSessionEvent()` and helper methods

### Database Updates:
The TeamMember entity now properly updates:
- `isCurrentlyWorking` (boolean) - Shows green/red status
- `currentApplication` (string) - Shows which app they're using

## Testing
1. Start the tracker: `python tracker.py`
2. The tracker will send a "start" event for WindowsTerminal.exe
3. Check the dashboard - you should now see:
   - Green indicator for online status
   - Current application showing

## Deployment
The backend has been rebuilt with the changes:
```bash
cd backend
mvn clean package -DskipTests
mvn spring-boot:run
```

The fix is now live and the online status should be working properly! ✅
