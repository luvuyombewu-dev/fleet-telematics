#!/usr/bin/env node
/**
 * Local functional smoke test for the Fleet Telematics secured API.
 * Requires Node.js 18+ and a running backend at localhost:8080.
 * Admin credentials are read from environment variables and never printed.
 */

const API_BASE = (process.env.FLEET_API_BASE_URL || 'http://localhost:8080/api/v1').replace(/\/+$/, '');
const username = process.env.FLEET_TEST_USERNAME;
const password = process.env.FLEET_TEST_PASSWORD;

if (!username || !password) {
  console.error('Set FLEET_TEST_USERNAME and FLEET_TEST_PASSWORD before running this script.');
  process.exit(2);
}

let adminToken = null;
let testVehicle = null;
let testDriver = null;
let testTrip = null;
let testMaintenance = null;
let testGeofence = null;
let testAlert = null;
let testUser = null;
let testUserPassword = null;
let seedVehicle = null;
let seedDriver = null;
let seedTrip = null;
const results = [];
const suffix = `${Date.now()}`;

function assert(condition, message) {
  if (!condition) throw new Error(message);
}

function expectStatus(result, expected, label) {
  assert(result.status === expected,
    `${label}: expected HTTP ${expected}, got ${result.status}${result.text ? ` — ${result.text.slice(0, 350)}` : ''}`);
}

async function request(path, { method = 'GET', body, token = adminToken } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal: AbortSignal.timeout(15000),
    });
  } catch (error) {
    throw new Error(`Cannot reach ${API_BASE}. Ensure Spring Boot is running. ${error.message}`);
  }

  const text = await response.text();
  let data = null;
  if (text) {
    try { data = JSON.parse(text); }
    catch { data = { message: text }; }
  }
  return { status: response.status, data, text };
}

async function run(name, fn) {
  try {
    await fn();
    results.push({ name, status: 'PASS' });
    console.log(`PASS  ${name}`);
  } catch (error) {
    results.push({ name, status: 'FAIL', error: error.message });
    console.error(`FAIL  ${name}\n      ${error.message}`);
  }
}

async function readArray(path, label = path) {
  const result = await request(path);
  expectStatus(result, 200, `GET ${label}`);
  assert(Array.isArray(result.data), `GET ${label}: response was not a JSON array`);
  return result.data;
}

async function cleanupDelete(path, label) {
  const result = await request(path, { method: 'DELETE' });
  if (result.status === 204 || result.status === 200) {
    console.log(`CLEAN ${label}`);
  } else if (result.status === 404) {
    console.log(`CLEAN ${label} (already absent)`);
  } else {
    console.warn(`WARN  Cleanup of ${label} returned HTTP ${result.status}; check the record manually.`);
  }
}

