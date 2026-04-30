export default function TaskTable({ tasks, onStatusChange, onEditTask, onDeleteTask }) {
  return (
    <div className="panel">
      <div className="panel-heading">
        <h3>Tasks</h3>
        <p>Track assignment, status, due date, and overdue work.</p>
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Task</th>
              <th>Project</th>
              <th>Assignee</th>
              <th>Status</th>
              <th>Due</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {tasks.map((task) => (
              <tr key={task.id}>
                <td>
                  <strong>{task.title}</strong>
                  <span>{task.description || 'No description'}</span>
                </td>
                <td>{task.projectName}</td>
                <td>{task.assignedTo?.fullName || 'Unassigned'}</td>
                <td>
                  <select value={task.status} onChange={(e) => onStatusChange(task, e.target.value)}>
                    <option value="TODO">Todo</option>
                    <option value="IN_PROGRESS">In progress</option>
                    <option value="DONE">Done</option>
                  </select>
                </td>
                <td>
                  <span className={task.overdue ? 'pill pill-danger' : 'pill'}>{task.dueDate || 'No due date'}</span>
                </td>
                <td>
                  <div className="action-row">
                    <button type="button" className="secondary-btn compact-btn" onClick={() => onEditTask(task)}>Edit</button>
                    <button type="button" className="danger-btn compact-btn" onClick={() => onDeleteTask(task.id)}>Delete</button>
                  </div>
                </td>
              </tr>
            ))}
            {tasks.length === 0 && (
              <tr>
                <td colSpan="6" className="empty-row">No tasks yet.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
