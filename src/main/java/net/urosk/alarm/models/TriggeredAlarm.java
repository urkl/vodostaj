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
public class TriggeredAlarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference na Alarm, ki se je sprožil
    @ManyToOne
    private Alarm alarm;

    // Čas, ko se je alarm sprožil
    private LocalDateTime triggeredAt;

    // Morda zabeležimo nivo, ki je povzročil alarm
    private double measuredValue;

    // Poljubno dodatno polje za opis ali beležko
    private String note;

    private String error;
}
