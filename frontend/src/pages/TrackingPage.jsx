import { useCallback, useEffect, useState } from 'react';
import { Activity, BatteryCharging, Clock3, Fuel, Gauge, LoaderCircle, MapPin, Navigation, RefreshCw, Truck, Wifi, WifiOff } from 'lucide-react';
import { apiRequest } from '../lib/api.js';
import StatusPill from '../components/StatusPill.jsx';

const dateText = (value) => value ? new Date(value).toLocaleString() : '—';

function TelemetryMetric({ label, value, unit, icon: Icon }) {
  return <div className="telemetry-metric"><div className="telemetry-metric-icon"><Icon size={17} /></div><span>{label}</span><strong>{value === null || value === undefined || value === '' ? '—' : value}{value !== null && value !== undefined && value !== '' && unit ? <small>{unit}</small> : null}</strong></div>;
}

export default function TrackingPage() {
  const [vehicles, setVehicles] = useState([]);
  const [selectedId, setSelectedId] = useState('');
  const [latest, setLatest] = useState(null);
  const [history, setHistory] = useState([]);
  const [loadingVehicles, setLoadingVehicles] = useState(true);
  const [loadingTelemetry, setLoadingTelemetry] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    apiRequest('/vehicles').then((list) => {
      const safe = Array.isArray(list) ? list : [];
      setVehicles(safe);
      if (safe.length) setSelectedId(String(safe[0].id));
    }).catch((err) => setError(err.message)).finally(() => setLoadingVehicles(false));
  }, []);

  const loadTelemetry = useCallback(async () => {
    if (!selectedId) { setLatest(null); setHistory([]); return; }
    setLoadingTelemetry(true); setError('');
    const results = await Promise.allSettled([
      apiRequest(`/telemetry/vehicle/${selectedId}/latest`),
      apiRequest(`/telemetry/vehicle/${selectedId}`),
    ]);
    if (results[0].status === 'fulfilled') setLatest(results[0].value);
    else setLatest(null);
    if (results[1].status === 'fulfilled') setHistory(Array.isArray(results[1].value) ? results[1].value : []);
    else setHistory([]);
    if (results[0].status === 'rejected' && results[1].status === 'rejected') setError('No telemetry is available for this vehicle yet, or the telemetry endpoints are unavailable.');
    setLoadingTelemetry(false);
  }, [selectedId]);
  useEffect(() => { if (selectedId) loadTelemetry(); }, [selectedId, loadTelemetry]);

  const vehicle = vehicles.find((item) => String(item.id) === selectedId);
  const lat = Number(latest?.latitude);
  const lon = Number(latest?.longitude);
  const validCoordinates = latest && Number.isFinite(lat) && Number.isFinite(lon) && Math.abs(lat) <= 90 && Math.abs(lon) <= 180;
  const bbox = validCoordinates ? `${(lon - 0.025).toFixed(5)}%2C${(lat - 0.018).toFixed(5)}%2C${(lon + 0.025).toFixed(5)}%2C${(lat + 0.018).toFixed(5)}` : '';
  const mapUrl = validCoordinates ? `https://www.openstreetmap.org/export/embed.html?bbox=${bbox}&layer=mapnik&marker=${lat}%2C${lon}` : '';
  const openMapUrl = validCoordinates ? `https://www.openstreetmap.org/?mlat=${lat}&mlon=${lon}#map=14/${lat}/${lon}` : '';

  return <div className="page-stack">
    <section className="page-heading"><div><span className="eyebrow">MONITORING / TELEMETRY</span><h1>Live fleet tracking</h1><p>Inspect the latest recorded position and telemetry history for a selected vehicle.</p></div><button className="button button-secondary" onClick={loadTelemetry} disabled={!selectedId || loadingTelemetry}><RefreshCw size={16} className={loadingTelemetry ? 'spin' : ''} />Refresh telemetry</button></section>
    <section className="panel tracking-selector"><div className="tracking-selector-label"><div className="quick-icon blue"><Truck size={19} /></div><div><strong>Selected vehicle</strong><span>Choose a vehicle to inspect its latest reading</span></div></div><div className="select-wrap vehicle-select"><select value={selectedId} disabled={loadingVehicles || !vehicles.length} onChange={(event) => setSelectedId(event.target.value)} aria-label="Select vehicle">{loadingVehicles && <option value="">Loading vehicles…</option>}{!loadingVehicles && vehicles.length === 0 && <option value="">No vehicles available</option>}{vehicles.map((item) => <option value={String(item.id)} key={item.id}>{item.registrationNumber || `Vehicle ${item.id}`} · {item.make || ''} {item.model || ''}</option>)}</select></div></section>
    {error && <div className="inline-notice"><Activity size={17} /><div><strong>Telemetry unavailable</strong><span>{error}</span></div></div>}
    <section className="tracking-grid">
      <article className="panel map-panel"><div className="panel-header"><div><span className="eyebrow">LAST KNOWN POSITION</span><h2>{vehicle?.registrationNumber || (selectedId ? `Vehicle ${selectedId}` : 'Vehicle position')}</h2></div>{latest && <StatusPill value={latest.ignitionStatus ? 'ONLINE' : 'OFFLINE'} />}</div>
        {loadingTelemetry ? <div className="map-placeholder"><LoaderCircle size={25} className="spin" /><span>Loading position…</span></div> : validCoordinates ? <><iframe className="fleet-map" title="OpenStreetMap vehicle location" src={mapUrl} loading="lazy" referrerPolicy="no-referrer" /><div className="map-coordinates"><div><MapPin size={16} /><span><strong>{lat.toFixed(5)}, {lon.toFixed(5)}</strong><small>{dateText(latest.recordedAt)}</small></span></div><a href={openMapUrl} target="_blank" rel="noreferrer">Open full map <Navigation size={14} /></a></div></> : <div className="map-placeholder"><div className="map-placeholder-icon"><MapPin size={26} /></div><strong>No recorded coordinates</strong><span>Position will display when a telemetry record is available for this vehicle.</span></div>}
      </article>
      <article className="panel telemetry-panel"><div className="panel-header"><div><span className="eyebrow">LATEST READING</span><h2>Vehicle telemetry</h2></div><div className="last-update"><Clock3 size={14} />{latest ? dateText(latest.recordedAt) : 'No reading'}</div></div>
        {loadingTelemetry ? <div className="table-loading"><LoaderCircle className="spin" size={22} /><span>Retrieving telemetry…</span></div> : <div className="telemetry-grid">
          <TelemetryMetric label="Speed" value={latest?.speedKmh} unit="km/h" icon={Gauge} />
          <TelemetryMetric label="Fuel level" value={latest?.fuelLevelPercent} unit="%" icon={Fuel} />
          <TelemetryMetric label="Battery" value={latest?.batteryVoltage} unit="V" icon={BatteryCharging} />
          <TelemetryMetric label="Engine temp." value={latest?.engineTemperatureC} unit="°C" icon={Activity} />
          <div className="telemetry-status-row"><span><Wifi size={16} />Ignition</span><StatusPill value={latest?.ignitionStatus ? 'ON' : latest ? 'OFF' : null} /></div>
          <div className="telemetry-status-row"><span><WifiOff size={16} />Engine</span><StatusPill value={latest?.engineStatus ? 'RUNNING' : latest ? 'STOPPED' : null} /></div>
        </div>}
      </article>
    </section>
    <section className="panel"><div className="panel-header"><div><span className="eyebrow">RECENT SAMPLES</span><h2>Telemetry history</h2></div><span className="table-subtle-count">{history.length} records</span></div>
      {loadingTelemetry ? <div className="table-loading"><LoaderCircle className="spin" size={22} /><span>Loading readings…</span></div> : history.length === 0 ? <div className="empty-state compact"><div className="empty-icon"><Activity size={21} /></div><strong>No telemetry history</strong><span>Telemetry records will appear here after devices or API clients submit readings.</span></div> : <div className="table-wrap"><table><thead><tr><th>Recorded at</th><th>Latitude</th><th>Longitude</th><th>Speed</th><th>Mileage</th><th>Fuel</th><th>Ignition</th></tr></thead><tbody>{history.slice(0, 20).map((item) => <tr key={item.id}><td className="date-cell">{dateText(item.recordedAt)}</td><td>{item.latitude}</td><td>{item.longitude}</td><td>{item.speedKmh ?? '—'} km/h</td><td>{item.mileage ?? '—'} km</td><td>{item.fuelLevelPercent == null ? '—' : `${item.fuelLevelPercent}%`}</td><td><StatusPill value={item.ignitionStatus ? 'ON' : 'OFF'} /></td></tr>)}</tbody></table></div>}
    </section>
    <p className="data-note">Tracking displays the latest records received by the API. It does not simulate GPS data or imply continuous real-time device streaming.</p>
  </div>;
}
