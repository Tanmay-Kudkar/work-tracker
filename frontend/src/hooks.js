import { useState, useEffect, useCallback } from 'react';
import { api, ApiError } from './api';

export function useMembers(selectedDate) {
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchSummary = useCallback(async () => {
    try {
      setError(null);
      const data = await api.getSummary(selectedDate);
      setMembers(data);
    } catch (err) {
      console.error("Error fetching summary:", err);
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Failed to connect to server");
      }
    } finally {
      setLoading(false);
    }
  }, [selectedDate]);

  useEffect(() => {
    fetchSummary();
    const interval = setInterval(fetchSummary, 5000);
    return () => clearInterval(interval);
  }, [fetchSummary]);

  return { members, loading, error, refetch: fetchSummary };
}

export function useDashboard(username, selectedDate) {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchDashboard = useCallback(async () => {
    if (!username) {
      setDashboard(null);
      setLoading(false);
      return;
    }

    try {
      setError(null);
      const data = await api.getDashboard(username, selectedDate);
      setDashboard(data);
    } catch (err) {
      console.error("Error fetching dashboard:", err);
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Failed to load dashboard");
      }
    } finally {
      setLoading(false);
    }
  }, [username, selectedDate]);

  useEffect(() => {
    if (username) {
      setLoading(true);
      fetchDashboard();
      const interval = setInterval(fetchDashboard, 5000);
      return () => clearInterval(interval);
    }
  }, [fetchDashboard, username]);

  return { dashboard, loading, error, refetch: fetchDashboard };
}

export function useWeeklySummary(selectedDate) {
  const [weeklySummary, setWeeklySummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchWeeklySummary = useCallback(async () => {
    try {
      setError(null);
      const data = await api.getWeeklySummary(selectedDate);
      setWeeklySummary(data);
    } catch (err) {
      console.error("Error fetching weekly summary:", err);
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("Failed to load weekly summary");
      }
    } finally {
      setLoading(false);
    }
  }, [selectedDate]);

  useEffect(() => {
    fetchWeeklySummary();
    const interval = setInterval(fetchWeeklySummary, 30000); // Refresh every 30 seconds
    return () => clearInterval(interval);
  }, [fetchWeeklySummary]);

  return { weeklySummary, loading, error, refetch: fetchWeeklySummary };
}

export function useCurrentTime() {
  const [currentTime, setCurrentTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  return currentTime;
}
