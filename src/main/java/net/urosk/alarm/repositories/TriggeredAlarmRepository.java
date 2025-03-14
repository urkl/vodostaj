package net.urosk.alarm.repositories;

import net.urosk.alarm.models.Alarm;
import net.urosk.alarm.models.TriggeredAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TriggeredAlarmRepository extends JpaRepository<TriggeredAlarm, Long> {
    List<TriggeredAlarm> findByAlarm(Alarm alarm);

    // Metoda, ki pobriše vse sprožene alarme za izbran "Alarm"
    void deleteAllByAlarm(Alarm alarm);

    @Query("""
   SELECT ta
   FROM TriggeredAlarm ta
   WHERE ta.alarm.user.id = :userId
   ORDER BY ta.triggeredAt DESC
""")
    List<TriggeredAlarm> findTop100ByUserIdOrderByTriggeredAtDesc(@Param("userId" ) String userId);

    Optional<TriggeredAlarm> findTopByAlarmOrderByTriggeredAtDesc(Alarm alarm);
}