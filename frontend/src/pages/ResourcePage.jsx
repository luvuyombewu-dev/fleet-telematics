import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { AlertTriangle, Check, ChevronDown, CirclePlus, Download, LoaderCircle, Pencil, RefreshCw, Search, Trash2, X } from 'lucide-react';
import { resourceConfig } from '../config/resources.js';
import { apiRequest } from '../lib/api.js';
import { useAuth } from '../context/AuthContext.jsx';
import StatusPill from '../components/StatusPill.jsx';

const dateText = (value, includeTime = true) => {
  if (!value) return '—';
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return String(value);
  return parsed.toLocaleString(undefined, includeTime ? { year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' } : { year: 'numeric', month: 'short', day: '2-digit' });
};
const canAny = (roles, allowed = []) => !allowed || allowed.some((role) => roles.includes(role));
const relationLabel = (item, fields = []) => fields.map((key) => item?.[key]).filter((value) => value !== null && value !== undefined && String(value).trim() !== '').join(' · ') || `Record ${item?.id ?? ''}`;
const toInputDate = (value, type) => {
  if (!value) return '';
  if (type === 'date') return String(value).slice(0, 10);
  if (type === 'datetime-local') {
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) return String(value).slice(0, 16);
    const local = new Date(parsed.getTime() - parsed.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 16);
  }
  return value;
};

function ModalField({ field, value, onChange, options = [], optionsLoading = false }) {
  const common = {
    id: field.key,
    name: field.key,
    value: value ?? '',
    required: Boolean(field.required),
    onChange: (event) => onChange(field.key, event.target.value),
    placeholder: field.placeholder || `Enter ${field.label.toLowerCase()}`,
    min: field.min,
    max: field.max,
    step: field.step,
    minLength: field.minLength,
    autoComplete: field.type === 'password' ? 'new-password' : undefined,
  };
  if (field.type === 'select' || field.type === 'relation') {
    const values = field.type === 'relation' ? options : (field.options || []).map((option) => ({ value: option, label: option.replaceAll('_', ' ') }));
    return <div className={`modal-field${field.full ? ' field-full' : ''}`}>
      <label htmlFor={field.key}>{field.label}{field.required && <i> *</i>}</label>
      <div className="select-wrap">
        <select {...common} disabled={optionsLoading || (field.type === 'relation' && values.length === 0)}>
          {!field.required && <option value="">{field.emptyLabel || 'None selected'}</option>}
          {field.type === 'relation' && values.length === 0 && <option value="">{optionsLoading ? 'Loading options…' : 'No records available'}</option>}
          {values.map((option) => <option value={option.value} key={String(option.value)}>{option.label}</option>)}
        </select><ChevronDown size={16} />
      </div>
      {field.type === 'relation' && !optionsLoading && values.length === 0 && <small className="field-help">Create the required {field.label.toLowerCase().replace(/ id$/, '')} first.</small>}
    </div>;
  }
  if (field.type === 'textarea') return <div className={`modal-field${field.full ? ' field-full' : ''}`}><label htmlFor={field.key}>{field.label}{field.required && <i> *</i>}</label><textarea {...common} rows={3} /></div>;
  return <div className={`modal-field${field.full ? ' field-full' : ''}`}><label htmlFor={field.key}>{field.label}{field.required && <i> *</i>}</label><input {...common} type={field.type || 'text'} /></div>;
}

function RecordModal({ config, record, onClose, onSaved }) {
  const editing = Boolean(record);
  const fields = useMemo(() => (config.fields || []).filter((field) => !(editing && field.createOnly)), [config.fields, editing]);
  const initial = Object.fromEntries(fields.map((field) => {
    let value = record?.[field.key] ?? field.defaultValue ?? (field.type === 'select' ? field.options?.[0] || '' : '');
    if (field.type === 'relation') value = value === null || value === undefined ? '' : String(value);
    else if (field.type === 'date' || field.type === 'datetime-local') value = toInputDate(value, field.type);
    else if (field.key === 'metadata' && value && typeof value === 'object') value = JSON.stringify(value, null, 2);
    else if (value !== null && value !== undefined && typeof value !== 'object') value = String(value);
    return [field.key, value];
  }));
  const [values, setValues] = useState(initial);
  const [optionsByEndpoint, setOptionsByEndpoint] = useState({});
  const [optionsLoading, setOptionsLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const update = (key, value) => setValues((current) => ({ ...current, [key]: value }));

  useEffect(() => {
    let alive = true;
    const endpoints = [...new Set(fields.filter((field) => field.type === 'relation').map((field) => field.optionsEndpoint))];
    if (!endpoints.length) { setOptionsLoading(false); return () => { alive = false; }; }
    setOptionsLoading(true);
    Promise.all(endpoints.map(async (endpoint) => {
      const data = await apiRequest(endpoint);
      return [endpoint, Array.isArray(data) ? data : []];
    })).then((entries) => {
      if (alive) setOptionsByEndpoint(Object.fromEntries(entries));
    }).catch((err) => {
      if (alive) setError(`Could not load driver/vehicle choices. ${err.message || ''}`.trim());
    }).finally(() => { if (alive) setOptionsLoading(false); });
    return () => { alive = false; };
  }, [config, fields]);

  async function submit(event) {
    event.preventDefault();
    setError('');
    setSaving(true);
    const payload = {};
    for (const field of fields) {
      const raw = values[field.key];
      if (raw === '' || raw === null || raw === undefined) {
        if (field.required) { setSaving(false); setError(`${field.label} is required.`); return; }
        continue;
      }
      if (field.type === 'number' || field.type === 'relation') payload[field.key] = Number(raw);
      else if (field.type === 'datetime-local') payload[field.key] = new Date(raw).toISOString();
      else if (field.key === 'metadata' && typeof raw === 'string') {
        try { payload[field.key] = JSON.parse(raw); } catch { setSaving(false); setError('Metadata must be valid JSON.'); return; }
      } else if (typeof raw === 'string') payload[field.key] = raw.trim();
      else payload[field.key] = raw;
    }
    // Fleet numbers are generated by the backend from database identity and must never be supplied by the browser.
    delete payload.fleetNumber;
    try {
      const endpoint = editing ? `${config.endpoint}/${record.id}` : config.endpoint;
      await apiRequest(endpoint, { method: editing ? 'PUT' : 'POST', body: payload });
      onSaved(`${config.singular || config.title.replace(/s$/, '')} ${editing ? 'updated' : 'created'} successfully.`);
    } catch (err) {
      setError(err.message || `Could not ${editing ? 'update' : 'create'} record.`);
    } finally { setSaving(false); }
  }

  return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}>
    <section className="modal-card" role="dialog" aria-modal="true" aria-labelledby="record-modal-title">
      <div className="modal-header"><div><span className="eyebrow">{editing ? 'EDIT RECORD' : 'NEW RECORD'}</span><h2 id="record-modal-title">{editing ? 'Edit' : 'Add'} {(config.singular || config.title.replace(/s$/, '')).toLowerCase()}</h2><p>{editing ? 'Update the fields below and save your changes.' : 'Enter the record details below. Required fields are marked.'}</p>{config.key === 'vehicles' && <p className="field-help">Fleet number is assigned automatically after the vehicle is saved.</p>}</div><button className="icon-button" onClick={onClose} aria-label="Close dialog"><X size={19} /></button></div>
      {error && <div className="form-alert modal-alert" role="alert"><AlertTriangle size={16} />{error}</div>}
      <form onSubmit={submit}><div className="modal-fields">{fields.map((field) => <ModalField key={field.key} field={field} value={values[field.key] ?? ''} onChange={update} options={field.type === 'relation' ? (optionsByEndpoint[field.optionsEndpoint] || []).map((item) => ({ value: String(item.id), label: relationLabel(item, field.labelFields) })) : []} optionsLoading={optionsLoading} />)}</div>
        <div className="modal-footer"><button className="button button-secondary" type="button" onClick={onClose} disabled={saving}>Cancel</button><button className="button button-primary" type="submit" disabled={saving || optionsLoading}>{saving ? <LoaderCircle className="spin" size={16} /> : <Check size={16} />} {saving ? 'Saving…' : editing ? 'Save changes' : 'Create record'}</button></div>
      </form>
    </section>
  </div>;
}

export default function ResourcePage() {
  const location = useLocation();
  const navigate = useNavigate();
  const key = location.pathname.replace(/^\//, '');
  const config = resourceConfig[key];
  const { user } = useAuth();
  const roles = user?.roles || [];
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [modalRecord, setModalRecord] = useState(null);
  const [showCreate, setShowCreate] = useState(false);
  const [notice, setNotice] = useState('');
  const [actionBusy, setActionBusy] = useState(null);

  const load = useCallback(async (silent = false) => {
    if (!config) return;
    if (silent) setRefreshing(true); else setLoading(true);
    setError('');
    try {
      const data = await apiRequest(config.endpoint);
      setRows(Array.isArray(data) ? data : []);
    } catch (err) { setError(err.message || 'Could not load records.'); setRows([]); }
    finally { setLoading(false); setRefreshing(false); }
  }, [config]);

  useEffect(() => { load(); }, [load]);
  useEffect(() => { setSearch(''); setNotice(''); setError(''); setModalRecord(null); setShowCreate(false); }, [key]);

  const filtered = useMemo(() => {
    const query = search.trim().toLowerCase();
    if (!query) return rows;
    return rows.filter((row) => JSON.stringify(row).toLowerCase().includes(query));
  }, [rows, search]);

  if (!config) return <div className="empty-state"><strong>Page not found</strong><button className="button button-secondary" onClick={() => navigate('/')}>Return to dashboard</button></div>;
  if (config.readRoles && !canAny(roles, config.readRoles)) return <div className="page-stack"><section className="page-heading"><div><span className="eyebrow">RESTRICTED AREA</span><h1>{config.title}</h1></div></section><div className="access-denied"><div className="empty-icon"><AlertTriangle size={22} /></div><h2>Access restricted</h2><p>Your current role does not have permission to open this section.</p></div></div>;

  const canCreate = Boolean(config.fields?.length && canAny(roles, config.createRoles || []));
  const canEdit = Boolean(config.editable && canAny(roles, config.editRoles || config.createRoles || []));
  const canDelete = Boolean(config.canDelete && roles.includes('ADMIN'));

  async function runRowAction(row, action) {
    const actionKey = `${row.id}-${action.label}`;
    setActionBusy(actionKey); setError(''); setNotice('');
    try {
      await apiRequest(typeof action.path === 'function' ? action.path(row) : action.path, { method: action.method, body: action.body });
      setNotice(`${action.label} completed.`); await load(true);
    } catch (err) { setError(err.message || 'Action failed.'); }
    finally { setActionBusy(null); }
  }

  async function deleteRow(row) {
    if (!window.confirm(`Delete record ${row.id}? This cannot be undone.`)) return;
    setActionBusy(`delete-${row.id}`); setError(''); setNotice('');
    try { await apiRequest(`${config.endpoint}/${row.id}`, { method: 'DELETE' }); setNotice('Record deleted successfully.'); await load(true); }
    catch (err) { setError(err.message || 'Could not delete record.'); }
    finally { setActionBusy(null); }
  }

  const toCsv = () => {
    const escape = (value) => `"${String(value ?? '').replaceAll('"', '""')}"`;
    const csv = [config.columns.map((column) => escape(column.label)).join(','), ...filtered.map((row) => config.columns.map((column) => escape(row[column.key])).join(','))].join('\r\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
    const link = document.createElement('a'); link.href = url; link.download = `${key}.csv`; link.click(); URL.revokeObjectURL(url);
  };

  return <div className="page-stack">
    <section className="page-heading"><div><span className="eyebrow">{config.eyebrow}</span><h1>{config.title}</h1><p>{config.description}</p></div><div className="heading-actions"><button className="button button-secondary" onClick={toCsv} disabled={loading || rows.length === 0}><Download size={16} />Export CSV</button>{canCreate && <button className="button button-primary" onClick={() => { setModalRecord(null); setShowCreate(true); }}><CirclePlus size={17} />Add record</button>}</div></section>
    {notice && <div className="success-notice"><Check size={16} />{notice}<button className="notice-close" onClick={() => setNotice('')} aria-label="Dismiss notice"><X size={15} /></button></div>}
    {error && <div className="inline-notice error-notice"><AlertTriangle size={17} /><div><strong>Request could not be completed</strong><span>{error}</span></div><button className="notice-close" onClick={() => setError('')} aria-label="Dismiss error"><X size={15} /></button></div>}
    <section className="panel data-panel"><div className="data-toolbar"><div className="results-count"><strong>{loading ? '—' : filtered.length}</strong><span>records{search ? ' matching search' : ''}</span></div><div className="table-tools"><label className="table-search"><Search size={16} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder={`Search ${config.title.toLowerCase()}…`} aria-label={`Search ${config.title}`} /></label><button className="icon-button refresh-button" onClick={() => load(true)} disabled={refreshing} title="Refresh data" aria-label="Refresh data"><RefreshCw size={17} className={refreshing ? 'spin' : ''} /></button></div></div>
      {loading ? <div className="table-loading"><LoaderCircle className="spin" size={23} /><span>Loading {config.title.toLowerCase()}…</span></div> : error && rows.length === 0 ? <div className="empty-state"><div className="empty-icon"><AlertTriangle size={21} /></div><strong>Could not load data</strong><span>Check your API connection and access permissions.</span><button className="button button-secondary" onClick={() => load()}>Try again</button></div> : filtered.length === 0 ? <div className="empty-state"><div className="empty-icon"><config.icon size={22} /></div><strong>{rows.length === 0 ? `No ${config.title.toLowerCase()} found` : 'No matching records'}</strong><span>{rows.length === 0 ? 'Records will appear here when available from the API.' : 'Try a different search phrase.'}</span>{canCreate && rows.length === 0 && <button className="button button-primary" onClick={() => { setModalRecord(null); setShowCreate(true); }}><CirclePlus size={16} />Add first record</button>}</div> : <div className="table-wrap"><table className="resource-table"><thead><tr>{config.columns.map((column) => <th key={column.key}>{column.label}</th>)}{(config.actions?.length || canDelete || canEdit) ? <th className="actions-heading">Actions</th> : null}</tr></thead><tbody>{filtered.map((row, index) => {
        const actions = (config.actions || []).filter((action) => canAny(roles, action.roles || []) && (!action.when || action.when(row)));
        return <tr key={row.id ?? index}>{config.columns.map((column) => {
          const value = row[column.key];
          let cell;
          if (column.type === 'status') cell = <StatusPill value={value} />;
          else if (column.type === 'datetime') cell = <span className="date-cell">{dateText(value)}</span>;
          else if (column.type === 'date') cell = <span className="date-cell">{dateText(value, false)}</span>;
          else if (column.type === 'currency' && value !== null && value !== undefined) cell = new Intl.NumberFormat('en-ZA', { style: 'currency', currency: 'ZAR', maximumFractionDigits: 2 }).format(Number(value));
          else if (column.primary) cell = <span className="primary-cell">{value ?? '—'}</span>;
          else if (value && typeof value === 'object') cell = <span className="json-cell">{JSON.stringify(value)}</span>;
          else cell = value === null || value === undefined || value === '' ? <span className="muted-dash">—</span> : String(value);
          return <td key={column.key}>{cell}</td>;
        })}{(config.actions?.length || canDelete || canEdit) ? <td><div className="row-actions">{canEdit && <button className="row-action secondary" onClick={() => { setModalRecord(row); setShowCreate(false); }} disabled={Boolean(actionBusy)} title="Edit record"><Pencil size={13} /><span>Edit</span></button>}{actions.map((action) => { const actionKey = `${row.id}-${action.label}`; return <button key={action.label} className={`row-action ${action.variant || 'secondary'}`} onClick={() => runRowAction(row, action)} disabled={Boolean(actionBusy)}>{actionBusy === actionKey ? <LoaderCircle size={13} className="spin" /> : action.label}</button>; })}{canDelete && <button className="row-action danger" onClick={() => deleteRow(row)} disabled={Boolean(actionBusy)} title="Delete record"><Trash2 size={14} /><span>Delete</span></button>}</div></td> : null}</tr>;
      })}</tbody></table></div>}
      <div className="table-footer"><span>{loading ? 'Retrieving records…' : `Showing ${filtered.length} of ${rows.length} records`}</span><span>Source: secured REST API</span></div>
    </section>
    {(showCreate || modalRecord) && <RecordModal key={`${key}-${modalRecord?.id ?? 'new'}`} config={{ ...config, key }} record={modalRecord} onClose={() => { setShowCreate(false); setModalRecord(null); }} onSaved={(message) => { setShowCreate(false); setModalRecord(null); setNotice(message); load(true); }} />}
  </div>;
}
