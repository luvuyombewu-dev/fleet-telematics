-- ============================================================
-- Fleet Telematics Development Seed Data
-- ============================================================

-- ============================================================
-- VEHICLES
-- ============================================================

INSERT INTO vehicles (
    registration_number,
    fleet_number,
    vin,
    make,
    model,
    vehicle_year,
    vehicle_type,
    fuel_type,
    mileage,
    status,
    device_id,
    tracking_status
)
VALUES
(
    'CA 123-456',
    'FLT-001',
    'VIN-DEMO-000001',
    'Toyota',
    'Hilux',
    2024,
    'LIGHT_COMMERCIAL',
    'DIESEL',
    45210.50,
    'ACTIVE',
    'GPS-0001',
    'ONLINE'
),
(
    'CA 234-567',
    'FLT-002',
    'VIN-DEMO-000002',
    'Ford',
    'Ranger',
    2023,
    'LIGHT_COMMERCIAL',
    'DIESEL',
    68430.20,
    'ACTIVE',
    'GPS-0002',
    'ONLINE'
),
(
    'CA 345-678',
    'FLT-003',
    'VIN-DEMO-000003',
    'Mercedes-Benz',
    'Sprinter',
    2022,
    'DELIVERY_VAN',
    'DIESEL',
    91220.70,
    'MAINTENANCE',
    'GPS-0003',
    'OFFLINE'
);

-- ============================================================
-- DRIVERS
-- ============================================================

INSERT INTO drivers (
    employee_number,
    first_name,
    last_name,
    email,
    phone,
    licence_number,
    licence_expiry_date,
    status
)
VALUES
(
    'DRV-001',
    'Thabo',
    'Mokoena',
    'thabo.mokoena@example.com',
    '+27820000001',
    'LIC-000001',
    '2028-06-30',
    'ACTIVE'
),
(
    'DRV-002',
    'Liam',
    'Williams',
    'liam.williams@example.com',
    '+27820000002',
    'LIC-000002',
    '2027-11-30',
    'ACTIVE'
),
(
    'DRV-003',
    'Marcus',
    'Dlamini',
    'marcus.dlamini@example.com',
    '+27820000003',
    'LIC-000003',
    '2029-02-28',
    'ACTIVE'
);

-- ============================================================
-- DRIVER ASSIGNMENTS
-- ============================================================

INSERT INTO driver_assignments (
    driver_id,
    vehicle_id,
    status
)
VALUES
(
    (SELECT id FROM drivers WHERE employee_number = 'DRV-001'),
    (SELECT id FROM vehicles WHERE fleet_number = 'FLT-001'),
    'ACTIVE'
),
(
    (SELECT id FROM drivers WHERE employee_number = 'DRV-002'),
    (SELECT id FROM vehicles WHERE fleet_number = 'FLT-002'),
    'ACTIVE'
);

-- ============================================================
-- GEOFENCES
-- ============================================================

INSERT INTO geofences (
    name,
    description,
    latitude,
    longitude,
    radius_meters,
    status
)
VALUES
(
    'Cape Town Depot',
    'Main fleet depot',
    -33.924900,
    18.424100,
    500,
    'ACTIVE'
),
(
    'Bellville Operations Area',
    'Fleet operating zone',
    -33.894000,
    18.629000,
    1000,
    'ACTIVE'
);

-- ============================================================
-- TELEMETRY
-- ============================================================

INSERT INTO telemetry (
    vehicle_id,
    latitude,
    longitude,
    speed_kmh,
    heading,
    mileage,
    fuel_level_percent,
    engine_temperature_c,
    battery_voltage,
    ignition_status,
    engine_status,
    recorded_at
)
VALUES
(
    (SELECT id FROM vehicles WHERE fleet_number = 'FLT-001'),
    -33.924900,
    18.424100,
    62.50,
    90,
    45210.50,
    74.00,
    89.50,
    13.90,
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP
),
(
    (SELECT id FROM vehicles WHERE fleet_number = 'FLT-002'),
    -33.894000,
    18.629000,
    48.20,
    180,
    68430.20,
    58.00,
    87.20,
    13.70,
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP
);

