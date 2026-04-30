import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AuthScreen from './AuthScreen';
import { useAuth } from '../context/AuthContext';

vi.mock('../context/AuthContext', () => ({
  useAuth: vi.fn()
}));

describe('AuthScreen', () => {
  it('switches to signup mode and submits signup payload', async () => {
    const signup = vi.fn().mockResolvedValue({});
    useAuth.mockReturnValue({ login: vi.fn(), signup });
    const user = userEvent.setup();

    render(<AuthScreen />);

    await user.click(screen.getByRole('button', { name: /need an account/i }));
    await user.type(screen.getByLabelText(/full name/i), 'Ava Owner');
    await user.type(screen.getByLabelText(/^email$/i), 'ava@test.com');
    await user.type(screen.getByLabelText(/^password$/i), 'Password@123');
    await user.click(screen.getByRole('button', { name: /create account/i }));

    expect(signup).toHaveBeenCalledWith({
      fullName: 'Ava Owner',
      email: 'ava@test.com',
      password: 'Password@123'
    });
  });
});
