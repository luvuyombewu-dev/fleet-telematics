#!/usr/bin/env bash
set -euo pipefail

# JPA entities may legally expose a public no-argument constructor. Services
# instantiate new entities before passing them to Spring Data repositories.
files=(
  "backend/src/main/java/za/co/fleettelematics/entity/Vehicle.java"
  "backend/src/main/java/za/co/fleettelematics/entity/Driver.java"
  "backend/src/main/java/za/co/fleettelematics/entity/DriverAssignment.java"
  "backend/src/main/java/za/co/fleettelematics/entity/Telemetry.java"
  "backend/src/main/java/za/co/fleettelematics/entity/GpsEvent.java"
  "backend/src/main/java/za/co/fleettelematics/entity/Trip.java"
  "backend/src/main/java/za/co/fleettelematics/entity/TripPoint.java"
  "backend/src/main/java/za/co/fleettelematics/entity/Alert.java"
  "backend/src/main/java/za/co/fleettelematics/entity/MaintenanceRecord.java"
  "backend/src/main/java/za/co/fleettelematics/entity/Geofence.java"
)

for file in "${files[@]}"; do
  [[ -f "$file" ]] || { echo "Missing expected file: $file" >&2; exit 1; }
done

sed -i   -e 's/protected Vehicle()/public Vehicle()/'   backend/src/main/java/za/co/fleettelematics/entity/Vehicle.java
sed -i   -e 's/protected Driver()/public Driver()/'   backend/src/main/java/za/co/fleettelematics/entity/Driver.java
sed -i   -e 's/protected DriverAssignment()/public DriverAssignment()/'   backend/src/main/java/za/co/fleettelematics/entity/DriverAssignment.java
sed -i   -e 's/protected Telemetry()/public Telemetry()/'   backend/src/main/java/za/co/fleettelematics/entity/Telemetry.java
sed -i   -e 's/protected GpsEvent()/public GpsEvent()/'   backend/src/main/java/za/co/fleettelematics/entity/GpsEvent.java
sed -i   -e 's/protected Trip()/public Trip()/'   backend/src/main/java/za/co/fleettelematics/entity/Trip.java
sed -i   -e 's/protected TripPoint()/public TripPoint()/'   backend/src/main/java/za/co/fleettelematics/entity/TripPoint.java
sed -i   -e 's/protected Alert()/public Alert()/'   backend/src/main/java/za/co/fleettelematics/entity/Alert.java
sed -i   -e 's/protected MaintenanceRecord()/public MaintenanceRecord()/'   backend/src/main/java/za/co/fleettelematics/entity/MaintenanceRecord.java
sed -i   -e 's/protected Geofence()/public Geofence()/'   backend/src/main/java/za/co/fleettelematics/entity/Geofence.java

echo "Entity constructors updated."
