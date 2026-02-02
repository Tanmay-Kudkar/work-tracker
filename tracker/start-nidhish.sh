#!/bin/bash
# WorkTracker - Nidhish Vartak (PRODUCTION)
cd "$(dirname "$0")"
export TRACKER_USER=nidhish_vartak
export TRACKER_ENV=production
python3 tracker.py
