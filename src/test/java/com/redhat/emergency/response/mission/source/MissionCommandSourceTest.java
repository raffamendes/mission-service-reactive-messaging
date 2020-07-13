package com.redhat.emergency.response.mission.source;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonNodeAbsent;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonNodePresent;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.util.Arrays;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import com.redhat.emergency.response.mission.map.RoutePlanner;
import com.redhat.emergency.response.mission.model.Location;
import com.redhat.emergency.response.mission.model.Mission;
import com.redhat.emergency.response.mission.model.MissionStep;
import com.redhat.emergency.response.mission.repository.MissionRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;
import io.smallrye.reactive.messaging.connectors.InMemorySource;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecord;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

@QuarkusTest
public class MissionCommandSourceTest {

    @Inject
    MissionCommandSource source;

    @Inject @Any
    InMemoryConnector connector;

    @InjectMock
    RoutePlanner routePlanner;

    @InjectMock
    MissionRepository repository;

    @Captor
    ArgumentCaptor<Mission> missionCaptor;

    @Captor
    ArgumentCaptor<Location> locationCaptor;

    @BeforeEach
    void init() {
        connector.sink("mission-event").clear();
        initMocks(this);
    }

    @Test
    void testProcessMessage() {

        String payload = "{\"id\":\"91cf5e82-8135-476d-ade4-5fe00dca2cc6\",\"messageType\":\"CreateMissionCommand\","
                + "\"invokingService\":\"IncidentProcessService\",\"timestamp\":1593363522344,\"body\": "
                + "{\"incidentId\":\"incident123\",\"responderId\":\"responder123\",\"responderStartLat\":\"40.12345\","
                + "\"responderStartLong\":\"-80.98765\",\"incidentLat\":\"30.12345\",\"incidentLong\":\"-70.98765\","
                + "\"destinationLat\":\"50.12345\",\"destinationLong\":\"-90.98765\",\"processId\":\"0\"}}";

        InMemorySink<String> missionEvents = connector.sink("mission-event");
        InMemorySource<String> missionCommand = connector.source("mission-command");

        MissionStep missionStep1 = new MissionStep();
        MissionStep missionStep2 = new MissionStep();

        when(routePlanner.getDirections(any(Location.class), any(Location.class), any(Location.class)))
                .thenReturn(Uni.createFrom().item(Arrays.asList(missionStep1, missionStep2)));

        missionCommand.send(payload);

        assertThat(missionEvents.received().size(), equalTo(1));
        Message<String> message = missionEvents.received().get(0);
        assertThat(message, instanceOf(OutgoingKafkaRecord.class));
        String value = message.getPayload();
        String key = ((OutgoingKafkaRecord<String, String>)message).getKey();
        assertThat(key, equalTo("incident123"));
        assertThat(value, jsonNodePresent("id"));
        assertThat(value, jsonPartEquals("messageType", "MissionStartedEvent"));
        assertThat(value, jsonPartEquals("invokingService", "MissionService"));
        assertThat(value, jsonNodePresent("timestamp"));
        assertThat(value, jsonNodePresent("body"));
        assertThat(value, jsonPartEquals("body.incidentId", "incident123"));
        assertThat(value, jsonNodePresent("body.id"));
        assertThat(value, jsonPartEquals("body.id", "${json-unit.regex}[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"));
        assertThat(value, jsonPartEquals("body.responderId", "responder123"));
        assertThat(value, jsonPartEquals("body.incidentLat", 30.12345));
        assertThat(value, jsonPartEquals("body.incidentLong", -70.98765));
        assertThat(value, jsonPartEquals("body.responderStartLat", 40.12345));
        assertThat(value, jsonPartEquals("body.responderStartLong", -80.98765));
        assertThat(value, jsonPartEquals("body.destinationLat", 50.12345));
        assertThat(value, jsonPartEquals("body.destinationLong", -90.98765));
        assertThat(value, jsonNodePresent("body.steps"));
        assertThat(value, jsonNodePresent("body.steps[0]"));
        assertThat(value, jsonNodePresent("body.steps[1]"));
        assertThat(value, jsonNodePresent("body.responderLocationHistory"));
        assertThat(value, jsonNodeAbsent("body.responderLocationHistory[0]"));

        verify(routePlanner).getDirections(locationCaptor.capture(),locationCaptor.capture(), locationCaptor.capture());
        Location location1 = locationCaptor.getAllValues().get(0);
        Location location2 = locationCaptor.getAllValues().get(1);
        Location location3 = locationCaptor.getAllValues().get(2);
        assertThat(location1, notNullValue());
        assertThat(location2, notNullValue());
        assertThat(location3, notNullValue());
        assertThat(location1.getLatitude(), equalTo(new BigDecimal("40.12345")));
        assertThat(location1.getLongitude(), equalTo(new BigDecimal("-80.98765")));
        assertThat(location2.getLatitude(), equalTo(new BigDecimal("50.12345")));
        assertThat(location2.getLongitude(), equalTo(new BigDecimal("-90.98765")));
        assertThat(location3.getLatitude(), equalTo(new BigDecimal("30.12345")));
        assertThat(location3.getLongitude(), equalTo(new BigDecimal("-70.98765")));

        verify(repository).put(missionCaptor.capture());
        Mission mission = missionCaptor.getValue();
        assertThat(mission, notNullValue());
        assertThat(mission.getIncidentId(), equalTo("incident123"));
        assertThat(mission.getIncidentLat(), equalTo(new BigDecimal("30.12345")));
        assertThat(mission.getIncidentLong(), equalTo(new BigDecimal("-70.98765")));
        assertThat(mission.getResponderId(), equalTo("responder123"));
        assertThat(mission.getResponderStartLat(), equalTo(new BigDecimal("40.12345")));
        assertThat(mission.getResponderStartLong(), equalTo(new BigDecimal("-80.98765")));
        assertThat(mission.getDestinationLat(), equalTo(new BigDecimal("50.12345")));
        assertThat(mission.getDestinationLong(), equalTo(new BigDecimal("-90.98765")));
        assertThat(mission.getSteps().size(), equalTo(2));

    }

