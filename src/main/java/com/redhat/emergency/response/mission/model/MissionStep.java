package com.redhat.emergency.response.mission.model;

import java.math.BigDecimal;

import io.vertx.core.json.Json;

public class MissionStep {

    private BigDecimal lat;

    private BigDecimal lon;

    private boolean wayPoint = false;

    private boolean destination = false;

    public BigDecimal getLat() {
        return lat;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public boolean isWayPoint() {
        return wayPoint;
    }

    public boolean isDestination() {
        return destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionStep step = (MissionStep) o;

        return (lat != null && step.lat != null && lat.compareTo(step.lat) == 0) || (lat == null && step.lat == null) &&
                (lon != null && step.lon != null && lon.compareTo(step.lon) == 0) || (lat == null && step.lat == null) &&
                step.destination == destination &&
                step.wayPoint == wayPoint;
    }

    public String toJson() {
        return Json.encode(this);
    }

    @Override
    public String toString() {
        return toJson();
    }

    public static Builder builder(BigDecimal lat, BigDecimal lon) {
        return new Builder(lat, lon);
    }

    public static class Builder {

        private final MissionStep missionStep = new MissionStep();

        public Builder(BigDecimal lat, BigDecimal lon) {
            missionStep.lat = lat;
            missionStep.lon = lon;
        }

        public Builder wayPoint(boolean waypoint) {
            missionStep.wayPoint = waypoint;
            return this;
        }

        public Builder destination(boolean destination) {
            missionStep.destination = destination;
            return this;
        }

        public MissionStep build() {
            return missionStep;
        }
    }

}
