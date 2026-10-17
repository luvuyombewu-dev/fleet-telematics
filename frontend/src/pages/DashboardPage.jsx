import { useCallback, useEffect, useState } from 'react';
import { Activity, ArrowDownRight, ArrowRight, ArrowUpRight, BellRing, CircleAlert, Clock3, Gauge, RefreshCw, Truck, Users, Wrench } from 'lucide-react';
import { Link } from 'react-router-dom';
import { apiRequest } from '../lib/api.js';
import StatusPill from '../components/StatusPill.jsx';

const dateTime = (value) => value ? new Date(value).toLocaleString(undefined, { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) : '—';

function MetricCard({ label, value, detail, icon: Icon, tone = 'blue', loading }) {
  return <article className="metric-card"><div className={`metric-icon ${tone}`}><Icon size={20} /></div><div className="metric-copy"><span>{label}</span><strong>{loading ? '—' : value}</strong><small>{detail}</small></div><div className="metric-spark" aria-hidden="true"><span /><span /><span /><span /><span /><span /><span /></div></article>;
}

export default function DashboardPage() {
  const [data, setData] = useState({ vehicles: [], drivers: [], trips: [], alerts: [], maintenance: [], geofences: [] });
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [errors, setErrors] = useState([]);

  const load = useCallback(async (silent = false) => {
    if (silent) setRefreshing(true); else setLoading(true);
    const requests = [
      ['vehicles', '/vehicles'], ['drivers', '/drivers'], ['trips', '/trips'],
      ['alerts', '/alerts?status=OPEN'], ['maintenance', '/maintenance-records?status=SCHEDULED'], ['geofences', '/geofences?status=ACTIVE'],
    ];
    const results = await Promise.allSettled(requests.map(([, path]) => apiRequest(path)));
    const next = {};
    const failed = [];
    results.forEach((result, index) => {
      const [key, path] = requests[index];
      if (result.status === 'fulfilled') next[key] = Array.isArray(result.value) ? result.value : [];
      else { next[key] = []; failed.push(path); }
    });
    setData(next);
    setErrors(failed);
    setLoading(false);
    setRefreshing(false);
  }, []);

  useEffect(() => { load(); }, [load]);

  const activeVehicles = data.vehicles.filter((v) => String(v.status || '').toUpperCase() === 'ACTIVE').length;
  const activeDrivers = data.drivers.filter((d) => String(d.status || '').toUpperCase() === 'ACTIVE').length;
  const inProgress = data.trips.filter((t) => String(t.status || '').toUpperCase() === 'IN_PROGRESS').length;
  const recentTrips = [...data.trips].sort((a, b) => new Date(b.startTime || 0) - new Date(a.startTime || 0)).slice(0, 5);
  const recentAlerts = [...data.alerts].sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0)).slice(0, 5);

  return <div className="page-stack">
    <section className="page-heading dashboard-heading"><div><span className="eyebrow">FLEET OPERATIONS / OVERVIEW</span><h1>Good day, fleet team</h1><p>Here is the latest picture of your fleet activity and vehicle health.</p></div><button className="button button-secondary" onClick={() => load(true)} disabled={refreshing}><RefreshCw size={16} className={refreshing ? 'spin' : ''} />Refresh data</button></section>
    {errors.length > 0 && <div className="inline-notice"><CircleAlert size={17} /><div><strong>Some dashboard data could not be loaded.</strong><span>Check the API endpoint or your assigned role for: {errors.join(', ')}.</span></div></div>}
    <section className="metric-grid">
      <MetricCard label="Fleet vehicles" value={data.vehicles.length} detail={`${activeVehicles} active vehicles`} icon={Truck} tone="blue" loading={loading} />
      <MetricCard label="Drivers" value={data.drivers.length} detail={`${activeDrivers} active drivers`} icon={Users} tone="teal" loading={loading} />
      <MetricCard label="Trips in progress" value={inProgress} detail={`${data.trips.length} trips recorded`} icon={Gauge} tone="violet" loading={loading} />
      <MetricCard label="Open alerts" value={data.alerts.length} detail="Require review" icon={BellRing} tone="orange" loading={loading} />
    </section>
    <section className="dashboard-columns">
      <article className="panel recent-panel"><div className="panel-header"><div><span className="eyebrow">FLEET ACTIVITY</span><h2>Recent trips</h2></div><Link className="text-link" to="/trips">All trips <ArrowRight size={15} /></Link></div>
        {loading ? <div className="loading-rows"><span /><span /><span /><span /></div> : recentTrips.length === 0 ? <div className="empty-state compact"><div className="empty-icon"><Truck size={21} /></div><strong>No trips recorded yet</strong><span>Trips will appear here once they are registered.</span><Link to="/trips" className="text-link">Open trip management <ArrowRight size={14} /></Link></div> : <div className="table-wrap"><table><thead><tr><th>Trip</th><th>Vehicle</th><th>Destination</th><th>Start time</th><th>Status</th></tr></thead><tbody>{recentTrips.map((trip) => <tr key={trip.id}><td className="primary-cell">TR-{trip.id}</td><td>VH-{trip.vehicleId}</td><td>{trip.destination || trip.startLocation || 'Route not specified'}</td><td>{dateTime(trip.startTime)}</td><td><StatusPill value={trip.status} /></td></tr>)}</tbody></table></div>}
      </article>
      <article className="panel alerts-panel"><div className="panel-header"><div><span className="eyebrow">NEEDS ATTENTION</span><h2>Open alerts</h2></div><Link className="text-link" to="/alerts">View all <ArrowRight size={15} /></Link></div>
        {loading ? <div className="loading-rows"><span /><span /><span /></div> : recentAlerts.length === 0 ? <div className="empty-state compact"><div className="empty-icon positive"><Activity size={21} /></div><strong>All clear for now</strong><span>No open alerts were returned by the API.</span></div> : <div className="alert-list">{recentAlerts.map((alert) => <div className="alert-list-item" key={alert.id}><div className={`alert-priority ${String(alert.severity).toLowerCase()}`}><CircleAlert size={17} /></div><div className="alert-list-copy"><strong>{alert.alertType || 'Fleet alert'} <span>· VH-{alert.vehicleId}</span></strong><p>{alert.description || 'No description provided.'}</p><small><Clock3 size={12} />{dateTime(alert.createdAt)}</small></div><StatusPill value={alert.severity || 'MEDIUM'} /></div>)}</div>}
      </article>
    </section>
    <section className="quick-grid">
      <Link to="/maintenance" className="quick-card"><div className="quick-icon amber"><Wrench size={19} /></div><div><strong>Maintenance schedule</strong><span>{loading ? 'Loading…' : `${data.maintenance.length} scheduled records`} · Review servicing</span></div><ArrowUpRight size={17} /></Link>
      <Link to="/geofences" className="quick-card"><div className="quick-icon green"><MapPinIcon /></div><div><strong>Active geofences</strong><span>{loading ? 'Loading…' : `${data.geofences.length} operational areas`} · Manage boundaries</span></div><ArrowDownRight size={17} /></Link>
    </section>
  </div>;
}

function MapPinIcon() { return <span className="pin-glyph">◎</span>; }
