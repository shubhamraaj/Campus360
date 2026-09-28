package com.campus360.backend.repository;

import com.campus360.backend.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    List<AttendanceRecord> findByStudentEmailOrderByTimestampDesc(String studentEmail);

    List<AttendanceRecord> findByBuildingIdOrderByTimestampDesc(UUID buildingId);
}