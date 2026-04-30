import { useState } from 'react';

export default function ProjectForm({ users, onCreate }) {
  const [form, setForm] = useState({ name: '', description: '', dueDate: '', memberIds: [] });

  const handleCheckbox = (userId) => {
    const exists = form.memberIds.includes(userId);
    setForm({
      ...form,
      memberIds: exists ? form.memberIds.filter((id) => id !== userId) : [...form.memberIds, userId]
    });
  };

  const submit = async (event) => {
    event.preventDefault();
    await onCreate({ ...form, dueDate: form.dueDate || null });
    setForm({ name: '', description: '', dueDate: '', memberIds: [] });
  };

  return (
    <form className="panel form-grid" onSubmit={submit}>
      <div className="panel-heading">
        <h3>Create project</h3>
        <p>Start a project and add your team in one step.</p>
      </div>
      <label>
        Name
        <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
      </label>
      <label>
        Due date
        <input type="date" value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
      </label>
      <label className="full-width">
        Description
        <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} rows="4" />
      </label>
      <div className="full-width">
        <span className="label-title">Assign members</span>
        <div className="tag-grid">
          {users.map((user) => (
            <label key={user.id} className="tag-option">
              <input type="checkbox" checked={form.memberIds.includes(user.id)} onChange={() => handleCheckbox(user.id)} />
              <span>{user.fullName}</span>
            </label>
          ))}
        </div>
      </div>
      <button className="primary-btn" type="submit">Create project</button>
    </form>
  );
}
