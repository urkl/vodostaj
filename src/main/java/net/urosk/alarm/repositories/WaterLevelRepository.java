package net.urosk.alarm.repositories;

import net.urosk.alarm.models.WaterLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WaterLevelRepository extends JpaRepository<WaterLevel, String> {


    @Query(value = "SELECT * FROM water_level LIMIT 100", nativeQuery = true)
    List<WaterLevel> findTopHundred();

    List<WaterLevel> findAllByStationId(String stationId);

    List<WaterLevel> findAllByStationIdOrderByDateDesc(String stationId);

    List<WaterLevel> findByStationIdOrderByDateDesc(String stationId, Pageable pageable);

    WaterLevel findTopByStationIdOrderByDateDesc(String stationId);



}
