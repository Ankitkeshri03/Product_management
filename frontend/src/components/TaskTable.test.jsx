import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskTable from './TaskTable';

const task = {
  id: 5,
  title: 'Ship metrics page',
  description: 'Finalize status widgets',
  projectName: 'Analytics',
  assignedTo: { id: 2, fullName: 'Member User' },
  status: 'TODO',
  dueDate: '2026-05-02',
  overdue: false
};

describe('TaskTable', () => {
  it('calls handlers for status changes and destructive actions', async () => {
    const user = userEvent.setup();
    const onStatusChange = vi.fn();
    const onEditTask = vi.fn();
    const onDeleteTask = vi.fn();

    render(
      <TaskTable
        tasks={[task]}
        onStatusChange={onStatusChange}
        onEditTask={onEditTask}
        onDeleteTask={onDeleteTask}
      />
    );

    await user.selectOptions(screen.getByRole('combobox'), 'DONE');
    await user.click(screen.getByRole('button', { name: 'Edit' }));
    await user.click(screen.getByRole('button', { name: 'Delete' }));

    expect(onStatusChange).toHaveBeenCalledWith(task, 'DONE');
    expect(onEditTask).toHaveBeenCalledWith(task);
    expect(onDeleteTask).toHaveBeenCalledWith(5);
  });
});
