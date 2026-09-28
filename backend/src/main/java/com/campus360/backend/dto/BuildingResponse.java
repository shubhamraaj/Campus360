package com.campus360.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private String category;
    private double latitude;
    private double longitude;
}