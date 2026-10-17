import { useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Menu, Truck, LogOut, ShieldCheck, X, ChevronRight } from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { navigation, resourceConfig } from '../config/resources.js';

function pathTitle(pathname) {
  if (pathname === '/') return 'Operations overview';
  if (pathname === '/tracking') return 'Live fleet tracking';
  const key = pathname.replace(/^\//, '');
  return resourceConfig[key]?.title || 'Fleet operations';
}

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const roles = user?.roles || [];
  const canSee = (item) => !item.roles || item.roles.some((role) => roles.includes(role));
  const handleLogout = () => { logout(); navigate('/login', { replace: true }); };

  const sidebar = (
    <aside className="sidebar">
      <div className="brand-lockup">
        <div className="brand-mark"><Truck size={23} strokeWidth={2.1} /></div>
        <div><strong>Fleet<span>Ops</span></strong><small>TELEMATICS PLATFORM</small></div>
      </div>
      <div className="workspace-switcher"><div className="workspace-icon">F</div><div><strong>Fleet Operations</strong><span>South Africa · Main fleet</span></div><ChevronRight size={15} /></div>
      <nav className="side-nav" aria-label="Main navigation">
        {navigation.map((group) => {
          const items = group.items.filter(canSee);
          if (!items.length) return null;
          return <div className="nav-group" key={group.group}>
            <p className="nav-group-label">{group.group}</p>
            {items.map(({ label, path, icon: Icon }) => <NavLink key={path} to={path} end={path === '/'} onClick={() => setMobileOpen(false)} className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
              <Icon size={18} strokeWidth={1.85} /><span>{label}</span>
              {path === '/alerts' && <span className="nav-dot" aria-label="Alerts section" />}
            </NavLink>)}
          </div>;
        })}
      </nav>
      <div className="sidebar-bottom">
        <div className="secure-note"><ShieldCheck size={17} /><div><strong>Secure session</strong><span>Token-protected access</span></div></div>
        <div className="profile-row"><div className="avatar">{(user?.username || 'U').slice(0, 1).toUpperCase()}</div><div className="profile-meta"><strong>{user?.username || 'User'}</strong><span>{roles.includes('ADMIN') ? 'Administrator' : (roles.find((r) => r !== 'FACTOR_PASSWORD') || 'Fleet user').replaceAll('_', ' ')}</span></div><button className="icon-button logout-button" onClick={handleLogout} title="Sign out" aria-label="Sign out"><LogOut size={17} /></button></div>
      </div>
    </aside>
  );

  return <div className="app-shell">
    {mobileOpen && <button type="button" className="mobile-scrim" aria-label="Close navigation" onClick={() => setMobileOpen(false)} />}
    <div className={`sidebar-wrap${mobileOpen ? ' mobile-open' : ''}`}>{sidebar}</div>
    <div className="main-shell">
      <header className="topbar">
        <button className="icon-button mobile-menu-button" onClick={() => setMobileOpen(true)} aria-label="Open navigation"><Menu size={20} /></button>
        <div className="breadcrumb"><span>Workspace</span><ChevronRight size={14} /><strong>{pathTitle(location.pathname)}</strong></div>
        <div className="topbar-right"><span className="secure-chip"><span className="secure-indicator" /> Secure API session</span><div className="avatar avatar-small">{(user?.username || 'U').slice(0, 1).toUpperCase()}</div></div>
      </header>
      <main className="page-content"><Outlet /></main>
      <footer className="app-footer"><span>Fleet Telematics</span><span>Operational visibility · Protected by role-based access</span></footer>
    </div>
  </div>;
}
