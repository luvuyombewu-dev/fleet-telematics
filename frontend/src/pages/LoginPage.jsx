import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Activity, ArrowRight, Eye, EyeOff, LockKeyhole, ShieldCheck, Truck } from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { ApiError } from '../lib/api.js';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setBusy(true);
    try {
      await login(username.trim(), password);
      navigate(location.state?.from || '/', { replace: true });
    } catch (err) {
      setError(err instanceof ApiError && err.status === 401 ? 'The username or password is incorrect.' : err.message || 'Could not sign in. Try again.');
    } finally {
      setBusy(false);
    }
  }

  return <div className="login-page">
    <section className="login-story">
      <div className="login-brand"><div className="brand-mark"><Truck size={23} /></div><div><strong>Fleet<span>Ops</span></strong><small>TELEMATICS PLATFORM</small></div></div>
      <div className="story-content">
        <span className="overline"><Activity size={15} /> FLEET OPERATIONS INTELLIGENCE</span>
        <h1>Every vehicle.<br />Every route.<br /><em>In view.</em></h1>
        <p>One secure workspace for vehicle tracking, driver assignments, trip activity and fleet health.</p>
        <div className="story-proof"><div className="proof-icon"><ShieldCheck size={19} /></div><div><strong>Access protected by design</strong><span>JWT authentication · Role-based permissions</span></div></div>
      </div>
      <div className="story-foot"><span>FLEET TELEMATICS & VEHICLE TRACKING</span><span>01 / SECURE ACCESS</span></div>
      <div className="story-grid" aria-hidden="true"><span /><span /><span /><span /><span /><span /><span /><span /><span /></div>
    </section>
    <section className="login-form-panel">
      <div className="login-form-wrap">
        <div className="mobile-login-brand"><div className="brand-mark"><Truck size={22} /></div><strong>Fleet<span>Ops</span></strong></div>
        <div className="login-form-heading"><div className="form-icon"><LockKeyhole size={21} /></div><span className="overline">SECURE WORKSPACE</span><h2>Welcome back</h2><p>Sign in using your fleet account credentials.</p></div>
        {error && <div className="form-alert" role="alert">{error}</div>}
        <form onSubmit={handleSubmit} className="login-form">
          <label className="field-label" htmlFor="username">Username</label>
          <input id="username" name="username" autoComplete="username" value={username} onChange={(event) => setUsername(event.target.value)} placeholder="Enter your username" required autoFocus />
          <div className="password-label-row"><label className="field-label" htmlFor="password">Password</label></div>
          <div className="password-input-wrap"><input id="password" name="password" autoComplete="current-password" type={showPassword ? 'text' : 'password'} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Enter your password" required /><button type="button" className="password-toggle" onClick={() => setShowPassword((visible) => !visible)} aria-label={showPassword ? 'Hide password' : 'Show password'}>{showPassword ? <EyeOff size={17} /> : <Eye size={17} />}</button></div>
          <button className="button button-primary login-submit" type="submit" disabled={busy}>{busy ? 'Signing in…' : 'Sign in securely'}{!busy && <ArrowRight size={17} />}</button>
        </form>
        <div className="login-security-foot"><ShieldCheck size={15} /><span>Your session is authenticated by the fleet API.</span></div>
      </div>
      <p className="login-copyright">© {new Date().getFullYear()} Fleet Telematics. Internal operations workspace.</p>
    </section>
  </div>;
}
