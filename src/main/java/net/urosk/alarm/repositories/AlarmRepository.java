package net.urosk.alarm.repositories;

import net.urosk.alarm.models.Alarm;
import net.urosk.alarm.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByUser(User user);

    List<Alarm> findTop100ByUserId(String userId);

    List<Alarm> findByUserIdAndStationId(String userId, String stationId);

    @Query("""
  SELECT a
  FROM Alarm a
    JOIN WaterLevel w ON w.stationId = a.stationId
  WHERE 
    w.date = (
      SELECT MAX(w2.date) 
      FROM WaterLevel w2 
      WHERE w2.stationId = a.stationId
    )
    AND w.level > a.alertThresholdLevel
    AND a.alertThresholdFlow = 0 AND a.alertThresholdLevel > 0
""")
    List<Alarm> findAlarmsOverThresholdForLevel();


    @Query("""
  SELECT a
  FROM Alarm a
    JOIN WaterLevel w ON w.stationId = a.stationId
  WHERE 
    w.date = (
      SELECT MAX(w2.date) 
      FROM WaterLevel w2 
      WHERE w2.stationId = a.stationId
    )
    AND w.flow > a.alertThresholdFlow
    AND a.alertThresholdLevel = 0 AND a.alertThresholdFlow > 0
""")
    List<Alarm> findAlarmsOverThresholdForFlow();

    void deleteByChatId(String endpoint);
}

