import { useEffect, useState } from 'react';
import { api } from '../api/client';
import { useAuth } from '../context/AuthContext';
import StatCard from '../components/StatCard';
import ProjectForm from '../components/ProjectForm';
import TaskForm from '../components/TaskForm';
import TaskTable from '../components/TaskTable';
import ProjectList from '../components/ProjectList';
import UserManagement from '../components/UserManagement';

export default function DashboardPage() {
  const { user, logout } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [projects, setProjects] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');

  const loadData = async () => {
    try {
      const [dashboardData, projectData, taskData, userData] = await Promise.all([
        api.dashboard(),
        api.projects(),
        api.tasks(),
        api.users()
      ]);
      setDashboard(dashboardData);
      setProjects(projectData);
      setTasks(taskData);
      setUsers(userData);
      setError('');
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleAsyncAction = async (action) => {
    try {
      await action();
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleCreateProject = async (payload) => {
    await handleAsyncAction(() => api.createProject(payload));
  };

  const handleCreateTask = async (payload) => {
    await handleAsyncAction(() => api.createTask(payload));
  };

  const handleStatusChange = async (task, status) => {
    await handleAsyncAction(() => api.updateTask(task.id, {
      title: task.title,
      description: task.description,
      assignedToId: task.assignedTo?.id || null,
      dueDate: task.dueDate,
      status
    }));
  };

  const handleEditTask = async (task) => {
    const title = window.prompt('Update task title', task.title);
    if (!title) return;
    const description = window.prompt('Update task description', task.description || '') ?? task.description;
    await handleAsyncAction(() => api.updateTask(task.id, {
      title,
      description,
      assignedToId: task.assignedTo?.id || null,
      dueDate: task.dueDate,
      status: task.status
    }));
  };

  const handleDeleteTask = async (taskId) => {
    if (!window.confirm('Delete this task?')) return;
    await handleAsyncAction(() => api.deleteTask(taskId));
  };

  const handleRoleChange = async (userId, role) => {
    await handleAsyncAction(() => api.updateRole(userId, role));
  };

  const handleAddMember = async (projectId, userId) => {
    await handleAsyncAction(() => api.addProjectMember(projectId, userId));
  };

  const handleRemoveMember = async (projectId, userId) => {
    if (!window.confirm('Remove this member from the project?')) return;
    await handleAsyncAction(() => api.removeProjectMember(projectId, userId));
  };

  const handleEditProject = async (projectId, payload) => {
    await handleAsyncAction(() => api.updateProject(projectId, payload));
  };

  const handleDeleteProject = async (projectId) => {
    if (!window.confirm('Delete this project and its tasks?')) return;
    await handleAsyncAction(() => api.deleteProject(projectId));
  };

  if (!dashboard) {
    return <div className="loading-screen">Loading dashboard...</div>;
  }

  return (
    <div className="app-shell">
      <header className="hero-header">
        <div>
          <p className="eyebrow">Workspace</p>
          <h1>Plan work, assign owners, and move projects forward.</h1>
          <p className="muted">Signed in as {user.fullName} ({user.role.replace('ROLE_', '')})</p>
        </div>
        <button className="secondary-btn" onClick={logout}>Logout</button>
      </header>

      {error ? <div className="error-banner spaced">{error}</div> : null}

      <section className="stats-grid">
        <StatCard label="Projects" value={dashboard.totalProjects} accent="linear-gradient(135deg, #2563eb, #8b5cf6)" />
        <StatCard label="All tasks" value={dashboard.totalTasks} accent="linear-gradient(135deg, #0ea5e9, #2563eb)" />
        <StatCard label="My tasks" value={dashboard.myTasks} accent="linear-gradient(135deg, #4f46e5, #7c3aed)" />
        <StatCard label="Overdue" value={dashboard.overdueTasks} accent="linear-gradient(135deg, #312e81, #2563eb)" />
      </section>

      <section className="content-grid two-up">
        <ProjectForm users={users.filter((item) => item.id !== user.id)} onCreate={handleCreateProject} />
        <TaskForm projects={projects} onCreate={handleCreateTask} />
      </section>

      <section className="content-grid">
        <ProjectList
          projects={projects}
          users={users}
          currentUser={user}
          onAddMember={handleAddMember}
          onRemoveMember={handleRemoveMember}
          onEditProject={handleEditProject}
          onDeleteProject={handleDeleteProject}
        />
      </section>

      <section className="content-grid">
        <TaskTable tasks={tasks} onStatusChange={handleStatusChange} onEditTask={handleEditTask} onDeleteTask={handleDeleteTask} />
      </section>

      {user.role === 'ROLE_ADMIN' && (
        <section className="content-grid">
          <UserManagement users={users} currentUser={user} onRoleChange={handleRoleChange} />
        </section>
      )}
    </div>
  );
}
