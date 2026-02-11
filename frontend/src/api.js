const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080/api";
const API_URL = `${API_BASE}/activity`;

function getTzOffsetMinutes() {
  // JS returns minutes *behind* UTC. We want minutes *ahead* of UTC (e.g., IST => +330).
  return -new Date().getTimezoneOffset();
}

class ApiError extends Error {
  constructor(message, status, data = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

async function handleResponse(response) {
  const data = await response.json();
  
  if (!response.ok) {
    throw new ApiError(
      data.message || 'An error occurred',
      response.status,
      data
    );
  }
  
  // Handle new ApiResponse format
  if (data.hasOwnProperty('success')) {
    if (!data.success) {
      throw new ApiError(data.message || 'Request failed', response.status, data);
    }
    return data.data;
  }
  
  return data;
}

export const api = {
  async getSummary(date) {
    const tzOffsetMinutes = getTzOffsetMinutes();
    const response = await fetch(`${API_URL}/summary?date=${date}&tzOffsetMinutes=${tzOffsetMinutes}`);
    return handleResponse(response);
  },

  async getDashboard(username, date) {
    const tzOffsetMinutes = getTzOffsetMinutes();
    const response = await fetch(`${API_URL}/dashboard/${username}?date=${date}&tzOffsetMinutes=${tzOffsetMinutes}`);
    return handleResponse(response);
  },

  async getWeeklySummary(date) {
    const tzOffsetMinutes = getTzOffsetMinutes();
    const response = await fetch(`${API_URL}/weekly-summary?date=${date}&tzOffsetMinutes=${tzOffsetMinutes}`);
    return handleResponse(response);
  },

  async getResources(username) {
    const response = await fetch(`${API_URL}/resources/${username}`);
    return handleResponse(response);
  },

  async logActivity(data) {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });
    return handleResponse(response);
  },

  // Leave Management APIs
  async createLeaveRequest(leaveData) {
    const response = await fetch(`${API_BASE}/leaves/request`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(leaveData),
    });
    return handleResponse(response);
  },

  async getUserLeaves(username) {
    const response = await fetch(`${API_BASE}/leaves/user/${username}`);
    return handleResponse(response);
  },

  async getPendingLeaves() {
    const response = await fetch(`${API_BASE}/leaves/pending`);
    return handleResponse(response);
  },

  async getApprovedLeaves(startDate, endDate) {
    const response = await fetch(`${API_BASE}/leaves/approved?startDate=${startDate}&endDate=${endDate}`);
    return handleResponse(response);
  },

  async approveLeave(leaveId, approvedBy) {
    const response = await fetch(`${API_BASE}/leaves/${leaveId}/approve?approvedBy=${approvedBy}`, {
      method: 'POST',
    });
    return handleResponse(response);
  },

  async rejectLeave(leaveId, rejectedBy) {
    const response = await fetch(`${API_BASE}/leaves/${leaveId}/reject?rejectedBy=${rejectedBy}`, {
      method: 'POST',
    });
    return handleResponse(response);
  },

  async deleteLeaveRequest(leaveId) {
    const response = await fetch(`${API_BASE}/leaves/${leaveId}`, {
      method: 'DELETE',
    });
    return handleResponse(response);
  },

  // Holiday Management APIs
  async createHoliday(holidayData) {
    const response = await fetch(`${API_BASE}/leaves/holidays`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(holidayData),
    });
    return handleResponse(response);
  },

  async getAllHolidays() {
    const response = await fetch(`${API_BASE}/leaves/holidays`);
    return handleResponse(response);
  },

  async getUpcomingHolidays() {
    const response = await fetch(`${API_BASE}/leaves/holidays/upcoming`);
    return handleResponse(response);
  },

  async deleteHoliday(holidayId) {
    const response = await fetch(`${API_BASE}/leaves/holidays/${holidayId}`, {
      method: 'DELETE',
    });
    return handleResponse(response);
  }
};

export { ApiError };
