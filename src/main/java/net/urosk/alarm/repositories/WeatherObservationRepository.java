package net.urosk.alarm.repositories;

import net.urosk.alarm.models.WeatherObservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherObservationRepository extends JpaRepository<WeatherObservation, Long> {
    // Dodatne metode za iskanje lahko dodate po potrebi

    // Preverimo, ali že obstaja zapis za postajo ob določenem času
    //@Query("SELECT w FROM WeatherObservation w WHERE w.stationId = :stationId AND w.observationTime = :observationTime")
    Optional<WeatherObservation> findByStationIdAndObservationTime( String stationId, LocalDateTime observationTime);


    @Query(value = """
            SELECT * FROM weather_observations w
            WHERE observation_time = (
                SELECT MAX(w2.observation_time)
                FROM weather_observations w2
                WHERE w2.station_id = w.station_id
            )
            """, nativeQuery = true)
    List<WeatherObservation> findLatestObservations();
}