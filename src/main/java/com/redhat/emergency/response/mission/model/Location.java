package com.redhat.emergency.response.mission.model;

import java.math.BigDecimal;

public class Location {

    private BigDecimal latitude;

    private BigDecimal longitude;

    public static Location of(BigDecimal latitude, BigDecimal longitude) {
        Location location = new Location();
        location.latitude = latitude;
        location.longitude = longitude;
        return location;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }
}