    @Test
    void testProcessMessageBadMessageType() {

        String payload = "{\"id\":\"91cf5e82-8135-476d-ade4-5fe00dca2cc6\",\"messageType\":\"WrongMessageType\","
                + "\"invokingService\":\"IncidentProcessService\",\"timestamp\":1593363522344,\"body\": "
                + "{\"incidentId\":\"incident123\",\"responderId\":\"responder123\",\"responderStartLat\":\"40.12345\","
                + "\"responderStartLong\":\"-80.98765\",\"incidentLat\":\"30.12345\",\"incidentLong\":\"-70.98765\","
                + "\"destinationLat\":\"50.12345\",\"destinationLong\":\"-90.98765\",\"processId\":\"0\"}}";

        InMemorySink<String> missionEvents = connector.sink("mission-event");
        InMemorySource<String> missionCommand = connector.source("mission-command");

        missionCommand.send(payload);

        assertThat(missionEvents.received().size(), equalTo(0));

        verify(repository, never()).put(any(Mission.class));
        verify(routePlanner, never()).getDirections(any(Location.class), any(Location.class), any(Location.class));
    }

    @Test
    void testProcessMessageMissingData() {

        String payload = "{\"id\":\"91cf5e82-8135-476d-ade4-5fe00dca2cc6\",\"messageType\":\"WrongMessageType\","
                + "\"invokingService\":\"IncidentProcessService\",\"timestamp\":1593363522344,\"body\": "
                + "{\"incidentId\":\"incident123\",\"responderId\":\"responder123\","
                + "\"incidentLat\":\"30.12345\",\"incidentLong\":\"-70.98765\","
                + "\"destinationLat\":\"50.12345\",\"destinationLong\":\"-90.98765\",\"processId\":\"0\"}}";

        InMemorySink<String> missionEvents = connector.sink("mission-event");
        InMemorySource<String> missionCommand = connector.source("mission-command");

        missionCommand.send(payload);

        assertThat(missionEvents.received().size(), equalTo(0));

        verify(repository, never()).put(any(Mission.class));
        verify(routePlanner, never()).getDirections(any(Location.class), any(Location.class), any(Location.class));
    }

}