-- ============================================================
-- GPS EVENTS
-- ============================================================

INSERT INTO gps_events (
    vehicle_id,
    driver_id,
    event_type,
    latitude,
    longitude,
    metadata,
    recorded_at
)
VALUES
(
    (SELECT id FROM vehicles WHERE fleet_number = 'FLT-001'),
    (SELECT id FROM drivers WHERE employee_number = 'DRV-001'),
    'VEHICLE_STARTED',
    -33.924900,
    18.424100,
    '{"source":"seed","description":"Vehicle started"}',
    CURRENT_TIMESTAMP
),
(
    (SELECT id FROM vehicles WHERE fleet_number = 'FLT-002'),
    (SELECT id FROM drivers WHERE employee_number = 'DRV-002'),
    'GEOFENCE_ENTRY',
    -33.894000,
    18.629000,
    '{"geofence":"Bellville Operations Area"}',
    CURRENT_TIMESTAMP
);

-- ============================================================
-- TRIPS
-- ============================================================

INSERT INTO trips (
    vehicle_id,
    driver_id,
    start_time,
    end_time,
    start_location,
    destination,
    distance_km,
    average_speed_kmh,
    maximum_speed_kmh,
    duration_seconds,
    status
)
VALUES
(
    (SELECT id FROM vehicles WHERE fleet_number = 'FLT-001'),
    (SELECT id FROM drivers WHERE employee_number = 'DRV-001'),
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '1 hour 30 minutes',
    'Cape Town Depot',
    'Bellville',
    24.50,
    51.30,
    82.00,
    1800,
    'COMPLETED'
);

-- ============================================================
-- TRIP POINT
-- ============================================================

INSERT INTO trip_points (
    trip_id,
    latitude,
    longitude,
    speed_kmh,
    recorded_at
)
SELECT
    id,
    -33.924900,
    18.424100,
    0,
    start_time
FROM trips
WHERE destination = 'Bellville'
LIMIT 1;

-- ============================================================
-- ALERTS
-- ============================================================

INSERT INTO alerts (
    vehicle_id,
    driver_id,
    alert_type,
    severity,
    description,
    latitude,
    longitude,
    status
)
VALUES
(
    (SELECT id FROM vehicles WHERE fleet_number = 'FLT-002'),
    (SELECT id FROM drivers WHERE employee_number = 'DRV-002'),
    'OVERSPEED',
    'MEDIUM',
    'Vehicle exceeded configured speed threshold.',
    -33.894000,
    18.629000,
    'OPEN'
),
(
    (SELECT id FROM vehicles WHERE fleet_number = 'FLT-003'),
    NULL,
    'TRACKING_OFFLINE',
    'HIGH',
    'Vehicle tracking device is currently offline.',
    NULL,
    NULL,
    'OPEN'
);

-- ============================================================
-- MAINTENANCE
-- ============================================================

INSERT INTO maintenance_records (
    vehicle_id,
    maintenance_type,
    description,
    scheduled_date,
    service_mileage,
    cost,
    status,
    notes
)
VALUES
(
    (SELECT id FROM vehicles WHERE fleet_number = 'FLT-001'),
    'Routine Service',
    'Standard vehicle service.',
    CURRENT_DATE + 14,
    50000,
    3500.00,
    'SCHEDULED',
    'Oil, filters and inspection.'
),
(
    (SELECT id FROM vehicles WHERE fleet_number = 'FLT-003'),
    'Brake Inspection',
    'Brake system inspection and replacement where required.',
    CURRENT_DATE,
    91220.70,
    6200.00,
    'DUE',
    'Vehicle currently unavailable.'
);