import { useState } from 'react';
import { api } from '../api';
import './HolidayCalendar.css';

const PRESET_HOLIDAYS_2026 = [
  // January
  { name: "New Year's Day", date: "2026-01-01", description: "New Year celebration" },
  { name: "Republic Day", date: "2026-01-26", description: "Indian Republic Day" },
  
  // February
  { name: "Maha Shivaratri", date: "2026-02-17", description: "Hindu festival" },
  
  // March
  { name: "Holi", date: "2026-03-06", description: "Festival of colors" },
  
  // April
  { name: "Good Friday", date: "2026-04-03", description: "Christian holiday" },
  { name: "Mahavir Jayanti", date: "2026-04-06", description: "Jain festival" },
  { name: "Ram Navami", date: "2026-04-10", description: "Hindu festival" },
  
  // May
  { name: "May Day", date: "2026-05-01", description: "Labour Day" },
  { name: "Buddha Purnima", date: "2026-05-04", description: "Buddha's birthday" },
  
  // August
  { name: "Independence Day", date: "2026-08-15", description: "Indian Independence Day" },
  { name: "Raksha Bandhan", date: "2026-08-28", description: "Hindu festival" },
  
  // September
  { name: "Ganesh Chaturthi", date: "2026-09-02", description: "Hindu festival celebrating Lord Ganesha" },
  
  // October
  { name: "Gandhi Jayanti", date: "2026-10-02", description: "Gandhi's birthday" },
  { name: "Dussehra", date: "2026-10-12", description: "Victory of good over evil" },
  { name: "Diwali", date: "2026-11-01", description: "Festival of lights" },
  
  // November
  { name: "Guru Nanak Jayanti", date: "2026-11-12", description: "Sikh festival" },
  
  // December
  { name: "Christmas", date: "2026-12-25", description: "Christian celebration of Jesus's birth" }
];

const MONTHS = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
];

export function HolidayCalendar({ existingHolidays, onAdd, onClose }) {
  const [addingHolidays, setAddingHolidays] = useState(new Set());
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth());

  const existingDates = new Set(existingHolidays.map(h => h.date));

  const holidaysByMonth = MONTHS.map((month, index) => ({
    month,
    holidays: PRESET_HOLIDAYS_2026.filter(h => {
      const holidayMonth = new Date(h.date).getMonth();
      return holidayMonth === index;
    })
  }));

  const handleAddHoliday = async (holiday) => {
    setAddingHolidays(prev => new Set([...prev, holiday.date]));
    try {
      await api.createHoliday(holiday);
      await onAdd();
    } catch (err) {
      alert(err.message || 'Failed to add holiday');
    } finally {
      setAddingHolidays(prev => {
        const next = new Set(prev);
        next.delete(holiday.date);
        return next;
      });
    }
  };

  const handleAddAll = async () => {
    const holidaysToAdd = PRESET_HOLIDAYS_2026.filter(h => !existingDates.has(h.date));
    
    if (holidaysToAdd.length === 0) {
      alert('All preset holidays are already added!');
      return;
    }

    if (!confirm(`Add ${holidaysToAdd.length} holidays to the calendar?`)) {
      return;
    }

    for (const holiday of holidaysToAdd) {
      try {
        await api.createHoliday(holiday);
      } catch (err) {
        console.error(`Failed to add ${holiday.name}:`, err);
      }
    }
    await onAdd();
  };

  return (
    <div className="holiday-calendar-overlay">
      <div className="holiday-calendar-modal">
        <div className="modal-header">
          <h2>📅 Add Company Holidays</h2>
          <button className="close-btn" onClick={onClose}>✕</button>
        </div>

        <div className="modal-actions">
          <button className="btn-add-all" onClick={handleAddAll}>
            ⚡ Add All Holidays (2026)
          </button>
          <p className="help-text">
            Or select individual holidays below to add them one by one
          </p>
        </div>

        <div className="month-selector">
          {MONTHS.map((month, index) => (
            <button
              key={month}
              className={`month-tab ${selectedMonth === index ? 'active' : ''}`}
              onClick={() => setSelectedMonth(index)}
            >
              {month.slice(0, 3)}
            </button>
          ))}
        </div>

        <div className="holidays-container">
          {holidaysByMonth[selectedMonth].holidays.length === 0 ? (
            <div className="no-holidays">
              <p>No preset holidays for {MONTHS[selectedMonth]}</p>
            </div>
          ) : (
            <div className="preset-holidays-list">
              {holidaysByMonth[selectedMonth].holidays.map((holiday) => {
                const isAdded = existingDates.has(holiday.date);
                const isAdding = addingHolidays.has(holiday.date);
                
                return (
                  <div key={holiday.date} className={`preset-holiday-item ${isAdded ? 'added' : ''}`}>
                    <div className="holiday-info">
                      <div className="holiday-header">
                        <span className="holiday-name">{holiday.name}</span>
                        <span className="holiday-date">
                          {new Date(holiday.date).toLocaleDateString('en-US', {
                            month: 'short',
                            day: 'numeric'
                          })}
                        </span>
                      </div>
                      <div className="holiday-desc">{holiday.description}</div>
                    </div>
                    <button
                      className={`add-holiday-btn ${isAdded ? 'added' : ''}`}
                      onClick={() => handleAddHoliday(holiday)}
                      disabled={isAdded || isAdding}
                    >
                      {isAdding ? '...' : isAdded ? '✓ Added' : '+ Add'}
                    </button>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
