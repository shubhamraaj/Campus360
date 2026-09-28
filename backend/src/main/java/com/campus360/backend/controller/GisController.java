package com.campus360.backend.controller;

import com.campus360.backend.dto.*;
import com.campus360.backend.service.GisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gis")
@RequiredArgsConstructor
public class GisController {

    private final GisService gisService;

    @GetMapping("/buildings")
    public ResponseEntity<List<BuildingResponse>> getAllBuildings() {
        return ResponseEntity.ok(gisService.getAllBuildings());
    }

    @GetMapping("/buildings/{id}")
    public ResponseEntity<BuildingResponse> getBuildingById(@PathVariable UUID id) {
        return ResponseEntity.ok(gisService.getBuildingById(id));
    }

    @GetMapping("/buildings/nearest")
    public ResponseEntity<List<BuildingResponse>> getNearestBuildings(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "3") int limit
    ) {
        return ResponseEntity.ok(gisService.getNearestBuildings(latitude, longitude, limit));
    }

    @PostMapping("/geofence/verify")
    public ResponseEntity<GeofenceCheckResponse> verifyGeofence(@RequestBody GeofenceCheckRequest request) {
        return ResponseEntity.ok(gisService.verifyGeofence(request));
    }

    @PostMapping("/route")
    public ResponseEntity<RouteResponse> calculateRoute(@RequestBody RouteRequest request) {
        return ResponseEntity.ok(gisService.calculateRoute(request));
    }
}