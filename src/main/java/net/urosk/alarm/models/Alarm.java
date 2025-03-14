package net.urosk.alarm.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.urosk.alarm.views.AlarmView;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String stationId;
    private String stationName;

    @Enumerated(EnumType.STRING)
    private AlarmView.NotificationMethod notificationMethod;
    private String chatId;
    private String authKey;
    private String p256dhKey;
    private double alertThresholdLevel=0;
    private double alertThresholdFlow=0;
}
