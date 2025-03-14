package net.urosk.alarm.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Station {

    @Id
    private String id; // Šifra postaje
    private String name; // Ime postaje
    private String river; // Ime reke
    private double latitude; // Geografska širina
    private double longitude;// Geografska dolžina
    private double level1; // Vodostaj
    private double level2; // Vodostaj
    private double level3; // Vodostaj

    private double flow1; // Pretok
    private double flow2; // Pretok
    private double flow3; // Pretok
    private double meterBaseline; // Referenčna kota

    @Transient
    private double tempCurrentFlow; // Pretok
    @Transient
    private double tempCurrentLevel; // Vodostaj
    @Transient
    private List<Double> flowHistory; // Pretok
    private String type; //Reka jezero

    @Transient
    private WaterLevel lastWaterLevel; // Zadnji vodostaj

    public Station(String stationId, String stationName) {
        this.id = stationId;
        this.name = stationName;
    }
}
