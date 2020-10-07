package com.redhat.emergency.response.mission.source;

import java.util.Arrays;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

@QuarkusTest
public class MissionCommandSourceTest {

	@InjectMock
	RoutePlanner routePlanner;

	@InjectMock
	MissionRepository repository;

	@Inject @Any
	InMemoryConnector connector;

	@BeforeEach
	void init() {
		connector.sink("mission-event").clear();
	}

	@Test
	void testProcessMessage() {
		//Set up
		InMemorySink<String> missionEvents = connector.sink("mission-event");
		InMemorySource<String> missionCommand = connector.source("mission-command");

		MissionStep missionStep1 = new MissionStep();
		MissionStep missionStep2 = new MissionStep();

		Mockito.when(routePlanner.getDirections(Mockito.any(Location.class), Mockito.any(Location.class), Mockito.any(Location.class)))
		.thenReturn(Uni.createFrom().item(Arrays.asList(missionStep1, missionStep2)));

		// send message
		String payload = "{\"id\":\"91cf5e82-8135-476d-ade4-5fe00dca2cc6\",\"messageType\":\"CreateMissionCommand\","
				+ "\"invokingService\":\"IncidentProcessService\",\"timestamp\":1593363522344,\"body\": "
				+ "{\"incidentId\":\"incident123\",\"responderId\":\"responder123\",\"responderStartLat\":\"40.12345\","
				+ "\"responderStartLong\":\"-80.98765\",\"incidentLat\":\"30.12345\",\"incidentLong\":\"-70.98765\","
				+ "\"destinationLat\":\"50.12345\",\"destinationLong\":\"-90.98765\",\"processId\":\"0\"}}";

		missionCommand.send(payload);

		// verify
		MatcherAssert.assertThat(missionEvents.received().size(), Matchers.equalTo(1));

		Mockito.verify(routePlanner).getDirections(Mockito.any(Location.class), Mockito.any(Location.class), Mockito.any(Location.class));
		Mockito.verify(repository).put(Mockito.any(Mission.class));

	}

}
