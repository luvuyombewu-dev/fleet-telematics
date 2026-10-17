FLEET TELEMATICS - FRONTEND FOUNDATION BATCH
============================================

What this batch adds
--------------------
- Vite + React single-page application.
- Login wired to POST /api/v1/auth/login.
- Session restore by checking GET /api/v1/auth/me.
- JWT bearer access for protected API calls; logout and expired-token handling.
- Responsive fleet operations dashboard with vehicle, driver, trip, open-alert, maintenance and geofence summaries.
- Resource pages connected to the existing vehicles, drivers, assignments, trips, alerts, maintenance, geofence, GPS-event, user and audit-log APIs.
- Table search, CSV export, refresh, create forms for supported resource types, role-aware controls, alert actions, assignment unassign and user status actions.
- Vehicle telemetry history and a map view of the latest recorded coordinates using OpenStreetMap.

Install this batch
------------------
1. Extract the ZIP into the fleet-telematics project root, preserving the frontend/ folder path.
2. Keep the backend terminal running with BOOTSTRAP_ENABLED=false.
3. Open a second Git Bash terminal and run:

   cd ~/Desktop/fleet-telematics/frontend
   cp .env.example .env
   npm install
   npm run dev

4. Open http://localhost:5173 in your browser.
5. Sign in with your existing fleetadmin account.

API configuration
-----------------
frontend/.env sets VITE_API_BASE_URL=http://localhost:8080/api/v1.
The backend currently permits http://localhost:5173 by default through its
APP_CORS_ALLOWED_ORIGINS setting. If you change the frontend port/origin, update
APP_CORS_ALLOWED_ORIGINS in backend/.env and restart Spring Boot.

Build verification
------------------
From the frontend folder:

   npm run build

After a successful build, commit the frontend from project root:

   cd ~/Desktop/fleet-telematics
   git add frontend README-FRONTEND-BATCH.txt
   git commit -m "Add React fleet operations frontend"
   git push origin main

Security notes
--------------
- The frontend uses sessionStorage for the bearer token because the current API
  returns a JWT to the single-page client. It clears the session on logout or a
  401 response. JavaScript-readable tokens are exposed to successful XSS; keep
  dependencies updated, never render untrusted HTML, and add a Content Security
  Policy when deploying. For stronger production session isolation, consider a
  backend-for-frontend with Secure, HttpOnly, SameSite cookies.
- Frontend role checks are for usability only. Backend authorization remains the
  authority and must be retained.
- The tracking map loads tiles from OpenStreetMap and displays recorded telemetry;
  it does not simulate live GPS streaming.
- Create forms are supplied where the current API exposes POST endpoints. The
  batch does not claim edit workflows for records without an implemented edit UI.
