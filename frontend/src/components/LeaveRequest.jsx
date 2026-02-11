import { useState } from 'react';
import { api } from '../api';
import './LeaveRequest.css';

const LEAVE_TYPES = ['SICK', 'VACATION', 'PERSONAL', 'EMERGENCY', 'OTHER'];

export function LeaveRequestForm({ username, onSuccess, onCancel }) {
  const [formData, setFormData] = useState({
    startDate: '',
    endDate: '',
    leaveType: 'VACATION',
    reason: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await api.createLeaveRequest({
        username,
        ...formData
      });
      onSuccess?.();
      // Reset form
      setFormData({
        startDate: '',
        endDate: '',
        leaveType: 'VACATION',
        reason: ''
      });
    } catch (err) {
      setError(err.message || 'Failed to submit leave request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="leave-request-form">
      <h3>Request Time Off</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Start Date</label>
          <input
            type="date"
            value={formData.startDate}
            onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
            required
            min={new Date().toISOString().split('T')[0]}
          />
        </div>

        <div className="form-group">
          <label>End Date</label>
          <input
            type="date"
            value={formData.endDate}
            onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
            required
            min={formData.startDate || new Date().toISOString().split('T')[0]}
          />
        </div>

        <div className="form-group">
          <label>Leave Type</label>
          <select
            value={formData.leaveType}
            onChange={(e) => setFormData({ ...formData, leaveType: e.target.value })}
            required
          >
            {LEAVE_TYPES.map(type => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label>Reason (Optional)</label>
          <textarea
            value={formData.reason}
            onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
            placeholder="Explain why you need time off..."
            rows={3}
          />
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Submitting...' : 'Submit Request'}
          </button>
          {onCancel && (
            <button type="button" className="btn-secondary" onClick={onCancel}>
              Cancel
            </button>
          )}
        </div>
      </form>
    </div>
  );
}

export function LeaveStatus({ status }) {
  const statusColors = {
    PENDING: '#f59e0b',
    APPROVED: '#10b981',
    REJECTED: '#ef4444'
  };

  return (
    <span className="leave-status" style={{ color: statusColors[status] }}>
      {status}
    </span>
  );
}
