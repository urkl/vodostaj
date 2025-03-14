package net.urosk.alarm.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.urosk.alarm.models.Station;
import net.urosk.alarm.models.WaterLevel;
import net.urosk.alarm.repositories.StationRepository;
import net.urosk.alarm.repositories.WaterLevelRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StationsService {

    private final StationRepository stationRepository;
    private final WaterLevelRepository waterLevelRepository;
    // Keš za postaje
    private final Map<String, Station> stationCache = new ConcurrentHashMap<>();

    public StationsService(StationRepository stationRepository, WaterLevelRepository waterLevelRepository) {
        this.stationRepository = stationRepository;
        this.waterLevelRepository = waterLevelRepository;
    }

    public List<Station> getStationCache() {
        return stationCache.values().stream()
                .sorted(Comparator.comparing(Station::getName, String.CASE_INSENSITIVE_ORDER)) // Sortiranje po imenu
                .collect(Collectors.toList());
    }

    /**
     * Refresh is called after getting all stations. Check Water Level service for more info.
     */
    //@Scheduled(fixedRate = 300_000) // 300,000 ms = 5 min
    public void refreshCache() {

        log.info("Refreshing station cache");

        stationCache.clear();

        var stations = stationRepository.findAllByOrderByNameAsc();

        stations.forEach(station -> {

            //log.info("🔄 Osveževanje podatkov za postajo: {}", station.getName());
            WaterLevel waterLevel = waterLevelRepository.findTopByStationIdOrderByDateDesc(station.getId());


            // Pridobite zadnjih 5 meritev za postajo
            List<WaterLevel> history = waterLevelRepository.findByStationIdOrderByDateDesc(station.getId(), PageRequest.of(0, 5));

            history=history.reversed();

            // Izluščite podatke o pretoku
            List<Double> flowHistory = history.stream()
                    .map(WaterLevel::getFlow)
                    .toList();
            station.setFlowHistory(flowHistory);

            station.setTempCurrentFlow(waterLevel.getFlow());
            station.setTempCurrentLevel(waterLevel.getLevel());
            station.setLastWaterLevel(waterLevel);
            stationCache.put(station.getId(), station);


        });


    }

    /**
     * Inicialno napolni tabelo stations z unikatnimi postajami iz WaterLevel.
     */
    @PostConstruct
    public void initializeStations() {
        List<WaterLevel> allWaterLevels = waterLevelRepository.findTopHundred();

        allWaterLevels.forEach(waterLevel -> {
            stationRepository.findStationByName(waterLevel.getName())
                    .orElseGet(() -> {
                        Station newStation = new Station(
                                waterLevel.getStationId(),
                                waterLevel.getName(),
                                waterLevel.getRiver(),
                                waterLevel.getLatitude(),
                                waterLevel.getLongitude(),
                                waterLevel.getLevel1(),
                                waterLevel.getLevel2(),
                                waterLevel.getLevel3(),
                                waterLevel.getFlow1(),
                                waterLevel.getFlow2(),
                                waterLevel.getFlow3(),
                                waterLevel.getMeterBaseline(),
                                0,
                                0,
                                List.of(),
                                waterLevel.getType(),null



                        );
                        return stationRepository.save(newStation);
                    });
        });

        refreshCache();
    }


    public List<Station> getAllStations() {
        return stationRepository.findAllByOrderByNameAsc();
    }


    public Station getStationFromId(String stationId) {
        return stationCache.get(stationId);
    }
}
