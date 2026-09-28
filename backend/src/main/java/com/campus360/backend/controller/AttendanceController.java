package com.campus360.backend.controller;

import com.campus360.backend.dto.AttendanceRequest;
import com.campus360.backend.dto.AttendanceResponse;
import com.campus360.backend.entity.AttendanceRecord;
import com.campus360.backend.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping(value = "/verify-and-mark", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttendanceResponse> verifyAndMarkAttendance(
            @RequestPart("file") MultipartFile file,
            @RequestParam("studentEmail") String studentEmail,
            @RequestParam("buildingId") UUID buildingId,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude
    ) {
        AttendanceRequest request = AttendanceRequest.builder()
                .studentEmail(studentEmail)
                .buildingId(buildingId)
                .latitude(latitude)
                .longitude(longitude)
                .build();

        return ResponseEntity.ok(attendanceService.processAttendance(file, request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AttendanceRecord>> getStudentHistory(@RequestParam String studentEmail) {
        return ResponseEntity.ok(attendanceService.getStudentHistory(studentEmail));
    }
}