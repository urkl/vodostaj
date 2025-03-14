package net.urosk.alarm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WaterLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; //id
    private String stationId; // Šifra postaje
    private String name; // Ime postaje
    private String river; // Ime reke
    @Column(columnDefinition = "TIMESTAMP") // Izbira ustrezne SQL vrste
    private LocalDateTime date; // Datum meritve
    private double level; // Vodostaj
    private double flow; // Pretok
    private double temperature; // Temperatura

    private double latitude; // Geografska širina
    private double longitude;// Geografska dolžina
    private double level1; // Vodostaj
    private double level2; // Vodostaj
    private double level3; // Vodostaj

    private double flow1; // Pretok
    private double flow2; // Pretok
    private double flow3; // Pretok

    private String currentLevelTitle; // Trenutni vodostaj

    private double meterBaseline; // Referenčna kota

    private String type; //Reka jezero

}