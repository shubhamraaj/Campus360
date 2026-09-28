package com.campus360.backend.service;

import com.campus360.backend.dto.AttendanceRequest;
import com.campus360.backend.dto.AttendanceResponse;
import com.campus360.backend.dto.GeofenceCheckRequest;
import com.campus360.backend.dto.GeofenceCheckResponse;
import com.campus360.backend.entity.AttendanceRecord;
import com.campus360.backend.repository.AttendanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final GisService gisService;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String FASTAPI_AI_URL = "http://campus360_ai_engine:8000/api/v1/vision/match-face";

    public AttendanceResponse processAttendance(MultipartFile faceImage, AttendanceRequest request) {
        // 1. Verify Geofence via PostGIS
        GeofenceCheckRequest geofenceRequest = GeofenceCheckRequest.builder()
                .buildingId(request.getBuildingId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        GeofenceCheckResponse geofenceResult = gisService.verifyGeofence(geofenceRequest);

        // 2. Forward Face Image to FastAPI AI Microservice
        boolean faceMatched = false;
        double confidence = 0.0;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileResource = new ByteArrayResource(faceImage.getBytes()) {
                @Override
                public String getFilename() {
                    return faceImage.getOriginalFilename() != null ? faceImage.getOriginalFilename() : "face.jpg";
                }
            };

            body.add("file", fileResource);
            body.add("student_id", request.getStudentEmail());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map<String, Object>> aiResponse = restTemplate.exchange(
                    FASTAPI_AI_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (aiResponse.getBody() != null && Boolean.TRUE.equals(aiResponse.getBody().get("matched"))) {
                faceMatched = true;
                confidence = Double.parseDouble(aiResponse.getBody().get("confidence").toString());
            }
        } catch (Exception e) {
            System.err.println("Error communicating with AI Engine: " + e.getMessage());
        }

        // 3. Dual-factor verification check
        boolean verified = geofenceResult.isInsideGeofence() && faceMatched;
        String status = verified
                ? "Attendance marked successfully! Geofence and Face matched."
                : "Attendance failed verification. Geofence: " + geofenceResult.isInsideGeofence() + ", Face Matched: " + faceMatched;

        LocalDateTime now = LocalDateTime.now();

        // 4. Save Record to Database
        AttendanceRecord record = AttendanceRecord.builder()
                .studentEmail(request.getStudentEmail())
                .buildingId(request.getBuildingId())
                .buildingName(geofenceResult.getBuildingName())
                .verified(verified)
                .faceMatched(faceMatched)
                .geofenceVerified(geofenceResult.isInsideGeofence())
                .confidenceScore(confidence)
                .timestamp(now)
                .statusMessage(status)
                .build();

        attendanceRecordRepository.save(record);

        return AttendanceResponse.builder()
                .verified(verified)
                .faceMatched(faceMatched)
                .geofenceVerified(geofenceResult.isInsideGeofence())
                .confidenceScore(confidence)
                .studentEmail(request.getStudentEmail())
                .timestamp(now)
                .statusMessage(status)
                .build();
    }

    public List<AttendanceRecord> getStudentHistory(String email) {
        return attendanceRecordRepository.findByStudentEmailOrderByTimestampDesc(email);
    }
}