package com.campus360.backend.service;

import com.campus360.backend.dto.*;
import com.campus360.backend.entity.Building;
import com.campus360.backend.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GisService {

    private final BuildingRepository buildingRepository;

    public List<BuildingResponse> getAllBuildings() {
        return buildingRepository.findAll().stream()
                .map(this::mapToBuildingResponse)
                .collect(Collectors.toList());
    }

    public BuildingResponse getBuildingById(UUID id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found with ID: " + id));
        return mapToBuildingResponse(building);
    }

    public GeofenceCheckResponse verifyGeofence(GeofenceCheckRequest request) {
        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found with ID: " + request.getBuildingId()));

        Integer isInsideFlag = buildingRepository.checkPointInsideBuilding(
                request.getBuildingId(), request.getLatitude(), request.getLongitude());
        Double distance = buildingRepository.getDistanceToBuildingMeters(
                request.getBuildingId(), request.getLatitude(), request.getLongitude());

        boolean inside = isInsideFlag != null && isInsideFlag == 1;
        double dist = distance != null ? Math.round(distance * 100.0) / 100.0 : 0.0;

        String message = inside
                ? "Location verified inside " + building.getName() + " geofence."
                : "Location outside " + building.getName() + " geofence (" + dist + "m away).";

        return GeofenceCheckResponse.builder()
                .insideGeofence(inside)
                .distanceMeters(dist)
                .buildingName(building.getName())
                .message(message)
                .build();
    }

    public List<BuildingResponse> getNearestBuildings(double lat, double lon, int limit) {
        return buildingRepository.findNearestBuildings(lat, lon, limit).stream()
                .map(this::mapToBuildingResponse)
                .collect(Collectors.toList());
    }

    public RouteResponse calculateRoute(RouteRequest request) {
        Building startBuilding = buildingRepository.findById(request.getStartBuildingId())
                .orElseThrow(() -> new RuntimeException("Start building not found"));
        Building destBuilding = buildingRepository.findById(request.getDestinationBuildingId())
                .orElseThrow(() -> new RuntimeException("Destination building not found"));

        Double distance = buildingRepository.getDistanceBetweenBuildingsMeters(
                startBuilding.getId(), destBuilding.getId());
        double distMeters = distance != null ? Math.round(distance * 100.0) / 100.0 : 0.0;

        // Estimated walking speed: ~1.4 meters per second (~84 meters/min)
        int walkTimeMin = (int) Math.ceil(distMeters / 84.0);

        Double startLat = buildingRepository.getCentroidLat(startBuilding.getId());
        Double startLon = buildingRepository.getCentroidLon(startBuilding.getId());
        Double destLat = buildingRepository.getCentroidLat(destBuilding.getId());
        Double destLon = buildingRepository.getCentroidLon(destBuilding.getId());

        List<RouteResponse.Waypoint> waypoints = Arrays.asList(
                new RouteResponse.Waypoint(startLat != null ? startLat : 0.0, startLon != null ? startLon : 0.0),
                new RouteResponse.Waypoint(destLat != null ? destLat : 0.0, destLon != null ? destLon : 0.0)
        );

        return RouteResponse.builder()
                .startBuildingName(startBuilding.getName())
                .destinationBuildingName(destBuilding.getName())
                .distanceMeters(distMeters)
                .estimatedWalkTimeMinutes(walkTimeMin)
                .waypoints(waypoints)
                .build();
    }

    private BuildingResponse mapToBuildingResponse(Building building) {
        Double lat = buildingRepository.getCentroidLat(building.getId());
        Double lon = buildingRepository.getCentroidLon(building.getId());

        return BuildingResponse.builder()
                .id(building.getId())
                .name(building.getName())
                .code(building.getCode())
                .description(building.getDescription())
                .category(building.getCategory())
                .latitude(lat != null ? lat : 0.0)
                .longitude(lon != null ? lon : 0.0)
                .build();
    }
}