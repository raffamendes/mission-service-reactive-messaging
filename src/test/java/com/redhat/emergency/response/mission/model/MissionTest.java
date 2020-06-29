package com.redhat.emergency.response.mission.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.math.BigDecimal;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

public class MissionTest {

    @Test
    void testMapFromJsonObject() {

        JsonObject jsonObject = new JsonObject().put("incidentId", "incident1").put("responderId", "15")
                .put("responderStartLat", "30.12345").put("responderStartLong", "-70.98765")
                .put("incidentLat", "31.98765").put("incidentLong", "-71.12345")
                .put("destinationLat", "32.85263").put("destinationLong", "-72.15975")
                .put("processId", "5");

        Mission mission = jsonObject.mapTo(Mission.class);

        assertThat(mission, notNullValue());
        assertThat(mission.getId().length(), equalTo(36));
        assertThat(mission.getResponderLocationHistory().size(), equalTo(0));
        assertThat(mission.getSteps().size(), equalTo(0));
        assertThat(mission.getIncidentId(), equalTo(jsonObject.getString("incidentId")));
        assertThat(mission.getResponderId(), equalTo(jsonObject.getString("responderId")));
        assertThat(mission.getResponderStartLat(), equalTo(new BigDecimal(jsonObject.getString("responderStartLat"))));
        assertThat(mission.getResponderStartLong(), equalTo(new BigDecimal(jsonObject.getString("responderStartLong"))));
        assertThat(mission.getIncidentLat(), equalTo(new BigDecimal(jsonObject.getString("incidentLat"))));
        assertThat(mission.getIncidentLong(), equalTo(new BigDecimal(jsonObject.getString("incidentLong"))));
        assertThat(mission.getDestinationLat(), equalTo(new BigDecimal(jsonObject.getString("destinationLat"))));
        assertThat(mission.getDestinationLong(), equalTo(new BigDecimal(jsonObject.getString("destinationLong"))));
    }

    @Test
    void testMapFromJsonObjectMissingFields() {

        JsonObject jsonObject = new JsonObject().put("incidentId", "incident1").put("responderId", "15")
                .put("incidentLat", "31.98765").put("incidentLong", "-71.12345")
                .put("destinationLat", "32.85263").put("destinationLong", "-72.15975")
                .put("processId", "5");

        Mission mission = jsonObject.mapTo(Mission.class);

        assertThat(mission, notNullValue());
        assertThat(mission.getId().length(), equalTo(36));
        assertThat(mission.getResponderLocationHistory().size(), equalTo(0));
        assertThat(mission.getSteps().size(), equalTo(0));
        assertThat(mission.getIncidentId(), equalTo(jsonObject.getString("incidentId")));
        assertThat(mission.getResponderId(), equalTo(jsonObject.getString("responderId")));
        assertThat(mission.getResponderStartLat(), nullValue());
        assertThat(mission.getResponderStartLong(), nullValue());
        assertThat(mission.getIncidentLat(), equalTo(new BigDecimal(jsonObject.getString("incidentLat"))));
        assertThat(mission.getIncidentLong(), equalTo(new BigDecimal(jsonObject.getString("incidentLong"))));
        assertThat(mission.getDestinationLat(), equalTo(new BigDecimal(jsonObject.getString("destinationLat"))));
        assertThat(mission.getDestinationLong(), equalTo(new BigDecimal(jsonObject.getString("destinationLong"))));
    }

}
