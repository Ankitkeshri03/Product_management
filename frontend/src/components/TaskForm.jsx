import { useMemo, useState } from 'react';

const emptyTask = { title: '', description: '', projectId: '', assignedToId: '', dueDate: '', status: 'TODO' };

export default function TaskForm({ projects, onCreate }) {
  const [form, setForm] = useState(emptyTask);

  const availableMembers = useMemo(() => {
    const project = projects.find((item) => item.id === Number(form.projectId));
    if (!project) return [];
    return [project.owner, ...project.members.filter((member) => member.id !== project.owner.id)];
  }, [projects, form.projectId]);

  const submit = async (event) => {
    event.preventDefault();
    await onCreate({
      ...form,
      projectId: Number(form.projectId),
      assignedToId: form.assignedToId ? Number(form.assignedToId) : null,
      dueDate: form.dueDate || null
    });
    setForm(emptyTask);
  };

  return (
    <form className="panel form-grid" onSubmit={submit}>
      <div className="panel-heading">
        <h3>Create task</h3>
        <p>Assign ownership, due date, and progress from one place.</p>
      </div>
      <label>
        Title
        <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
      </label>
      <label>
        Project
        <select value={form.projectId} onChange={(e) => setForm({ ...form, projectId: e.target.value, assignedToId: '' })} required>
          <option value="">Select project</option>
          {projects.map((project) => <option key={project.id} value={project.id}>{project.name}</option>)}
        </select>
      </label>
      <label>
        Assignee
        <select value={form.assignedToId} onChange={(e) => setForm({ ...form, assignedToId: e.target.value })}>
          <option value="">Unassigned</option>
          {availableMembers.map((member) => <option key={member.id} value={member.id}>{member.fullName}</option>)}
        </select>
      </label>
      <label>
        Due date
        <input type="date" value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
      </label>
      <label>
        Status
        <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
          <option value="TODO">Todo</option>
          <option value="IN_PROGRESS">In progress</option>
          <option value="DONE">Done</option>
        </select>
      </label>
      <label className="full-width">
        Description
        <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} rows="4" />
      </label>
      <button className="primary-btn" type="submit">Create task</button>
    </form>
  );
}
