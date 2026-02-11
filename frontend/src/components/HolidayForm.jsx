import { useState } from 'react';
import { api } from '../api';
import './LeaveRequest.css';

export function HolidayForm({ onSuccess, onCancel }) {
  const [formData, setFormData] = useState({
    name: '',
    date: '',
    description: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await api.createHoliday(formData);
      onSuccess?.();
      // Reset form
      setFormData({
        name: '',
        date: '',
        description: ''
      });
    } catch (err) {
      setError(err.message || 'Failed to create holiday');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="leave-request-form">
      <h3>Add Company Holiday</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Holiday Name *</label>
          <input
            type="text"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            placeholder="e.g., Christmas, New Year's Day"
            required
          />
        </div>

        <div className="form-group">
          <label>Date *</label>
          <input
            type="date"
            value={formData.date}
            onChange={(e) => setFormData({ ...formData, date: e.target.value })}
            required
          />
        </div>

        <div className="form-group">
          <label>Description (Optional)</label>
          <textarea
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            placeholder="Additional notes about this holiday..."
            rows={2}
          />
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Adding...' : 'Add Holiday'}
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
