package net.urosk.alarm.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "weather_observations")
public class WeatherObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primarni ključ

    // Globalni meta podatki
    private String messageIdentifier; // Identifikator sporočila (npr. observationAms_si_latest)
    private LocalDateTime sentTime;   // Čas pošiljanja sporočila
    private String source;            // Vir podatkov (npr. ARSO)
    private String scope;             // Obseg podatkov (npr. površinske meritve)

    // Podatki postaje
    private String stationId;         // ID postaje (enolični identifikator meteorološke postaje)
    private String stationName;       // Ime postaje
    private String stationTitle;      // Naslov postaje
    private Double latitude;          // Zemljepisna širina postaje
    private Double longitude;         // Zemljepisna dolžina postaje
    private Double altitude;          // Nadmorska višina postaje (v metrih)

    // Podatki opazovanja
    private LocalDateTime observationTime; // Čas opazovanja (datum in ura meritve)

    private Double temperature;       // Temperatura (°C)
    private Integer humidity;         // Vlažnost (%)
    private String windDirection;    // Smer vetra (stopinje)
    private Double windSpeed;         // Hitrost vetra (km/h)
    private Double windGust;          // Sunki vetra (km/h)
    private Double pressure;          // Zračni tlak (hPa)
    private Double precipitation;     // Padavine (mm)
    private String cloudCover;        // Ocenjena oblačnost (odstotki)
    private Double solarIrradiance;   // Sončno obsevanje (W/m²)
    private Double snowCoverHeight;   // Višina snežne odeje (cm)
    private Double dewPoint;          // Temperatura rosišča (°C)

    private LocalDateTime validStart;
    private LocalDateTime validEnd;
    private String webcamEast;
    private String webcamWest;
    private String webcamNorth;
    private String webcamSouth;

}
