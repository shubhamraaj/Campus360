package com.campus360.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String studentEmail;

    @Column(nullable = false)
    private UUID buildingId;

    private String buildingName;

    @Column(nullable = false)
    private boolean verified;

    private boolean faceMatched;

    private boolean geofenceVerified;

    private double confidenceScore;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String statusMessage;
}