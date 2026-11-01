import { useEffect, useRef, useState } from 'react';
import { Activity, LoaderCircle } from 'lucide-react';
import { apiRequest } from '../lib/api.js';

const ROUTE = [
  [-26.2041, 28.0473, 28, 20],
  [-26.2032, 28.0501, 36, 65],
  [-26.2014, 28.0520, 42, 105],
  [-26.1991, 28.0508, 31, 165],
  [-26.1980, 28.0479, 24, 220],
  [-26.2002, 28.0451, 38, 260],
  [-26.2030, 28.0447, 33, 310],
];

export default function GpsSimulator({ vehicleId, onSample }) {
  const [running, setRunning] = useState(false);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');
  const index = useRef(0);
  const inFlight = useRef(false);

  useEffect(() => {
    if (!running || !vehicleId) return undefined;
    let active = true;

    const send = async () => {
      if (!active || inFlight.current) return;
      inFlight.current = true;
      setBusy(true);
      const [latitude, longitude, speedKmh, heading] = ROUTE[index.current % ROUTE.length];
      try {
        const saved = await apiRequest('/telemetry', {
          method: 'POST',
          body: {
            vehicleId: Number(vehicleId),
            latitude,
            longitude,
            speedKmh,
            heading,
            ignitionStatus: true,
            engineStatus: true,
            recordedAt: new Date().toISOString(),
          },
        });
        if (!active) return;
        index.current += 1;
        onSample?.(saved);
        setMessage('Synthetic Johannesburg coordinates saved. This is test data, not a physical GPS fix.');
      } catch (error) {
        if (active) {
          setMessage(`Simulator stopped: ${error.message || 'Telemetry request failed.'}`);
          setRunning(false);
        }
      } finally {
        inFlight.current = false;
        if (active) setBusy(false);
      }
    };

    void send();
    const timer = window.setInterval(send, 5000);
    return () => {
      active = false;
      window.clearInterval(timer);
    };
  }, [running, vehicleId, onSample]);

  if (!import.meta.env.DEV) return null;

  return <section className="panel tracking-selector">
    <div className="tracking-selector-label">
      <div className="quick-icon blue"><Activity size={19} /></div>
      <div>
        <strong>Development GPS simulator</strong>
        <span>{running ? 'Running · synthetic data only' : 'Test telemetry ingestion without tracker hardware'}</span>
        {message && <small className="field-help">{message}</small>}
      </div>
    </div>
    <button className={running ? 'button button-secondary' : 'button button-primary'}
      disabled={!vehicleId || busy}
      onClick={() => {
        if (running) {
          setRunning(false);
          setMessage('Development GPS simulator stopped.');
        } else {
          index.current = 0;
          setMessage('Starting simulator…');
          setRunning(true);
        }
      }}>
      {busy ? <LoaderCircle size={16} className="spin" /> : null}
      {running ? 'Stop simulator' : 'Start simulator'}
    </button>
  </section>;
}
