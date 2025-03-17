package net.urosk.alarm.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.urosk.alarm.models.WaterLevel;
import net.urosk.alarm.repositories.WaterLevelRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static net.urosk.alarm.lib.Constants.ARSO_XML_URL;

@Slf4j
@Service
public class WaterLevelService {


    private final WaterLevelRepository waterLevelRepository;

    private final StationsService stationsService;

    public WaterLevelService(WaterLevelRepository waterLevelRepository, StationsService stationsService) {
        this.waterLevelRepository = waterLevelRepository;
        this.stationsService = stationsService;
    }

    private double parseOrDefault(String value, double defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // Če pride do težav pri parsiranju (npr. napačna oblika), vrne defaultValue
            return defaultValue;
        }
    }

    @PostConstruct
    public void initializeWaterLevels() {
        fetchAndSaveWaterLevels();
    }

    /*
     * CRON izraz: "0 5,35 * * * *"
     *
     * | Del izraza | Pomen                                 |
     * |------------|--------------------------------------|
     * | 0          | Začne izvajanje ob 0. sekundi       |
     * | 5,35       | Minute: 5 minut in 35 minut po uri |
     * | *          | Ura: Vsako uro                      |
     * | *          | Dan v mesecu: Vsak dan             |
     * | *          | Mesec: Vsak mesec                   |
     * | *          | Dan v tednu: Vsak dan               |
     *
     * Izvajanje: 00:05, 00:35, 01:05, 01:35, ..., 23:05, 23:35 NON-STOP
     */
    @Scheduled(cron = "0 5,35 * * * *")
    public void fetchAndSaveWaterLevels() {
        try {
            log.info("🔄 Pridobivanje novih vodostajev...");

            // Pridobi XML iz URL-ja
            URL url = new URL(ARSO_XML_URL);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(url.openStream());

            // Parsiraj XML
            NodeList postajaNodes = doc.getElementsByTagName("postaja");
            List<WaterLevel> latestLevels = new ArrayList<>();
//http://hmljn.arso.gov.si/vode/podatki/opis_hidro_xml.pdf

            for (int i = 0; i < postajaNodes.getLength(); i++) {
                Element postajaElement = (Element) postajaNodes.item(i);
                String stationId = postajaElement.getAttribute("sifra");
                double longitude = parseOrDefault(postajaElement.getAttribute("wgs84_dolzina"), 0);
                double latitude = parseOrDefault(postajaElement.getAttribute("wgs84_sirina"), 0);
                double meterBaseline = parseOrDefault(postajaElement.getAttribute("kota_0"), 0);

                String name = getTagValue("ime_kratko", postajaElement);
                String river = getTagValue("reka", postajaElement);
                String dateString = getTagValue("datum", postajaElement);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime date = LocalDateTime.parse(dateString, formatter);
// Če je vrednost null ali prazna, nastavi 0, sicer parsiraj v double
                double level = parseOrDefault(getTagValue("vodostaj", postajaElement), 0.0);
                double flow = parseOrDefault(getTagValue("pretok", postajaElement), 0.0);
                double temperature = parseOrDefault(getTagValue("temp_vode", postajaElement), 0.0);


                String type = "reka";
                // rekla jezero

                if (hasTagValue("prvi_vv_vodostaj", postajaElement)) {
                    type = "jezero";
                }


                double level1 = parseOrDefault(getTagValue("prvi_vv_vodostaj", postajaElement), 0.0);
                double level2 = parseOrDefault(getTagValue("drugi_vv_vodostaj", postajaElement), 0.0);
                double level3 = parseOrDefault(getTagValue("tretji_vv_vodostaj", postajaElement), 0.0);

                double flow1 = parseOrDefault(getTagValue("prvi_vv_pretok", postajaElement), 0.0);
                double flow2 = parseOrDefault(getTagValue("drugi_vv_pretok", postajaElement), 0.0);
                double flow3 = parseOrDefault(getTagValue("tretji_vv_pretok", postajaElement), 0.0);


                String currentLevelTitle = getTagValue("pretok_znacilni", postajaElement);

                var waterLevel = new WaterLevel();
                waterLevel.setStationId(stationId);
                waterLevel.setName(name);
                waterLevel.setRiver(river);
                waterLevel.setDate(date);
                waterLevel.setLevel(level);
                waterLevel.setFlow(flow);
                waterLevel.setTemperature(temperature);

                waterLevel.setLatitude(latitude);
                waterLevel.setLongitude(longitude);
                waterLevel.setLevel1(level1);
                waterLevel.setLevel2(level2);
                waterLevel.setLevel3(level3);

                waterLevel.setFlow1(flow1);
                waterLevel.setFlow2(flow2);
                waterLevel.setFlow3(flow3);

                waterLevel.setCurrentLevelTitle(currentLevelTitle);
                waterLevel.setMeterBaseline(meterBaseline);

                waterLevel.setType(type);

                latestLevels.add(waterLevel);
            }

            latestLevels.forEach(latestLevel -> {
                WaterLevel existingLevel = waterLevelRepository.findTopByStationIdOrderByDateDesc(latestLevel.getStationId());
                if (existingLevel != null && !latestLevel.getDate().isEqual(existingLevel.getDate())) {
                    waterLevelRepository.save(latestLevel);

                } else if (existingLevel == null) {
                    waterLevelRepository.save(latestLevel);
                }


            });


        } catch (Exception e) {
            log.error("❌ Napaka pri pridobivanju vodostajev: " + e.getMessage(), e);


        }


        //refreshCache
        stationsService.refreshCache();
    }

    private boolean hasTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        return nodeList.getLength() > 0 && nodeList.item(0).getTextContent() != null && !nodeList.item(0).getTextContent().isBlank();
    }

    public List<WaterLevel> getLastXWaterLevelsForStation(String stationId, int pageSize) {
        PageRequest pageRequest = PageRequest.of(0, pageSize);
        return waterLevelRepository.findByStationIdOrderByDateDesc(stationId, pageRequest);

    }

    private String getTagValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : "";
    }

    public List<WaterLevel> getAllWaterLevels(String stationId) {
        return waterLevelRepository.findAllByStationIdOrderByDateDesc(stationId);
    }

    public WaterLevel getCurrentByStationId(String stationId) {
        return waterLevelRepository.findTopByStationIdOrderByDateDesc(stationId);
    }
}
