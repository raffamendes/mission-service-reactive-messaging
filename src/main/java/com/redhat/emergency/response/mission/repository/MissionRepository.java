package com.redhat.emergency.response.mission.repository;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import com.redhat.emergency.response.mission.model.Mission;

@ApplicationScoped
public class MissionRepository {

    private Map<String, Mission> repository = new HashMap<>();

    public void put(Mission mission) {
        repository.put(mission.getKey(), mission);
    }
}
