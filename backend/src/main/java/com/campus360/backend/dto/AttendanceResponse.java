package com.campus360.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private boolean verified;
    private boolean faceMatched;
    private boolean geofenceVerified;
    private double confidenceScore;
    private String studentEmail;
    private LocalDateTime timestamp;
    private String statusMessage;
}