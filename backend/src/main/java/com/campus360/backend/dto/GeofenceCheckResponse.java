package com.campus360.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceCheckResponse {
    private boolean insideGeofence;
    private double distanceMeters;
    private String buildingName;
    private String message;
}