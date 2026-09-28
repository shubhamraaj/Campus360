package com.campus360.backend.repository;

import com.campus360.backend.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuildingRepository extends JpaRepository<Building, UUID> {

    Optional<Building> findByCode(String code);

    @Query(value = "SELECT CASE WHEN ST_Contains(boundary, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)) THEN 1 ELSE 0 END FROM buildings WHERE id = :buildingId", nativeQuery = true)
    Integer checkPointInsideBuilding(@Param("buildingId") UUID buildingId, @Param("lat") double lat, @Param("lon") double lon);

    @Query(value = "SELECT ST_Distance(ST_Centroid(boundary)::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) FROM buildings WHERE id = :buildingId", nativeQuery = true)
    Double getDistanceToBuildingMeters(@Param("buildingId") UUID buildingId, @Param("lat") double lat, @Param("lon") double lon);

    // Precise geodesic distance calculation between two building centroids (in meters)
    @Query(value = "SELECT ST_Distance(ST_Centroid(b1.boundary)::geography, ST_Centroid(b2.boundary)::geography) FROM buildings b1, buildings b2 WHERE b1.id = :startId AND b2.id = :destId", nativeQuery = true)
    Double getDistanceBetweenBuildingsMeters(@Param("startId") UUID startId, @Param("destId") UUID destId);

    @Query(value = "SELECT ST_Y(ST_Centroid(boundary)) FROM buildings WHERE id = :buildingId", nativeQuery = true)
    Double getCentroidLat(@Param("buildingId") UUID buildingId);

    @Query(value = "SELECT ST_X(ST_Centroid(boundary)) FROM buildings WHERE id = :buildingId", nativeQuery = true)
    Double getCentroidLon(@Param("buildingId") UUID buildingId);

    @Query(value = "SELECT * FROM buildings ORDER BY boundary <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) LIMIT :limitCount", nativeQuery = true)
    List<Building> findNearestBuildings(@Param("lat") double lat, @Param("lon") double lon, @Param("limitCount") int limitCount);
}