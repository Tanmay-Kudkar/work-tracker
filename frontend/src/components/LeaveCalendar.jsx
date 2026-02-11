import { useState, useEffect } from 'react';
import { api } from '../api';
import { LeaveRequestForm, LeaveStatus } from './LeaveRequest';
import { HolidayForm } from './HolidayForm';
import { HolidayCalendar } from './HolidayCalendar';
import './LeaveCalendar.css';

export function LeaveCalendar({ currentUser = 'atharva_raut' }) {
  const [leaves, setLeaves] = useState([]);
  const [holidays, setHolidays] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showRequestForm, setShowRequestForm] = useState(false);
  const [showHolidayForm, setShowHolidayForm] = useState(false);
  const [showHolidayCalendar, setShowHolidayCalendar] = useState(false);
  const [activeView, setActiveView] = useState('my-leaves'); // 'my-leaves', 'all-leaves', 'holidays'

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [userLeaves, allHolidays] = await Promise.all([
        api.getUserLeaves(currentUser),
        api.getAllHolidays()
      ]);
      setLeaves(userLeaves);
      setHolidays(allHolidays);
    } catch (err) {
      console.error('Error loading leave data:', err);
      setError(err.message || 'Failed to load leave data. Make sure the backend server is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [currentUser]);

  const handleDeleteLeave = async (leaveId) => {
    if (!confirm('Are you sure you want to delete this leave request?')) return;
    
    try {
      await api.deleteLeaveRequest(leaveId);
      await loadData();
    } catch (err) {
      alert(err.message || 'Failed to delete leave request');
    }
  };

  const handleDeleteHoliday = async (holidayId) => {
    if (!confirm('Are you sure you want to delete this holiday?')) return;
    
    try {
      await api.deleteHoliday(holidayId);
      await loadData();
    } catch (err) {
      alert(err.message || 'Failed to delete holiday');
    }
  };

  const formatDate = (dateStr) => {
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const calculateDays = (startDate, endDate) => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = Math.abs(end - start);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return diffDays;
  };

  if (loading) {
    return (
      <div className="leave-calendar loading">
        <div className="loading-spinner"></div>
        <p>Loading leave data...</p>
      </div>
    );
  }

  return (
    <div className="leave-calendar">
      <div className="leave-calendar-header">
        <h2>Leave Management</h2>
      </div>

      {showRequestForm && (
        <LeaveRequestForm
          username={currentUser}
          onSuccess={() => {
            setShowRequestForm(false);
            loadData();
          }}
          onCancel={() => setShowRequestForm(false)}
        />
      )}

      {error && <div className="error-banner">{error}</div>}

      <div className="view-tabs">
        <button
          className={`tab ${activeView === 'my-leaves' ? 'active' : ''}`}
          onClick={() => setActiveView('my-leaves')}
        >
          My Leaves ({leaves.length})
        </button>
        <button
          className={`tab ${activeView === 'holidays' ? 'active' : ''}`}
          onClick={() => setActiveView('holidays')}
        >
          Holidays ({holidays.length})
        </button>
      </div>

      {activeView === 'my-leaves' && (
        <div className="leaves-section">
          <h3>My Leave Requests</h3>
          {leaves.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🏖️</div>
              <p className="empty-title">No Leave Requests Yet</p>
              <p className="empty-subtitle">Start by requesting your first time off</p>
              <button className="btn-primary" onClick={() => setShowRequestForm(true)}>
                + Request Your First Leave
              </button>
            </div>
          ) : (
            <div className="leaves-list">
              {leaves.map(leave => (
                <div key={leave.id} className="leave-card">
                  <div className="leave-card-header">
                    <div>
                      <span className="leave-type">{leave.leaveType}</span>
                      <LeaveStatus status={leave.status} />
                    </div>
                    {leave.status === 'PENDING' && (
                      <button
                        className="btn-delete"
                        onClick={() => handleDeleteLeave(leave.id)}
                      >
                        Delete
                      </button>
                    )}
                  </div>
                  <div className="leave-card-body">
                    <div className="leave-dates">
                      <span className="date-label">From:</span>
                      <span className="date-value">{formatDate(leave.startDate)}</span>
                      <span className="date-label">To:</span>
                      <span className="date-value">{formatDate(leave.endDate)}</span>
                      <span className="days-count">
                        ({calculateDays(leave.startDate, leave.endDate)} days)
                      </span>
                    </div>
                    {leave.reason && (
                      <div className="leave-reason">
                        <strong>Reason:</strong> {leave.reason}
                      </div>
                    )}
                    <div className="leave-meta">
                      Requested on {formatDate(leave.requestedAt)}
                      {leave.approvedBy && (
                        <span> • {leave.status} by {leave.approvedBy}</span>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {activeView === 'holidays' && (
        <div className="holidays-section">
          <div className="section-header">
            <h3>Company Holidays</h3>
            <div className="header-buttons">
              <button
                className="btn-add-from-calendar"
                onClick={() => setShowHolidayCalendar(true)}
              >
                📅 Add from Calendar
              </button>
              <button
                className="btn-add-holiday"
                onClick={() => setShowHolidayForm(!showHolidayForm)}
              >
                {showHolidayForm ? '✕ Cancel' : '+ Custom Holiday'}
              </button>
            </div>
          </div>

          {showHolidayForm && (
            <HolidayForm
              onSuccess={() => {
                setShowHolidayForm(false);
                loadData();
              }}
              onCancel={() => setShowHolidayForm(false)}
            />
          )}

          {holidays.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🎉</div>
              <p className="empty-title">No Holidays Scheduled</p>
              <p className="empty-subtitle">Add company-wide holidays for your team</p>
              <div className="empty-actions">
                <button className="btn-primary" onClick={() => setShowHolidayCalendar(true)}>
                  📅 Browse Calendar
                </button>
                <button className="btn-secondary-outline" onClick={() => setShowHolidayForm(true)}>
                  + Custom Holiday
                </button>
              </div>
            </div>
          ) : (
            <div className="holidays-list">
              {holidays
                .sort((a, b) => new Date(a.date) - new Date(b.date))
                .map(holiday => {
                  const isPast = new Date(holiday.date) < new Date();
                  return (
                    <div key={holiday.id} className={`holiday-card ${isPast ? 'past' : ''}`}>
                      <button
                        className="holiday-delete-btn"
                        onClick={() => handleDeleteHoliday(holiday.id)}
                        title="Delete holiday"
                      >
                        ✕
                      </button>
                      <div className="holiday-date">
                        {new Date(holiday.date).toLocaleDateString('en-US', {
                          weekday: 'short',
                          month: 'short',
                          day: 'numeric',
                          year: 'numeric'
                        })}
                      </div>
                      <div className="holiday-name">{holiday.name}</div>
                      {holiday.description && (
                        <div className="holiday-description">{holiday.description}</div>
                      )}
                    </div>
                  );
                })}
            </div>
          )}
        </div>
      )}

      {showHolidayCalendar && (
        <HolidayCalendar
          existingHolidays={holidays}
          onAdd={() => {
            loadData();
            setShowHolidayCalendar(false);
          }}
          onClose={() => setShowHolidayCalendar(false)}
        />
      )}
    </div>
  );
}
