package com.redhat.emergency.response.mission.map;

import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

import com.redhat.emergency.response.mission.model.Location;
import com.redhat.emergency.response.mission.model.MissionStep;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class RoutePlanner {

    public Uni<List<MissionStep>> getDirections(Location origin, Location destination, Location waypoint) {

        return Uni.createFrom().item(Collections::emptyList);
    }

}
