const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

async function parseErrorResponse(response) {
  const contentType = response.headers.get('content-type') || '';

  if (contentType.includes('application/json')) {
    const error = await response.json().catch(() => null);
    if (!error) {
      return null;
    }

    if (error.validations && typeof error.validations === 'object') {
      const validationMessage = Object.values(error.validations).find(
        (value) => typeof value === 'string' && value.trim()
      );
      if (validationMessage) {
        return validationMessage;
      }
    }

    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }

    if (typeof error.message === 'string' && error.message.trim()) {
      return error.message;
    }
  }

  const text = await response.text().catch(() => '');
  return text.trim() || null;
}

async function request(path, options = {}) {
  const token = localStorage.getItem('taskmanager_token');
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers
    });
  } catch (error) {
    const backendHint = API_BASE_URL.startsWith('http') ? API_BASE_URL : 'the configured backend';
    throw new Error(`Cannot reach backend at ${backendHint}. Make sure the Spring Boot server is running.`);
  }

  if (!response.ok) {
    const errorMessage = await parseErrorResponse(response);

    if ([500, 502, 503, 504].includes(response.status)) {
      throw new Error(errorMessage || 'Backend is unavailable. Restart the Spring Boot server and try again.');
    }

    throw new Error(errorMessage || `Request failed with status ${response.status}`);
  }

  return response.status === 204 ? null : response.json();
}

export const api = {
  login: (payload) => request('/auth/login', { method: 'POST', body: JSON.stringify(payload) }),
  signup: (payload) => request('/auth/signup', { method: 'POST', body: JSON.stringify(payload) }),
  me: () => request('/users/me'),
  users: () => request('/users'),
  updateRole: (userId, role) => request(`/users/${userId}/role`, { method: 'PUT', body: JSON.stringify({ role }) }),
  dashboard: () => request('/dashboard'),
  projects: () => request('/projects'),
  createProject: (payload) => request('/projects', { method: 'POST', body: JSON.stringify(payload) }),
  updateProject: (projectId, payload) => request(`/projects/${projectId}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteProject: (projectId) => request(`/projects/${projectId}`, { method: 'DELETE' }),
  addProjectMember: (projectId, userId) => request(`/projects/${projectId}/members/${userId}`, { method: 'POST' }),
  removeProjectMember: (projectId, userId) => request(`/projects/${projectId}/members/${userId}`, { method: 'DELETE' }),
  tasks: () => request('/tasks'),
  createTask: (payload) => request('/tasks', { method: 'POST', body: JSON.stringify(payload) }),
  updateTask: (taskId, payload) => request(`/tasks/${taskId}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteTask: (taskId) => request(`/tasks/${taskId}`, { method: 'DELETE' })
};
