import { Activity, BellRing, ClipboardList, MapPin, Route, Settings2, Truck, Users, Wrench, FileClock, UserCog } from 'lucide-react';

const field = (key, label, type = 'text', extra = {}) => ({ key, label, type, ...extra });
const activeStatus = { type: 'select', options: ['ACTIVE', 'INACTIVE', 'SUSPENDED'], defaultValue: 'ACTIVE' };

export const resourceConfig = {
  vehicles: {
    title: 'Vehicles', eyebrow: 'Fleet management', description: 'Manage fleet assets, tracking devices and vehicle status.', endpoint: '/vehicles', editable: true, singular: 'Vehicle',
    icon: Truck, createRoles: ['ADMIN', 'FLEET_MANAGER', 'DISPATCHER'], canDelete: true,
    columns: [
      { key: 'registrationNumber', label: 'Registration', primary: true }, { key: 'fleetNumber', label: 'Fleet ID' },
      { key: 'make', label: 'Make' }, { key: 'model', label: 'Model' }, { key: 'vehicleType', label: 'Type' },
      { key: 'trackingStatus', label: 'Tracking', type: 'status' }, { key: 'status', label: 'Status', type: 'status' },
    ],
    fields: [
      field('registrationNumber', 'Registration number', 'text', { required: true }),
      field('vin', 'VIN / chassis number'), field('make', 'Make', 'text', { required: true }), field('model', 'Model', 'text', { required: true }),
      field('vehicleYear', 'Vehicle year', 'number', { min: 1900, max: 2100 }),
      field('vehicleType', 'Vehicle type', 'select', { required: true, options: ['TRUCK', 'VAN', 'CAR', 'BUS', 'TRAILER', 'OTHER'], defaultValue: 'TRUCK' }),
      field('fuelType', 'Fuel type', 'select', { required: true, options: ['DIESEL', 'PETROL', 'ELECTRIC', 'HYBRID', 'OTHER'], defaultValue: 'DIESEL' }),
      field('mileage', 'Mileage (km)', 'number', { min: 0, step: '0.1', defaultValue: '0' }),
      field('status', 'Vehicle status', 'select', activeStatus),
      field('trackingStatus', 'Tracking status', 'select', { options: ['ONLINE', 'OFFLINE'], defaultValue: 'OFFLINE' }),
    ],
  },
  drivers: {
    title: 'Drivers', eyebrow: 'People and assignments', description: 'Manage driver profiles, licence information and availability.', endpoint: '/drivers', editable: true, singular: 'Driver',
    icon: Users, createRoles: ['ADMIN', 'FLEET_MANAGER', 'DISPATCHER'], canDelete: true,
    columns: [
      { key: 'employeeNumber', label: 'Employee ID', primary: true }, { key: 'firstName', label: 'First name' }, { key: 'lastName', label: 'Last name' },
      { key: 'email', label: 'Email' }, { key: 'licenceNumber', label: 'Licence number' }, { key: 'status', label: 'Status', type: 'status' },
    ],
    fields: [
      field('employeeNumber', 'Employee number', 'text', { required: true }), field('firstName', 'First name', 'text', { required: true }),
      field('lastName', 'Last name', 'text', { required: true }), field('email', 'Email', 'email'), field('phone', 'Phone number', 'tel'),
      field('licenceNumber', 'Licence number'), field('licenceExpiryDate', 'Licence expiry date', 'date'),
      field('status', 'Driver status', 'select', activeStatus),
    ],
  },
  assignments: {
    title: 'Driver assignments', eyebrow: 'People and assignments', description: 'Review current driver-to-vehicle assignments and unassign vehicles when needed.', endpoint: '/driver-assignments', editable: true, singular: 'Driver assignment',
    icon: ClipboardList, createRoles: ['ADMIN', 'FLEET_MANAGER', 'DISPATCHER'],
    columns: [
      { key: 'id', label: 'Assignment ID', primary: true }, { key: 'driverId', label: 'Driver ID' }, { key: 'vehicleId', label: 'Vehicle ID' },
      { key: 'assignedAt', label: 'Assigned', type: 'datetime' }, { key: 'unassignedAt', label: 'Unassigned', type: 'datetime' }, { key: 'status', label: 'Status', type: 'status' },
    ],
    fields: [field('driverId', 'Driver', 'relation', { required: true, optionsEndpoint: '/drivers', labelFields: ['firstName', 'lastName', 'employeeNumber'] }), field('vehicleId', 'Vehicle', 'relation', { required: true, optionsEndpoint: '/vehicles', labelFields: ['registrationNumber', 'fleetNumber', 'make', 'model'] })],
    actions: [
      { label: 'Unassign', method: 'POST', path: row => `/driver-assignments/${row.id}/unassign`, roles: ['ADMIN', 'FLEET_MANAGER', 'DISPATCHER'], when: row => String(row.status || '').toUpperCase() === 'ACTIVE', variant: 'secondary' },
    ],
  },
  trips: {
    title: 'Trips', eyebrow: 'Operations', description: 'Review scheduled and completed trips, routes and recorded journey metrics.', endpoint: '/trips', editable: true, singular: 'Trip',
    icon: Route, createRoles: ['ADMIN', 'FLEET_MANAGER', 'DISPATCHER'], canDelete: true,
    columns: [
      { key: 'id', label: 'Trip ID', primary: true }, { key: 'vehicleId', label: 'Vehicle ID' }, { key: 'driverId', label: 'Driver ID' },
      { key: 'startLocation', label: 'From' }, { key: 'destination', label: 'Destination' }, { key: 'startTime', label: 'Start time', type: 'datetime' },
      { key: 'distanceKm', label: 'Distance (km)' }, { key: 'status', label: 'Status', type: 'status' },
    ],
    fields: [
      field('vehicleId', 'Vehicle', 'relation', { required: true, optionsEndpoint: '/vehicles', labelFields: ['registrationNumber', 'fleetNumber', 'make', 'model'] }), field('driverId', 'Driver (optional)', 'relation', { optionsEndpoint: '/drivers', labelFields: ['firstName', 'lastName', 'employeeNumber'], emptyLabel: 'No driver selected' }),
      field('startTime', 'Start time', 'datetime-local', { required: true }), field('endTime', 'End time', 'datetime-local'),
      field('startLocation', 'Start location'), field('destination', 'Destination'), field('status', 'Trip status', 'select', { options: ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'], defaultValue: 'SCHEDULED' }),
    ],
  },
  alerts: {
    title: 'Alerts', eyebrow: 'Monitoring', description: 'Review fleet alerts and acknowledge or resolve active incidents.', endpoint: '/alerts', editable: true, singular: 'Alert',
    icon: BellRing, createRoles: ['ADMIN', 'FLEET_MANAGER', 'DISPATCHER'],
    columns: [
      { key: 'id', label: 'Alert ID', primary: true }, { key: 'alertType', label: 'Type' }, { key: 'description', label: 'Description' },
      { key: 'severity', label: 'Severity', type: 'status' }, { key: 'vehicleId', label: 'Vehicle ID' }, { key: 'createdAt', label: 'Created', type: 'datetime' }, { key: 'status', label: 'Status', type: 'status' },
    ],
    fields: [
      field('vehicleId', 'Vehicle', 'relation', { required: true, optionsEndpoint: '/vehicles', labelFields: ['registrationNumber', 'fleetNumber', 'make', 'model'] }), field('driverId', 'Driver (optional)', 'relation', { optionsEndpoint: '/drivers', labelFields: ['firstName', 'lastName', 'employeeNumber'], emptyLabel: 'No driver selected' }),
      field('alertType', 'Alert type', 'text', { required: true, placeholder: 'e.g. SPEEDING' }),
      field('severity', 'Severity', 'select', { options: ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'], defaultValue: 'MEDIUM' }),
      field('description', 'Description', 'textarea', { required: true, full: true }), field('latitude', 'Latitude', 'number', { min: -90, max: 90, step: 'any' }),
      field('longitude', 'Longitude', 'number', { min: -180, max: 180, step: 'any' }),
    ],
    actions: [
      { label: 'Acknowledge', method: 'POST', path: row => `/alerts/${row.id}/acknowledge`, roles: ['ADMIN', 'FLEET_MANAGER', 'DISPATCHER'], when: row => String(row.status || '').toUpperCase() === 'OPEN', variant: 'secondary' },
      { label: 'Resolve', method: 'POST', path: row => `/alerts/${row.id}/resolve`, roles: ['ADMIN', 'FLEET_MANAGER', 'DISPATCHER'], when: row => !['RESOLVED', 'CLOSED'].includes(String(row.status || '').toUpperCase()), variant: 'primary' },
    ],
  },
  maintenance: {
    title: 'Maintenance', eyebrow: 'Asset health', description: 'Track scheduled services, completed work and maintenance costs.', endpoint: '/maintenance-records', editable: true, singular: 'Maintenance record',
    icon: Wrench, createRoles: ['ADMIN', 'FLEET_MANAGER', 'MAINTENANCE_MANAGER'], canDelete: true,
    columns: [
      { key: 'id', label: 'Record ID', primary: true }, { key: 'vehicleId', label: 'Vehicle ID' }, { key: 'maintenanceType', label: 'Service type' },
      { key: 'scheduledDate', label: 'Scheduled', type: 'date' }, { key: 'completedDate', label: 'Completed', type: 'date' },
      { key: 'cost', label: 'Cost (R)', type: 'currency' }, { key: 'status', label: 'Status', type: 'status' },
    ],
    fields: [
      field('vehicleId', 'Vehicle', 'relation', { required: true, optionsEndpoint: '/vehicles', labelFields: ['registrationNumber', 'fleetNumber', 'make', 'model'] }), field('maintenanceType', 'Maintenance type', 'text', { required: true }),
      field('description', 'Description', 'textarea', { full: true }), field('scheduledDate', 'Scheduled date', 'date'), field('completedDate', 'Completed date', 'date'),
      field('serviceMileage', 'Service mileage (km)', 'number', { min: 0, step: '0.1' }), field('cost', 'Cost (R)', 'number', { min: 0, step: '0.01' }),
      field('status', 'Status', 'select', { options: ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'], defaultValue: 'SCHEDULED' }), field('notes', 'Notes', 'textarea', { full: true }),
    ],
  },
  geofences: {
    title: 'Geofences', eyebrow: 'Location controls', description: 'Define operational areas using a centre coordinate and radius in metres.', endpoint: '/geofences', editable: true, singular: 'Geofence',
    icon: MapPin, createRoles: ['ADMIN', 'FLEET_MANAGER', 'DISPATCHER'], canDelete: true,
    columns: [
      { key: 'id', label: 'ID', primary: true }, { key: 'name', label: 'Geofence name' }, { key: 'latitude', label: 'Latitude' },
      { key: 'longitude', label: 'Longitude' }, { key: 'radiusMeters', label: 'Radius (m)' }, { key: 'status', label: 'Status', type: 'status' },
    ],
    fields: [
      field('name', 'Name', 'text', { required: true }), field('description', 'Description'),
      field('latitude', 'Latitude', 'number', { required: true, min: -90, max: 90, step: 'any' }),
      field('longitude', 'Longitude', 'number', { required: true, min: -180, max: 180, step: 'any' }),
      field('radiusMeters', 'Radius (metres)', 'number', { required: true, min: 0.01, step: 'any' }),
      field('status', 'Status', 'select', { options: ['ACTIVE', 'INACTIVE'], defaultValue: 'ACTIVE' }),
    ],
  },
  'gps-events': {
    title: 'GPS events', eyebrow: 'Monitoring', description: 'Inspect GPS event records reported by devices and fleet operations.', endpoint: '/gps-events', editable: true, singular: 'GPS event', canDelete: true,
    icon: Activity, createRoles: ['ADMIN', 'FLEET_MANAGER', 'DISPATCHER'],
    columns: [
      { key: 'id', label: 'Event ID', primary: true }, { key: 'eventType', label: 'Event type' }, { key: 'vehicleId', label: 'Vehicle ID' },
      { key: 'driverId', label: 'Driver ID' }, { key: 'latitude', label: 'Latitude' }, { key: 'longitude', label: 'Longitude' }, { key: 'recordedAt', label: 'Recorded', type: 'datetime' },
    ],
    fields: [
      field('vehicleId', 'Vehicle', 'relation', { required: true, optionsEndpoint: '/vehicles', labelFields: ['registrationNumber', 'fleetNumber', 'make', 'model'] }), field('driverId', 'Driver (optional)', 'relation', { optionsEndpoint: '/drivers', labelFields: ['firstName', 'lastName', 'employeeNumber'], emptyLabel: 'No driver selected' }),
      field('eventType', 'Event type', 'text', { required: true, placeholder: 'e.g. IGNITION_ON' }),
      field('latitude', 'Latitude', 'number', { required: true, min: -90, max: 90, step: 'any' }),
      field('longitude', 'Longitude', 'number', { required: true, min: -180, max: 180, step: 'any' }),
      field('recordedAt', 'Recorded at', 'datetime-local'), field('metadata', 'Metadata (JSON)', 'textarea', { full: true, placeholder: '{"source":"manual"}' }),
    ],
  },
  users: {
    title: 'User management', eyebrow: 'Administration', description: 'Create application users and change account status. Passwords are never shown.', endpoint: '/users',
    icon: UserCog, readRoles: ['ADMIN'], createRoles: ['ADMIN'],
    columns: [
      { key: 'id', label: 'User ID', primary: true }, { key: 'username', label: 'Username' }, { key: 'email', label: 'Email' },
      { key: 'role', label: 'Role', type: 'status' }, { key: 'status', label: 'Status', type: 'status' }, { key: 'createdAt', label: 'Created', type: 'datetime' },
    ],
    fields: [
      field('username', 'Username', 'text', { required: true }), field('email', 'Email', 'email', { required: true }),
      field('password', 'Temporary password (12+ characters)', 'password', { required: true, minLength: 12, full: true }),
      field('role', 'Role', 'select', { required: true, options: ['FLEET_MANAGER', 'DISPATCHER', 'MAINTENANCE_MANAGER', 'DRIVER', 'MANAGEMENT', 'ADMIN'], defaultValue: 'FLEET_MANAGER' }),
      field('status', 'Status', 'select', { options: ['ACTIVE', 'INACTIVE', 'SUSPENDED'], defaultValue: 'ACTIVE' }),
    ],
    actions: [
      { label: 'Deactivate', method: 'PATCH', path: row => `/users/${row.id}/status`, body: { status: 'INACTIVE' }, roles: ['ADMIN'], when: row => String(row.status || '').toUpperCase() === 'ACTIVE', variant: 'secondary' },
      { label: 'Activate', method: 'PATCH', path: row => `/users/${row.id}/status`, body: { status: 'ACTIVE' }, roles: ['ADMIN'], when: row => String(row.status || '').toUpperCase() !== 'ACTIVE', variant: 'primary' },
    ],
  },
  'audit-logs': {
    title: 'Audit logs', eyebrow: 'Administration', description: 'Review recorded user actions and affected entities.', endpoint: '/audit-logs',
    icon: FileClock, readRoles: ['ADMIN', 'MANAGEMENT'],
    columns: [
      { key: 'id', label: 'Log ID', primary: true }, { key: 'userId', label: 'User ID' }, { key: 'action', label: 'Action' },
      { key: 'entityType', label: 'Entity type' }, { key: 'entityId', label: 'Entity ID' }, { key: 'createdAt', label: 'Timestamp', type: 'datetime' },
    ],
  },
};

export const navigation = [
  { group: 'Overview', items: [{ label: 'Dashboard', path: '/', icon: Settings2 }] },
  { group: 'Fleet operations', items: [
    { label: 'Vehicles', path: '/vehicles', icon: Truck }, { label: 'Drivers', path: '/drivers', icon: Users },
    { label: 'Assignments', path: '/assignments', icon: ClipboardList }, { label: 'Trips', path: '/trips', icon: Route },
  ] },
  { group: 'Monitoring', items: [
    { label: 'Live tracking', path: '/tracking', icon: MapPin }, { label: 'GPS events', path: '/gps-events', icon: Activity },
    { label: 'Alerts', path: '/alerts', icon: BellRing }, { label: 'Maintenance', path: '/maintenance', icon: Wrench },
    { label: 'Geofences', path: '/geofences', icon: MapPin },
  ] },
  { group: 'Administration', items: [
    { label: 'Users', path: '/users', icon: UserCog, roles: ['ADMIN'] },
    { label: 'Audit logs', path: '/audit-logs', icon: FileClock, roles: ['ADMIN', 'MANAGEMENT'] },
  ] },
];