async function main() {
  console.log(`Fleet Telematics API functional tests: ${API_BASE}`);
  console.log('These tests use uniquely labelled QA records. See README before running.\n');

  await run('Admin login returns a JWT', async () => {
    const result = await request('/auth/login', {
      method: 'POST', body: { username, password }, token: null,
    });
    expectStatus(result, 200, 'POST /auth/login');
    assert(typeof result.data?.accessToken === 'string' && result.data.accessToken.length > 20,
      'Login response did not contain accessToken');
    adminToken = result.data.accessToken;
  });
  if (!adminToken) {
    console.error('\nCannot continue without an admin JWT. Check credentials and the backend.');
    process.exit(1);
  }

  await run('Authenticated profile includes ADMIN role', async () => {
    const result = await request('/auth/me');
    expectStatus(result, 200, 'GET /auth/me');
    assert(result.data?.username === username, 'Profile username did not match the login account');
    assert(Array.isArray(result.data?.roles) && result.data.roles.includes('ADMIN'),
      `Expected ADMIN role; received ${JSON.stringify(result.data?.roles)}`);
  });

  await run('Protected endpoint rejects a request without a token (401)', async () => {
    const result = await request('/vehicles', { token: null });
    expectStatus(result, 401, 'Unauthenticated GET /vehicles');
  });

  const collections = {};
  await run('All list endpoints return JSON arrays', async () => {
    const endpoints = [
      ['vehicles', '/vehicles'], ['drivers', '/drivers'], ['assignments', '/driver-assignments'],
      ['trips', '/trips'], ['alerts', '/alerts'], ['maintenance', '/maintenance-records'],
      ['geofences', '/geofences'], ['gps events', '/gps-events'], ['users', '/users'],
      ['audit logs', '/audit-logs'],
    ];
    for (const [label, path] of endpoints) collections[label] = await readArray(path, label);
    assert(collections.vehicles.length > 0, 'No existing vehicle fixtures found');
    assert(collections.drivers.length > 0, 'No existing driver fixtures found');
    assert(collections.trips.length > 0, 'No existing trip fixture found');
    seedVehicle = collections.vehicles[0];
    seedDriver = collections.drivers[0];
    seedTrip = collections.trips[0];
  });

  await run('Invalid vehicle request is rejected (400)', async () => {
    const result = await request('/vehicles', { method: 'POST', body: {} });
    expectStatus(result, 400, 'POST /vehicles with empty body');
  });

  await run('Vehicle create, read, update and list verification', async () => {
    const payload = {
      registrationNumber: `QA-${suffix}`, fleetNumber: `QA-FLEET-${suffix}`,
      vin: `QA-VIN-${suffix}`, make: 'QA Test', model: 'Before Update', vehicleYear: 2026,
      vehicleType: 'VAN', fuelType: 'DIESEL', mileage: 100, status: 'ACTIVE',
      deviceId: `QA-DEVICE-${suffix}`, trackingStatus: 'OFFLINE',
    };
    const created = await request('/vehicles', { method: 'POST', body: payload });
    expectStatus(created, 201, 'POST /vehicles');
    assert(created.data?.id != null, 'Created vehicle response has no id');
    testVehicle = created.data;

    const got = await request(`/vehicles/${testVehicle.id}`);
    expectStatus(got, 200, 'GET created vehicle');
    assert(got.data.registrationNumber === payload.registrationNumber, 'Created registration number did not persist');

    const updated = await request(`/vehicles/${testVehicle.id}`, {
      method: 'PUT', body: { ...payload, model: 'After Update', mileage: 101 },
    });
    expectStatus(updated, 200, 'PUT /vehicles/{id}');
    assert(updated.data.model === 'After Update', 'Vehicle model update did not persist in response');
    const reread = await request(`/vehicles/${testVehicle.id}`);
    expectStatus(reread, 200, 'GET vehicle after update');
    assert(reread.data.model === 'After Update', 'Vehicle update did not persist after reread');
    const list = await readArray('/vehicles', 'vehicles after create/update');
    assert(list.some((item) => item.id === testVehicle.id), 'Created vehicle is missing from the list');
  });

  await run('Driver create, read, update and list verification', async () => {
    const payload = {
      employeeNumber: `QA-EMP-${suffix}`, firstName: 'QA', lastName: 'Tester',
      email: `qa.driver.${suffix}@example.test`, phone: '+27000000000',
      licenceNumber: `QA-LIC-${suffix}`, licenceExpiryDate: '2030-12-31', status: 'ACTIVE',
    };
    const created = await request('/drivers', { method: 'POST', body: payload });
    expectStatus(created, 201, 'POST /drivers');
    assert(created.data?.id != null, 'Created driver response has no id');
    testDriver = created.data;

    const got = await request(`/drivers/${testDriver.id}`);
    expectStatus(got, 200, 'GET created driver');
    assert(got.data.employeeNumber === payload.employeeNumber, 'Created employee number did not persist');

    const updated = await request(`/drivers/${testDriver.id}`, {
      method: 'PUT', body: { ...payload, firstName: 'UpdatedQA', phone: '+27000000001' },
    });
    expectStatus(updated, 200, 'PUT /drivers/{id}');
    assert(updated.data.firstName === 'UpdatedQA', 'Driver first-name update did not persist');
    const reread = await request(`/drivers/${testDriver.id}`);
    expectStatus(reread, 200, 'GET driver after update');
    assert(reread.data.firstName === 'UpdatedQA', 'Driver update did not persist after reread');
    const list = await readArray('/drivers', 'drivers after create/update');
    assert(list.some((item) => item.id === testDriver.id), 'Created driver is missing from the list');
  });

  await run('Trip create, read, update and delete', async () => {
    assert(testVehicle?.id != null && testDriver?.id != null, 'Vehicle/driver setup failed; cannot test trip CRUD');
    const payload = {
      vehicleId: testVehicle.id, driverId: testDriver.id, startTime: new Date().toISOString(),
      startLocation: 'QA Start Point', destination: 'QA Destination', status: 'SCHEDULED',
      distanceKm: 10, averageSpeedKmh: 40, maximumSpeedKmh: 60, durationSeconds: 900,
    };
    const created = await request('/trips', { method: 'POST', body: payload });
    expectStatus(created, 201, 'POST /trips');
    testTrip = created.data;
    assert(testTrip?.id != null, 'Created trip response has no id');

    const got = await request(`/trips/${testTrip.id}`);
    expectStatus(got, 200, 'GET created trip');
    const updated = await request(`/trips/${testTrip.id}`, {
      method: 'PUT', body: { ...payload, status: 'COMPLETED', distanceKm: 12.5 },
    });
    expectStatus(updated, 200, 'PUT /trips/{id}');
    assert(String(updated.data.status).toUpperCase() === 'COMPLETED', 'Trip status update did not persist');
    const reread = await request(`/trips/${testTrip.id}`);
    expectStatus(reread, 200, 'GET trip after update');
    assert(String(reread.data.status).toUpperCase() === 'COMPLETED', 'Trip update did not persist after reread');

    const deleted = await request(`/trips/${testTrip.id}`, { method: 'DELETE' });
    expectStatus(deleted, 204, 'DELETE /trips/{id}');
    testTrip = null;
    const missing = await request(`/trips/${created.data.id}`);
    expectStatus(missing, 404, 'GET deleted trip');
  });

  await run('Maintenance create, read, update and delete', async () => {
    assert(testVehicle?.id != null, 'Vehicle setup failed; cannot test maintenance CRUD');
    const payload = {
      vehicleId: testVehicle.id, maintenanceType: 'QA Functional Service',
      description: 'Temporary QA record', scheduledDate: new Date().toISOString().slice(0, 10),
      serviceMileage: 101, cost: 500, status: 'SCHEDULED', notes: `QA-${suffix}`,
    };
    const created = await request('/maintenance-records', { method: 'POST', body: payload });
    expectStatus(created, 201, 'POST /maintenance-records');
    testMaintenance = created.data;
    assert(testMaintenance?.id != null, 'Created maintenance response has no id');

    const got = await request(`/maintenance-records/${testMaintenance.id}`);
    expectStatus(got, 200, 'GET created maintenance record');
    const updated = await request(`/maintenance-records/${testMaintenance.id}`, {
      method: 'PUT', body: { ...payload, maintenanceType: 'QA Updated Service', cost: 650, status: 'COMPLETED', completedDate: new Date().toISOString().slice(0, 10) },
    });
    expectStatus(updated, 200, 'PUT /maintenance-records/{id}');
    assert(updated.data.maintenanceType === 'QA Updated Service', 'Maintenance update did not persist');
    const reread = await request(`/maintenance-records/${testMaintenance.id}`);
    expectStatus(reread, 200, 'GET maintenance after update');
    assert(reread.data.maintenanceType === 'QA Updated Service', 'Maintenance update did not persist after reread');
    const deleted = await request(`/maintenance-records/${testMaintenance.id}`, { method: 'DELETE' });
    expectStatus(deleted, 204, 'DELETE /maintenance-records/{id}');
    testMaintenance = null;
    expectStatus(await request(`/maintenance-records/${created.data.id}`), 404, 'GET deleted maintenance record');
  });

  await run('Geofence create, read, update and delete', async () => {
    const payload = {
      name: `QA Geofence ${suffix}`, description: 'Temporary QA geofence',
      latitude: -33.9249, longitude: 18.4241, radiusMeters: 250, status: 'ACTIVE',
    };
    const created = await request('/geofences', { method: 'POST', body: payload });
    expectStatus(created, 201, 'POST /geofences');
    testGeofence = created.data;
    assert(testGeofence?.id != null, 'Created geofence response has no id');
    expectStatus(await request(`/geofences/${testGeofence.id}`), 200, 'GET created geofence');
    const updated = await request(`/geofences/${testGeofence.id}`, {
      method: 'PUT', body: { ...payload, description: 'Updated QA geofence', radiusMeters: 300 },
    });
    expectStatus(updated, 200, 'PUT /geofences/{id}');
    assert(Number(updated.data.radiusMeters) === 300, 'Geofence radius update did not persist');
    const reread = await request(`/geofences/${testGeofence.id}`);
    expectStatus(reread, 200, 'GET geofence after update');
    assert(Number(reread.data.radiusMeters) === 300, 'Geofence update did not persist after reread');
    const deleted = await request(`/geofences/${testGeofence.id}`, { method: 'DELETE' });
    expectStatus(deleted, 204, 'DELETE /geofences/{id}');
    testGeofence = null;
    expectStatus(await request(`/geofences/${created.data.id}`), 404, 'GET deleted geofence');
  });

  await run('Driver assignment create, filter and unassign', async () => {
    const vehicles = collections.vehicles || [];
    const drivers = collections.drivers || [];
    assert(vehicles.length && drivers.length, 'Seeded vehicles/drivers are required');
    const current = await readArray('/driver-assignments?status=ACTIVE', 'active assignments');
    const busyVehicleIds = new Set(current.map((item) => String(item.vehicleId)));
    const busyDriverIds = new Set(current.map((item) => String(item.driverId)));
    const freeVehicle = vehicles.find((item) => !busyVehicleIds.has(String(item.id)));
    const freeDriver = drivers.find((item) => !busyDriverIds.has(String(item.id)));
    if (!freeVehicle || !freeDriver) {
      console.log('SKIP  Assignment create/unassign: no currently unassigned seeded vehicle/driver pair is available.');
      results.push({ name: 'Driver assignment create, filter and unassign', status: 'SKIP' });
      return;
    }
    const created = await request('/driver-assignments', {
      method: 'POST', body: { vehicleId: freeVehicle.id, driverId: freeDriver.id },
    });
    expectStatus(created, 201, 'POST /driver-assignments');
    assert(created.data?.id != null, 'Created assignment response has no id');
    const queried = await readArray(`/driver-assignments?status=ACTIVE&vehicleId=${freeVehicle.id}&driverId=${freeDriver.id}`, 'filtered assignments');
    assert(queried.some((item) => item.id === created.data.id), 'Assignment missing from filtered query');
    const unassigned = await request(`/driver-assignments/${created.data.id}/unassign`, { method: 'POST' });
    expectStatus(unassigned, 200, 'POST /driver-assignments/{id}/unassign');
    assert(String(unassigned.data.status).toUpperCase() !== 'ACTIVE', 'Assignment remained active after unassign');
  });

  await run('Alert create, update, acknowledge and resolve', async () => {
    assert(seedVehicle?.id != null && seedDriver?.id != null, 'Seeded vehicle/driver fixtures unavailable');
    const payload = {
      vehicleId: seedVehicle.id, driverId: seedDriver.id, alertType: 'QA_FUNCTIONAL_TEST',
      severity: 'MEDIUM', description: `QA-${suffix} functional-test alert (will be resolved)`,
      latitude: -33.9249, longitude: 18.4241, status: 'OPEN',
    };
    const created = await request('/alerts', { method: 'POST', body: payload });
    expectStatus(created, 201, 'POST /alerts');
    testAlert = created.data;
    assert(testAlert?.id != null, 'Created alert response has no id');
    expectStatus(await request(`/alerts/${testAlert.id}`), 200, 'GET created alert');
    const updated = await request(`/alerts/${testAlert.id}`, {
      method: 'PUT', body: { ...payload, description: `QA-${suffix} updated then resolved`, severity: 'HIGH', status: 'OPEN' },
    });
    expectStatus(updated, 200, 'PUT /alerts/{id}');
    assert(updated.data.description.includes('updated'), 'Alert description update did not persist');
    const acknowledged = await request(`/alerts/${testAlert.id}/acknowledge`, { method: 'POST' });
    expectStatus(acknowledged, 200, 'POST /alerts/{id}/acknowledge');
    const resolved = await request(`/alerts/${testAlert.id}/resolve`, { method: 'POST' });
    expectStatus(resolved, 200, 'POST /alerts/{id}/resolve');
    assert(['RESOLVED', 'CLOSED'].includes(String(resolved.data.status).toUpperCase()),
      `Alert was not resolved; status was ${resolved.data.status}`);
  });

  await run('Telemetry ingestion, history and latest-record lookup', async () => {
    assert(seedVehicle?.id != null, 'Seeded vehicle fixture unavailable');
    const payload = {
      vehicleId: seedVehicle.id, latitude: -33.9249, longitude: 18.4241,
      speedKmh: 0, heading: 90, mileage: 1234.5, fuelLevelPercent: 65,
      engineTemperatureC: 82, batteryVoltage: 12.6, ignitionStatus: true,
      engineStatus: true, recordedAt: new Date().toISOString(),
    };
    const created = await request('/telemetry', { method: 'POST', body: payload });
    expectStatus(created, 201, 'POST /telemetry');
    assert(created.data?.id != null, 'Telemetry response has no id');
    const history = await readArray(`/telemetry/vehicle/${seedVehicle.id}`, 'telemetry history');
    assert(history.some((item) => item.id === created.data.id), 'Created telemetry record not present in history');
    const latest = await request(`/telemetry/vehicle/${seedVehicle.id}/latest`);
    expectStatus(latest, 200, 'GET latest telemetry');
    assert(latest.data.id === created.data.id, 'Latest telemetry is not the newly recorded test sample');
  });

  await run('GPS event ingestion and vehicle/driver filters', async () => {
    assert(seedVehicle?.id != null && seedDriver?.id != null, 'Seeded vehicle/driver fixtures unavailable');
    const payload = {
      vehicleId: seedVehicle.id, driverId: seedDriver.id, eventType: 'QA_FUNCTIONAL_TEST',
      latitude: -33.9249, longitude: 18.4241, metadata: { source: 'api-functional-test', qaRun: suffix },
      recordedAt: new Date().toISOString(),
    };
    const created = await request('/gps-events', { method: 'POST', body: payload });
    expectStatus(created, 201, 'POST /gps-events');
    assert(created.data?.id != null, 'GPS event response has no id');
    for (const path of ['/gps-events', `/gps-events/vehicle/${seedVehicle.id}`, `/gps-events/driver/${seedDriver.id}`]) {
      const rows = await readArray(path, path);
      assert(rows.some((item) => item.id === created.data.id), `Created GPS event missing from ${path}`);
    }
  });

  await run('Trip-point ingestion and trip history lookup', async () => {
    assert(seedTrip?.id != null, 'Seeded trip fixture unavailable');
    const created = await request('/trip-points', {
      method: 'POST', body: {
        tripId: seedTrip.id, latitude: -33.9249, longitude: 18.4241,
        speedKmh: 25, recordedAt: new Date().toISOString(),
      },
    });
    expectStatus(created, 201, 'POST /trip-points');
    assert(created.data?.id != null, 'Trip-point response has no id');
    const points = await readArray(`/trip-points/trip/${seedTrip.id}`, 'trip points by trip');
    assert(points.some((item) => item.id === created.data.id), 'Created trip point missing from trip history');
  });

  await run('User creation, role restrictions and activation status', async () => {
    const qaUsername = `qa_driver_${suffix}`;
    testUserPassword = `FleetQA_${suffix}_SafePass!`;
    const created = await request('/users', {
      method: 'POST', body: {
        username: qaUsername, email: `qa.user.${suffix}@example.test`,
        password: testUserPassword, role: 'DRIVER', status: 'ACTIVE',
      },
    });
    expectStatus(created, 201, 'POST /users');
    testUser = created.data;
    assert(testUser?.id != null, 'Created user response has no id');
    assert(!('password' in testUser) && !('passwordHash' in testUser), 'API exposed password fields in UserResponse');

    const userList = await readArray('/users', 'users after create');
    assert(userList.some((item) => item.id === testUser.id), 'Created user missing from user list');

    const driverLogin = await request('/auth/login', {
      method: 'POST', body: { username: qaUsername, password: testUserPassword }, token: null,
    });
    expectStatus(driverLogin, 200, 'Login as QA DRIVER');
    const driverToken = driverLogin.data?.accessToken;
    assert(typeof driverToken === 'string', 'QA driver login did not return a token');
    const me = await request('/auth/me', { token: driverToken });
    expectStatus(me, 200, 'GET /auth/me as QA DRIVER');
    assert(Array.isArray(me.data.roles) && me.data.roles.includes('DRIVER'), `Expected DRIVER role; got ${JSON.stringify(me.data.roles)}`);

    expectStatus(await request('/audit-logs', { token: driverToken }), 403, 'Driver GET /audit-logs');
    expectStatus(await request('/users', { token: driverToken }), 403, 'Driver GET /users');

    const inactive = await request(`/users/${testUser.id}/status`, { method: 'PATCH', body: { status: 'INACTIVE' } });
    expectStatus(inactive, 200, 'Deactivate QA user');
    assert(String(inactive.data.status).toUpperCase() === 'INACTIVE', 'User status did not change to INACTIVE');
    const inactiveLogin = await request('/auth/login', {
      method: 'POST', body: { username: qaUsername, password: testUserPassword }, token: null,
    });
    expectStatus(inactiveLogin, 401, 'Inactive user login is rejected');
  });

  await run('Unknown vehicle returns a structured 404', async () => {
    const result = await request('/vehicles/9223372036854775807');
    expectStatus(result, 404, 'GET missing vehicle');
    assert(result.data && typeof result.data === 'object', '404 response was not structured JSON');
  });

  await run('Driver delete returns 204 and subsequent lookup returns 404', async () => {
    assert(testDriver?.id != null, 'Temporary driver was not created');
    const id = testDriver.id;
    const deleted = await request(`/drivers/${id}`, { method: 'DELETE' });
    expectStatus(deleted, 204, 'DELETE /drivers/{id}');
    testDriver = null;
    expectStatus(await request(`/drivers/${id}`), 404, 'GET deleted driver');
  });

  await run('Vehicle delete returns 204 and subsequent lookup returns 404', async () => {
    assert(testVehicle?.id != null, 'Temporary vehicle was not created');
    const id = testVehicle.id;
    const deleted = await request(`/vehicles/${id}`, { method: 'DELETE' });
    expectStatus(deleted, 204, 'DELETE /vehicles/{id}');
    testVehicle = null;
    expectStatus(await request(`/vehicles/${id}`), 404, 'GET deleted vehicle');
  });

  console.log('\nRESULTS');
  const passed = results.filter((item) => item.status === 'PASS').length;
  const failed = results.filter((item) => item.status === 'FAIL').length;
  const skipped = results.filter((item) => item.status === 'SKIP').length;
  console.log(`PASS: ${passed} | FAIL: ${failed} | SKIP: ${skipped}`);
  if (failed) {
    console.log('\nFailing checks:');
    for (const item of results.filter((entry) => entry.status === 'FAIL')) console.log(`- ${item.name}: ${item.error}`);
  }
  console.log('\nExpected retained QA data: one resolved QA alert, one telemetry record, one GPS event, one trip point, one inactive QA driver account, and one inactive assignment if assignment testing ran.');
  console.log('Those resource types do not expose DELETE endpoints in the current API.');
  process.exitCode = failed ? 1 : 0;
}

main().catch((error) => {
  console.error(`Fatal test-runner error: ${error.message}`);
  process.exitCode = 1;
}).finally(async () => {
  // A final best-effort cleanup if earlier tests failed partway through.
  if (adminToken) {
    if (testMaintenance?.id != null) await cleanupDelete(`/maintenance-records/${testMaintenance.id}`, 'temporary maintenance record');
    if (testTrip?.id != null) await cleanupDelete(`/trips/${testTrip.id}`, 'temporary trip');
    if (testGeofence?.id != null) await cleanupDelete(`/geofences/${testGeofence.id}`, 'temporary geofence');
    if (testDriver?.id != null) await cleanupDelete(`/drivers/${testDriver.id}`, 'temporary driver');
    if (testVehicle?.id != null) await cleanupDelete(`/vehicles/${testVehicle.id}`, 'temporary vehicle');
    if (testUser?.id != null) {
      await request(`/users/${testUser.id}/status`, { method: 'PATCH', body: { status: 'INACTIVE' } })
        .then((result) => { if ([200, 204].includes(result.status)) console.log('CLEAN QA user left inactive'); })
        .catch(() => {});
    }
  }
});
