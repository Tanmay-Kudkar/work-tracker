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
  }
};

export { ApiError };
