export default function UserManagement({ users, currentUser, onRoleChange }) {
  return (
    <div className="panel">
      <div className="panel-heading">
        <h3>Team roles</h3>
        <p>Admins can promote or demote team members here.</p>
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>{user.fullName}</td>
                <td>{user.email}</td>
                <td>
                  {currentUser.id === user.id ? (
                    <span className="pill">{user.role}</span>
                  ) : (
                    <select value={user.role} onChange={(e) => onRoleChange(user.id, e.target.value)}>
                      <option value="ROLE_ADMIN">Admin</option>
                      <option value="ROLE_MEMBER">Member</option>
                    </select>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
