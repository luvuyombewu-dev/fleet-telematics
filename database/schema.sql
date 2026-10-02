CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_user_role CHECK (
        role IN (
            'ADMIN',
            'FLEET_MANAGER',
            'DISPATCHER',
            'MAINTENANCE_MANAGER',
            'DRIVER',
            'MANAGEMENT'
        )
    ),

    CONSTRAINT chk_user_status CHECK (
        status IN (
            'ACTIVE',
            'INACTIVE',
            'SUSPENDED'
        )
    )
);

CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    registration_number VARCHAR(30) NOT NULL UNIQUE,
    fleet_number VARCHAR(30) UNIQUE,
    vin VARCHAR(50) UNIQUE,
    make VARCHAR(80) NOT NULL,
    model VARCHAR(80) NOT NULL,
    vehicle_year INTEGER,
    vehicle_type VARCHAR(50) NOT NULL,
    fuel_type VARCHAR(30) NOT NULL,
    mileage NUMERIC(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    device_id VARCHAR(100) UNIQUE,
    tracking_status VARCHAR(30) NOT NULL DEFAULT 'OFFLINE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_vehicle_year CHECK (
        vehicle_year IS NULL
        OR vehicle_year BETWEEN 1900 AND 2100
    ),

    CONSTRAINT chk_vehicle_mileage CHECK (mileage >= 0),

    CONSTRAINT chk_vehicle_status CHECK (
        status IN (
            'ACTIVE',
            'INACTIVE',
            'MAINTENANCE',
            'DECOMMISSIONED'
        )
    ),

    CONSTRAINT chk_tracking_status CHECK (
        tracking_status IN (
            'ONLINE',
            'OFFLINE',
            'NO_SIGNAL',
            'UNKNOWN'
        )
    )
);

CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    employee_number VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(30),
    licence_number VARCHAR(80) UNIQUE,
    licence_expiry_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_driver_status CHECK (
        status IN (
            'ACTIVE',
            'INACTIVE',
            'SUSPENDED'
        )
    )
);

CREATE TABLE driver_assignments (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unassigned_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_assignment_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_assignment_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_assignment_status CHECK (
        status IN ('ACTIVE', 'ENDED')
    ),

    CONSTRAINT chk_assignment_dates CHECK (
        unassigned_at IS NULL
        OR unassigned_at >= assigned_at
    )
);

CREATE TABLE telemetry (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    latitude NUMERIC(9,6) NOT NULL,
    longitude NUMERIC(9,6) NOT NULL,
    speed_kmh NUMERIC(8,2) NOT NULL DEFAULT 0,
    heading NUMERIC(6,2),
    mileage NUMERIC(12,2),
    fuel_level_percent NUMERIC(5,2),
    engine_temperature_c NUMERIC(6,2),
    battery_voltage NUMERIC(6,2),
    ignition_status BOOLEAN NOT NULL DEFAULT FALSE,
    engine_status BOOLEAN NOT NULL DEFAULT FALSE,
    recorded_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_telemetry_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_telemetry_latitude
        CHECK (latitude BETWEEN -90 AND 90),

    CONSTRAINT chk_telemetry_longitude
        CHECK (longitude BETWEEN -180 AND 180),

    CONSTRAINT chk_telemetry_speed
        CHECK (speed_kmh >= 0),

    CONSTRAINT chk_telemetry_heading
        CHECK (
            heading IS NULL
            OR heading BETWEEN 0 AND 360
        ),

    CONSTRAINT chk_telemetry_mileage
        CHECK (
            mileage IS NULL
            OR mileage >= 0
        ),

    CONSTRAINT chk_telemetry_fuel
        CHECK (
            fuel_level_percent IS NULL
            OR fuel_level_percent BETWEEN 0 AND 100
        )
);

CREATE TABLE gps_events (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    driver_id BIGINT,
    event_type VARCHAR(50) NOT NULL,
    latitude NUMERIC(9,6) NOT NULL,
    longitude NUMERIC(9,6) NOT NULL,
    metadata JSONB,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gps_event_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_gps_event_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_gps_event_latitude
        CHECK (latitude BETWEEN -90 AND 90),

    CONSTRAINT chk_gps_event_longitude
        CHECK (longitude BETWEEN -180 AND 180)
);

CREATE TABLE trips (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    driver_id BIGINT,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ,
    start_location VARCHAR(255),
    destination VARCHAR(255),
    distance_km NUMERIC(10,2),
    average_speed_kmh NUMERIC(8,2),
    maximum_speed_kmh NUMERIC(8,2),
    duration_seconds BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trip_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_trip_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_trip_status CHECK (
        status IN (
            'SCHEDULED',
            'ACTIVE',
            'COMPLETED',
            'CANCELLED'
        )
    ),

    CONSTRAINT chk_trip_times CHECK (
        end_time IS NULL
        OR end_time >= start_time
    ),

    CONSTRAINT chk_trip_distance CHECK (
        distance_km IS NULL
        OR distance_km >= 0
    )
);

