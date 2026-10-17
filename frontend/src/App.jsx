import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import AppLayout from './components/AppLayout.jsx';
import LoginPage from './pages/LoginPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import ResourcePage from './pages/ResourcePage.jsx';
import TrackingPage from './pages/TrackingPage.jsx';

const resourcePaths = ['vehicles', 'drivers', 'assignments', 'trips', 'alerts', 'maintenance', 'geofences', 'gps-events', 'users', 'audit-logs'];

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="tracking" element={<TrackingPage />} />
          {resourcePaths.map((path) => <Route key={path} path={path} element={<ResourcePage />} />)}
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
