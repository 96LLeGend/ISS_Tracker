package model;

/**
 * Enumerated type for all possible telemetry status:
 * "Full" - All telemetry is in realtime
 * "LocationSourceOnly" - Only location telemetry is in realtime
 * "FlightDataSourceOnly" - Only flight data telemetry is in real time
 * "LostDelta" - Lost the telemetry about the Delta
 * "LostAll" - Lost all telemetry
 */
public enum TelemetryStatus {Connected, LostLocationSource, LostFlightDataSource, LostConnection}
