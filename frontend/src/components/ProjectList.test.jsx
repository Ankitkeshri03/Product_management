import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ProjectList from './ProjectList';

const baseProject = {
  id: 7,
  name: 'Platform Revamp',
  description: 'Refresh internal tooling',
  dueDate: '2026-05-20',
  owner: { id: 1, fullName: 'Owner User', email: 'owner@test.com', role: 'ROLE_MEMBER' },
  members: [{ id: 2, fullName: 'Member User', email: 'member@test.com', role: 'ROLE_MEMBER' }],
  totalTasks: 5,
  completedTasks: 2
};

describe('ProjectList', () => {
  it('shows project actions for admins and submits inline edits', async () => {
    const user = userEvent.setup();
    const onEditProject = vi.fn();
    const onDeleteProject = vi.fn();

    render(
      <ProjectList
        projects={[baseProject]}
        users={[]}
        currentUser={{ id: 99, role: 'ROLE_ADMIN' }}
        onAddMember={vi.fn()}
        onRemoveMember={vi.fn()}
        onEditProject={onEditProject}
        onDeleteProject={onDeleteProject}
      />
    );

    await user.click(screen.getByRole('button', { name: 'Edit' }));
    await user.clear(screen.getByLabelText(/project name/i));
    await user.type(screen.getByLabelText(/project name/i), 'Platform Refresh');
    await user.click(screen.getByRole('button', { name: /save changes/i }));
    await user.click(screen.getByRole('button', { name: 'Delete' }));

    expect(onEditProject).toHaveBeenCalledWith(7, {
      name: 'Platform Refresh',
      description: 'Refresh internal tooling',
      dueDate: '2026-05-20',
      memberIds: [2]
    });
    expect(onDeleteProject).toHaveBeenCalledWith(7);
  });

  it('hides management controls for non-owner members', () => {
    render(
      <ProjectList
        projects={[baseProject]}
        users={[]}
        currentUser={{ id: 3, role: 'ROLE_MEMBER' }}
        onAddMember={vi.fn()}
        onRemoveMember={vi.fn()}
        onEditProject={vi.fn()}
        onDeleteProject={vi.fn()}
      />
    );

    expect(screen.queryByRole('button', { name: 'Edit' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Delete' })).not.toBeInTheDocument();
  });
});