CREATE TABLE trip_points (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    latitude NUMERIC(9,6) NOT NULL,
    longitude NUMERIC(9,6) NOT NULL,
    speed_kmh NUMERIC(8,2) NOT NULL DEFAULT 0,
    recorded_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_trip_point_trip
        FOREIGN KEY (trip_id)
        REFERENCES trips(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_trip_point_latitude
        CHECK (latitude BETWEEN -90 AND 90),

    CONSTRAINT chk_trip_point_longitude
        CHECK (longitude BETWEEN -180 AND 180),

    CONSTRAINT chk_trip_point_speed
        CHECK (speed_kmh >= 0)
);

CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    driver_id BIGINT,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    description TEXT NOT NULL,
    latitude NUMERIC(9,6),
    longitude NUMERIC(9,6),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ,

    CONSTRAINT fk_alert_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_alert_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_alert_severity CHECK (
        severity IN (
            'LOW',
            'MEDIUM',
            'HIGH',
            'CRITICAL'
        )
    ),

    CONSTRAINT chk_alert_status CHECK (
        status IN (
            'OPEN',
            'ACKNOWLEDGED',
            'RESOLVED'
        )
    )
);

CREATE TABLE maintenance_records (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    maintenance_type VARCHAR(100) NOT NULL,
    description TEXT,
    scheduled_date DATE,
    completed_date DATE,
    service_mileage NUMERIC(12,2),
    cost NUMERIC(12,2),
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_maintenance_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_maintenance_status CHECK (
        status IN (
            'SCHEDULED',
            'DUE',
            'IN_PROGRESS',
            'COMPLETED',
            'OVERDUE'
        )
    ),

    CONSTRAINT chk_maintenance_mileage CHECK (
        service_mileage IS NULL
        OR service_mileage >= 0
    ),

    CONSTRAINT chk_maintenance_cost CHECK (
        cost IS NULL
        OR cost >= 0
    )
);

CREATE TABLE geofences (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    latitude NUMERIC(9,6) NOT NULL,
    longitude NUMERIC(9,6) NOT NULL,
    radius_meters NUMERIC(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_geofence_latitude
        CHECK (latitude BETWEEN -90 AND 90),

    CONSTRAINT chk_geofence_longitude
        CHECK (longitude BETWEEN -180 AND 180),

    CONSTRAINT chk_geofence_radius
        CHECK (radius_meters > 0),

    CONSTRAINT chk_geofence_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_driver_assignments_driver
    ON driver_assignments(driver_id);

CREATE INDEX idx_driver_assignments_vehicle
    ON driver_assignments(vehicle_id);

CREATE INDEX idx_telemetry_vehicle_recorded
    ON telemetry(vehicle_id, recorded_at DESC);

CREATE INDEX idx_gps_events_vehicle_recorded
    ON gps_events(vehicle_id, recorded_at DESC);

CREATE INDEX idx_gps_events_type
    ON gps_events(event_type);

CREATE INDEX idx_trips_vehicle_start
    ON trips(vehicle_id, start_time DESC);

CREATE INDEX idx_trips_driver_start
    ON trips(driver_id, start_time DESC);

CREATE INDEX idx_trips_status
    ON trips(status);

CREATE INDEX idx_trip_points_trip_recorded
    ON trip_points(trip_id, recorded_at);

CREATE INDEX idx_alerts_vehicle_created
    ON alerts(vehicle_id, created_at DESC);

CREATE INDEX idx_alerts_status
    ON alerts(status);

CREATE INDEX idx_alerts_severity
    ON alerts(severity);

CREATE INDEX idx_maintenance_vehicle
    ON maintenance_records(vehicle_id);

CREATE INDEX idx_maintenance_status
    ON maintenance_records(status);

CREATE INDEX idx_maintenance_scheduled
    ON maintenance_records(scheduled_date);

CREATE INDEX idx_audit_logs_user
    ON audit_logs(user_id);

CREATE INDEX idx_audit_logs_created
    ON audit_logs(created_at DESC);

CREATE UNIQUE INDEX uq_active_driver_assignment
    ON driver_assignments(driver_id)
    WHERE status = 'ACTIVE';

CREATE UNIQUE INDEX uq_active_vehicle_assignment
    ON driver_assignments(vehicle_id)
    WHERE status = 'ACTIVE';

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_vehicles_updated_at
BEFORE UPDATE ON vehicles
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_drivers_updated_at
BEFORE UPDATE ON drivers
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_trips_updated_at
BEFORE UPDATE ON trips
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_maintenance_updated_at
BEFORE UPDATE ON maintenance_records
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_geofences_updated_at
BEFORE UPDATE ON geofences
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();