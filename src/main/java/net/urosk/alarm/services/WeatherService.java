package net.urosk.alarm.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.urosk.alarm.models.WeatherObservation;
import net.urosk.alarm.repositories.WeatherObservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

@Slf4j
@Service
@Getter
public class WeatherService {

    private String webcamUrlBase;

    private static final String WEATHER_URL = "https://meteo.arso.gov.si/uploads/probase/www/observ/surface/text/sl/observationAms_si_latest.xml";
    // Formatter za čas v formatu "dd.MM.yyyy HH:mm z" npr. "17.03.2025 10:50 CET"
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm z");

    @Autowired
    private WeatherObservationRepository weatherObservationRepository;

    @Scheduled(fixedRate = 1800000)
    public void fetchAndStoreWeatherData() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String xmlData = restTemplate.getForObject(WEATHER_URL, String.class);

            if (xmlData == null) {
                throw new RuntimeException("Ni bilo mogoče pridobiti XML podatkov");
            }

            log.debug("Prejeti XML podatki:\n{}", xmlData.substring(0, Math.min(xmlData.length(), 1000)));

            // Parsiramo in shranimo vsako observacijo posebej
            parseAndSaveObservations(xmlData);

        } catch (Exception e) {
            log.error("Napaka pri pridobivanju podatkov: {}", e.getMessage(), e);
        }
    }
    private Document parseXml(String xmlData) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Pravilno dekodiranje UTF-8
        InputSource is = new InputSource(new StringReader(xmlData));
        is.setEncoding("UTF-8"); // Doda kodiranje UTF-8

        return builder.parse(is);
    }
    private void parseAndSaveObservations(String xmlData) {
        try {
            // Priprava DOM parserja (XML nima namespace‑ov)
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            factory.setNamespaceAware(false);
//            DocumentBuilder builder = factory.newDocumentBuilder();
//            Document doc = builder.parse(new InputSource(new StringReader(xmlData)));
            Document doc = parseXml(xmlData);
            doc.getDocumentElement().normalize();

            // Element <data> vsebuje skupne podatke in elemente <metData> za vsako postajo
            Element dataElem = (Element) doc.getElementsByTagName("data").item(0);
            if (dataElem == null) {
                throw new RuntimeException("Ni najden element <data> v XML.");
            }

            // Skupni podatki iz <data>
            String language = getElementValue(dataElem, "language");
            String credit = getElementValue(dataElem, "credit");
            String creditUrl = getElementValue(dataElem, "credit_url");
            String imageUrl = getElementValue(dataElem, "image_url");
            String suggestedPickup = getElementValue(dataElem, "suggested_pickup");
            String suggestedPickupPeriod = getElementValue(dataElem, "suggested_pickup_period");
            webcamUrlBase = getElementValue(dataElem, "webcam_url_base");
            String iconUrlBase = getElementValue(dataElem, "icon_url_base");
            String iconFormat = getElementValue(dataElem, "icon_format");
            String docsUrl = getElementValue(dataElem, "docs_url");
            String disclaimerUrl = getElementValue(dataElem, "disclaimer_url");
            String copyrightUrl = getElementValue(dataElem, "copyright_url");
            String privacyPolicyUrl = getElementValue(dataElem, "privacy_policy_url");
            String managingEditor = getElementValue(dataElem, "managing_editor");
            String webMaster = getElementValue(dataElem, "web_master");
            String generator = getElementValue(dataElem, "generator");
            String collection = getElementValue(dataElem, "collection");
            String meteosiUrl = getElementValue(dataElem, "meteosi_url");
            String twoDayHistoryUrl = getElementValue(dataElem, "two_day_history_url");

            // Preberemo vse elemente <metData>
            NodeList metDataNodes = dataElem.getElementsByTagName("metData");
            if (metDataNodes.getLength() == 0) {
                throw new RuntimeException("Ni najdenih elementov <metData> v XML.");
            }

            for (int i = 0; i < metDataNodes.getLength(); i++) {
                Element metDataElem = (Element) metDataNodes.item(i);
                WeatherObservation observation = new WeatherObservation();
                logXmlElement(metDataElem);

                // Nastavimo skupne vrednosti
//                observation.setLanguage(language);
//                observation.setCredit(credit);
//                observation.setCreditUrl(creditUrl);
//                observation.setImageUrl(imageUrl);
//                observation.setSuggestedPickup(suggestedPickup);
//                observation.setSuggestedPickupPeriod(suggestedPickupPeriod);
             //   observation.setWebcamUrlBase(webcamUrlBase);
//                observation.setIconUrlBase(iconUrlBase);
//                observation.setIconFormat(iconFormat);
//                observation.setDocsUrl(docsUrl);
//                observation.setDisclaimerUrl(disclaimerUrl);
//                observation.setCopyrightUrl(copyrightUrl);
//                observation.setPrivacyPolicyUrl(privacyPolicyUrl);
//                observation.setManagingEditor(managingEditor);
//                observation.setWebMaster(webMaster);
//                observation.setGenerator(generator);
//                observation.setCollection(collection);
//                observation.setMeteosiUrl(meteosiUrl);
//                observation.setTwoDayHistoryUrl(twoDayHistoryUrl);

                // Podatki o postaji in meritvah iz <metData>
                observation.setStationName(getElementValue(metDataElem, "domain_title"));
                observation.setStationTitle(getElementValue(metDataElem, "domain_longTitle"));
                observation.setStationId(getElementValue(metDataElem, "domain_meteosiId"));
                observation.setLatitude(parseDouble(getElementValue(metDataElem, "domain_lat")));
                observation.setLongitude(parseDouble(getElementValue(metDataElem, "domain_lon")));
                observation.setAltitude(parseDouble(getElementValue(metDataElem, "domain_altitude")));


                // Čas opazovanja: iz elementa <tsValid_issued> v formatu "dd.MM.yyyy HH:mm z"
                String tsValidIssued = getElementValue(metDataElem, "tsValid_issued_RFC822");
                if (tsValidIssued != null) {
                    try {
                        observation.setObservationTime(LocalDateTime.from(TIME_FORMATTER.parse(tsValidIssued)));
                    } catch (Exception e) {
                        // Fallback, če parsing s časovnim pasom ne uspe
                        if (tsValidIssued.length() >= 16) {
                            DateTimeFormatter fallbackFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm");
                       //     observation.setObservationTime(LocalDateTime.parse(tsValidIssued.substring(0, 16), fallbackFormatter));
                            observation.setObservationTime(parseObservationTime(getElementValue(metDataElem, "tsValid_issued_RFC822")));

                        }
                    }
                }

                // Parsiranje časa veljavnosti
            //    observation.setValidStart(parseDate(getElementValue(metDataElem, "validStart")));
              //  observation.setValidEnd(parseDate(getElementValue(metDataElem, "validEnd")));

                // Parsiranje webcam URL-jev po view-direction atributu
                NodeList webcamNodes = metDataElem.getElementsByTagName("webcam");
                for (int j = 0; j < webcamNodes.getLength(); j++) {
                    Element webcamElem = (Element) webcamNodes.item(j);
                    String direction = webcamElem.getAttribute("view-direction");
                    String webcamUrl = webcamElem.getTextContent();

                    switch (direction) {
                        case "e":
                            observation.setWebcamEast(webcamUrl);
                            break;
                        case "w":
                            observation.setWebcamWest(webcamUrl);
                            break;
                        case "n":
                            observation.setWebcamNorth(webcamUrl);
                            break;
                        case "s":
                            observation.setWebcamSouth(webcamUrl); // Če obstaja pogled na jug
                            break;
                    }
                }


                observation.setTemperature(parseDouble(getElementValue(metDataElem, "t")));
                observation.setDewPoint(parseDouble(getElementValue(metDataElem, "td")));
                observation.setHumidity(parseInteger(getElementValue(metDataElem, "rh")));
                observation.setPressure(parseDouble(getElementValue(metDataElem, "P")));
                observation.setWindSpeed(parseDouble(getElementValue(metDataElem, "ff")));
                observation.setWindGust(parseDouble(getElementValue(metDataElem, "fg")));
                observation.setWindDirection(getElementValue(metDataElem, "dd_val"));
                observation.setCloudCover(getElementValue(metDataElem, "nn_var_desc"));
                observation.setPrecipitation(parseDouble(getElementValue(metDataElem, "rr_val")));
                observation.setSolarIrradiance(parseDouble(getElementValue(metDataElem, "S")));
                observation.setSnowCoverHeight(parseDouble(getElementValue(metDataElem, "SN")));


                // Preverimo, ali ta zapis že obstaja v bazi
                Optional<WeatherObservation> existingObservation = weatherObservationRepository
                        .findByStationIdAndObservationTime(observation.getStationId(), observation.getObservationTime());

                if (existingObservation.isPresent()) {
                    log.info("⏭️ Podatki za postajo {} ob {} že obstajajo. Preskakujem shranjevanje.",
                            observation.getStationId(), observation.getObservationTime());
                    continue; // Preskočimo shranjevanje
                }

                // Shrani entiteto za to postajo
                weatherObservationRepository.save(observation);
                log.info("Shranjena observacija za postajo {}: {}", observation.getStationId(), observation);
            }
        } catch (Exception e) {
            log.error("Napaka pri parsanju XML-ja: {}", e.getMessage(), e);
        }
    }

    private String getElementValue(Element parent, String tagName) {
        NodeList nl = parent.getElementsByTagName(tagName);
        if (nl.getLength() > 0) {
            try {
                return new String(nl.item(0).getTextContent().trim().getBytes("ISO-8859-1"), "UTF-8");
            } catch (Exception e) {
                log.warn("❌ Napaka pri dekodiranju UTF-8: {}", e.getMessage());
            }
        }
        return null;
    }


    private Double parseDouble(String value) {

        try {
            return value != null ? Double.parseDouble(value) : null;
        } catch (NumberFormatException e) {
            log.warn("Napaka pri pretvorbi v double: {}", value);
            return null;
        }
    }
    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm z");
            return LocalDateTime.from(formatter.parse(dateStr));
        } catch (Exception e) {
            log.warn("❌ Napaka pri parsiranju datuma: {} -> {}", dateStr, e.getMessage());
            return null;
        }
    }
    private Integer parseInteger(String value) {
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            log.warn("Napaka pri pretvorbi v integer: {}", value);
            return null;
        }
    }

    public List<WeatherObservation> getWeatherObservations() {
        return weatherObservationRepository.findAll();
    }

    private static final Map<String, String> TIMEZONE_MAP = new HashMap<>();

    static {
        TIMEZONE_MAP.put("CET", "Europe/Paris");
        TIMEZONE_MAP.put("CEST", "Europe/Paris");
        TIMEZONE_MAP.put("UTC", "UTC");
    }

    private void logXmlElement(Element element) {
        try {
            StringBuilder formattedXml = new StringBuilder();
            formattedXml.append("\n📌 Obdelujem XML element: <").append(element.getTagName()).append(">\n");

            // Pridobi vse child elemente
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i) instanceof Element) {
                    Element child = (Element) childNodes.item(i);
                    String tagName = child.getTagName();
                    String value = child.getTextContent().trim();

                    if (!value.isEmpty()) {
                        formattedXml.append("   📍 <").append(tagName).append(">: ").append(value).append("\n");
                    }
                }
            }

            formattedXml.append("</").append(element.getTagName()).append(">\n");
            log.info(formattedXml.toString());

        } catch (Exception e) {
            log.warn("❌ Napaka pri logiranju XML elementa: {}", e.getMessage());
        }
    }


    public List<WeatherObservation> findLatestObservations() {
        return weatherObservationRepository.findLatestObservations();
    }



    private LocalDateTime parseObservationTime(String rfc822TimeString) {
        if (rfc822TimeString == null || rfc822TimeString.trim().isEmpty()) {
            return null;
        }

        try {
            // RFC 822 format primer: "18 Mar 2025 06:50:00 +0000"
            DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

            // Parsiramo v ZonedDateTime (ki že vsebuje časovni pas)
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(rfc822TimeString, formatter);

            // Pretvorimo v UTC in vrnemo LocalDateTime
            return zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

        } catch (Exception e) {
            log.warn("❌ Napaka pri parsiranju RFC 822 datuma: '{}' -> {}", rfc822TimeString, e.getMessage());
            return null;
        }
    }
}
