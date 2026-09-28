package com.campus360.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    private String startBuildingName;
    private String destinationBuildingName;
    private double distanceMeters;
    private int estimatedWalkTimeMinutes;
    private List<Waypoint> waypoints;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Waypoint {
        private double latitude;
        private double longitude;
    }
}