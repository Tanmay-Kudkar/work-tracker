const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api/activity";
const SESSION_URL = import.meta.env.VITE_SESSION_URL || "http://localhost:8080/api/sessions";

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
    const response = await fetch(`${API_URL}/summary?date=${date}`);
    return handleResponse(response);
  },

  async getDashboard(username, date) {
    const response = await fetch(`${API_URL}/dashboard/${username}?date=${date}`);
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
