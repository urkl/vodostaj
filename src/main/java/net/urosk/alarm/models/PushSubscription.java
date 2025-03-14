package net.urosk.alarm.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "deviceName"}) // user_id je ime stolpca v bazi za User, deviceName je ime stolpca za deviceName
        }
)
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String endpoint;
    private String p256dhKey;
    private String authKey;

    private String deviceName;
}