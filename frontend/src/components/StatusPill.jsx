export default function StatusPill({ value }) {
  if (value === null || value === undefined || value === '') return <span className="muted-dash">—</span>;
  const label = String(value).replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
  const normalized = String(value).toUpperCase();
  let tone = 'neutral';
  if (['ACTIVE', 'ONLINE', 'COMPLETED', 'RESOLVED', 'ACKNOWLEDGED'].includes(normalized)) tone = 'success';
  if (['OPEN', 'CRITICAL', 'HIGH', 'OVERDUE', 'SUSPENDED'].includes(normalized)) tone = 'danger';
  if (['MEDIUM', 'IN_PROGRESS', 'SCHEDULED', 'MAINTENANCE'].includes(normalized)) tone = 'warning';
  if (['OFFLINE', 'INACTIVE', 'CANCELLED', 'CLOSED'].includes(normalized)) tone = 'neutral';
  return <span className={`status-pill ${tone}`}><span className="status-dot" />{label}</span>;
}
