package com.redhat.emergency.response.mission.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.Json;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Mission {

    private String id;

    private String incidentId;

    private String responderId;

    private BigDecimal responderStartLat;

    private BigDecimal responderStartLong;

    private BigDecimal incidentLat;

    private BigDecimal incidentLong;

    private BigDecimal destinationLat;

    private BigDecimal destinationLong;

    private List<ResponderLocationHistory> responderLocationHistory;

    private String status;

    private List<MissionStep> steps;

    public Mission() {
        id = UUID.randomUUID().toString();
        responderLocationHistory = new ArrayList<>();
        steps = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public String getResponderId() {
        return responderId;
    }

    public BigDecimal getResponderStartLat() {
        return responderStartLat;
    }

    public BigDecimal getResponderStartLong() {
        return responderStartLong;
    }

    public BigDecimal getIncidentLat() {
        return incidentLat;
    }

    public BigDecimal getIncidentLong() {
        return incidentLong;
    }

    public BigDecimal getDestinationLat() {
        return destinationLat;
    }

    public BigDecimal getDestinationLong() {
        return destinationLong;
    }

    public List<ResponderLocationHistory> getResponderLocationHistory() {
        return responderLocationHistory;
    }

    public String getStatus() {
        return status;
    }

    public Mission status(MissionStatus status) {
        this.status = status.name();
        return this;
    }

    public List<MissionStep> getSteps() {
        return steps;
    }

    public Location responderLocation() {
        return Location.of(responderStartLat, responderStartLong);
    }

    public Location incidentLocation() {
        return Location.of(incidentLat, incidentLong);
    }

    public Location destinationLocation() {
        return Location.of(destinationLat, destinationLong);
    }

    public String toJson() {
        return Json.encode(this);
    }

    @Override
    public String toString() {
        return toJson();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mission mission = (Mission) o;
        return Objects.equals(responderId, mission.responderId) && Objects.equals(incidentId, mission.incidentId);
    }

    @JsonIgnore
    public String getKey(){
        return this.incidentId + ":" + this.responderId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }
}
