package com.redhat.emergency.response.mission.source;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import com.redhat.emergency.response.mission.map.RoutePlanner;
import com.redhat.emergency.response.mission.model.Mission;
import com.redhat.emergency.response.mission.model.MissionStatus;
import com.redhat.emergency.response.mission.repository.MissionRepository;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecordMetadata;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.core.json.JsonObject;

public class MissionCommandSource {

	private static final Logger log = Logger.getLogger(MissionCommandSource.class);

	@Inject
	RoutePlanner routeplanner;

	@Inject
	MissionRepository repository;

	@SuppressWarnings("rawtypes")
	@Incoming("mission-command")
    @Outgoing("mission-event")
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)
	public Uni<Message<String>> process(Message<String> missionCommandMessage) {
		Optional<IncomingKafkaRecordMetadata> metadata = missionCommandMessage.getMetadata(IncomingKafkaRecordMetadata.class);
		metadata.ifPresent(m -> log.debug("Consumed message from topic '" + m.getTopic() + "'', partition:offset '" + m.getPartition() + ":" + m.getOffset() + "'"));
		log.debug("Processing message: " + missionCommandMessage.getPayload());
		return Uni.createFrom().item(missionCommandMessage)
				.onItem().apply(mcm -> accept(missionCommandMessage.getPayload()))
				.onItem().apply(o -> o.flatMap(j -> validate(j.getJsonObject("body"))).orElseThrow(() -> new IllegalStateException("Message ignored")))
				.onItem().apply(m -> m.status(MissionStatus.CREATED))
				.onItem().produceUni(m -> routeplanner.getDirections(m.responderLocation(), m.destinationLocation(), m.incidentLocation())
						.map(missionSteps -> {
							m.getSteps().addAll(missionSteps);
							return m;
						}))
				.onItem().apply(m -> {
					repository.put(m);
					return m;
				})
				.onItem().apply(m -> {
					JsonObject message = new JsonObject().put("id", UUID.randomUUID().toString())
							.put("invokingService", "MissionService")
							.put("timestamp", Instant.now().toEpochMilli())
							.put("messageType", "MissionStartedEvent")
							.put("body", JsonObject.mapFrom(m));
					return (Message<String>)KafkaRecord.of(m.getIncidentId(), message.encode());
				})
				.onFailure().recoverWithUni(() -> Uni.createFrom().nullItem());
	}


	private Optional<JsonObject> accept(String messageAsJson) {
		try {
			JsonObject json = new JsonObject(messageAsJson);
			String messageType = json.getString("messageType");
			if ("CreateMissionCommand".equals(messageType) && json.containsKey("body")) {
				return Optional.of(json);
			}
			log.debug("Message with type '" + messageType + "' is ignored");
		} catch (Exception e) {
			log.warn("Unexpected message which is not JSON or without 'messageType' field.");
			log.warn("Message: " + messageAsJson);
		}
		return Optional.empty();
	}

	private Optional<Mission> validate(JsonObject json) {
		try {
			Optional<Mission> mission = Optional.of(json.mapTo(Mission.class))
					.filter(m -> m.getIncidentId() != null && !(m.getIncidentId().isBlank()))
					.filter(m -> m.getResponderId() != null && !(m.getIncidentId().isBlank()))
					.filter(m -> m.getIncidentLat() != null && m.getIncidentLong() != null)
					.filter(m -> m.getResponderStartLat() != null && m.getResponderStartLong() != null)
					.filter(m -> m.getDestinationLat() != null && m.getDestinationLong() != null);
			if (mission.isEmpty()) {
				log.warn("Missing data in Mission object. Ignoring.");
			}
			return mission;
		} catch (Exception e) {
			log.error("Exception when deserializing message body into Mission object:", e);
		}
		return Optional.empty();
	}



}
