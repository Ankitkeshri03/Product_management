import { useState } from 'react';

const createDraft = (project) => ({
  name: project.name,
  description: project.description || '',
  dueDate: project.dueDate || ''
});

export default function ProjectList({ projects, users, currentUser, onAddMember, onRemoveMember, onEditProject, onDeleteProject }) {
  const [editingProjectId, setEditingProjectId] = useState(null);
  const [draft, setDraft] = useState(null);

  const beginEdit = (project) => {
    setEditingProjectId(project.id);
    setDraft(createDraft(project));
  };

  const cancelEdit = () => {
    setEditingProjectId(null);
    setDraft(null);
  };

  const submitEdit = async (event, project) => {
    event.preventDefault();
    await onEditProject(project.id, {
      name: draft.name.trim(),
      description: draft.description.trim() || null,
      dueDate: draft.dueDate || null,
      memberIds: project.members.map((member) => member.id)
    });
    cancelEdit();
  };

  return (
    <div className="panel">
      <div className="panel-heading">
        <h3>Projects</h3>
        <p>Each card shows ownership, scope, progress, and collaborators.</p>
      </div>
      <div className="project-grid">
        {projects.map((project) => {
          const canManageProject = currentUser.role === 'ROLE_ADMIN' || project.owner.id === currentUser.id;
          const isEditing = editingProjectId === project.id && draft;
          return (
            <article key={project.id} className="project-card">
              <div className="project-topline">
                <h4>{project.name}</h4>
                <span className="pill">{project.completedTasks}/{project.totalTasks} done</span>
              </div>
              {isEditing ? (
                <form className="project-edit-form" onSubmit={(event) => submitEdit(event, project)}>
                  <label>
                    Project name
                    <input
                      value={draft.name}
                      minLength="3"
                      maxLength="120"
                      onChange={(event) => setDraft({ ...draft, name: event.target.value })}
                      required
                    />
                  </label>
                  <label>
                    Due date
                    <input
                      type="date"
                      value={draft.dueDate}
                      onChange={(event) => setDraft({ ...draft, dueDate: event.target.value })}
                    />
                  </label>
                  <label className="full-width">
                    Description
                    <textarea
                      rows="4"
                      maxLength="1000"
                      value={draft.description}
                      onChange={(event) => setDraft({ ...draft, description: event.target.value })}
                    />
                  </label>
                  <div className="action-row">
                    <button type="submit" className="primary-btn compact-btn">Save changes</button>
                    <button type="button" className="secondary-btn compact-btn" onClick={cancelEdit}>Cancel</button>
                  </div>
                </form>
              ) : (
                <>
                  <p>{project.description || 'No description added yet.'}</p>
                  <div className="meta-line">
                    <span>Owner: {project.owner.fullName}</span>
                    <span>Due: {project.dueDate || 'Flexible'}</span>
                  </div>
                </>
              )}
              <div className="member-stack">
                {[project.owner, ...project.members.filter((member) => member.id !== project.owner.id)].map((member) => (
                  <span key={member.id} className="member-chip">
                    {member.fullName}
                    {canManageProject && !isEditing && member.id !== project.owner.id ? (
                      <button type="button" className="chip-remove" onClick={() => onRemoveMember(project.id, member.id)}>
                        ×
                      </button>
                    ) : null}
                  </span>
                ))}
              </div>
              {canManageProject && !isEditing && (
                <div className="inline-member-add">
                  <select onChange={(e) => e.target.value && onAddMember(project.id, Number(e.target.value))} defaultValue="">
                    <option value="">Add member</option>
                    {users
                      .filter((user) => user.id !== project.owner.id && !project.members.some((member) => member.id === user.id))
                      .map((user) => <option key={user.id} value={user.id}>{user.fullName}</option>)}
                  </select>
                </div>
              )}
              {canManageProject && !isEditing && (
                <div className="action-row">
                  <button type="button" className="secondary-btn compact-btn" onClick={() => beginEdit(project)}>Edit</button>
                  <button type="button" className="danger-btn compact-btn" onClick={() => onDeleteProject(project.id)}>Delete</button>
                </div>
              )}
            </article>
          );
        })}
      </div>
    </div>
  );
}
