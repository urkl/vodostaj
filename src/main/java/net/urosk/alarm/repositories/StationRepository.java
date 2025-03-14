package net.urosk.alarm.repositories;


import net.urosk.alarm.models.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, String> {

    Optional<Station> findStationByName(String name);

    List<Station> findAllByOrderByNameAsc();
}
