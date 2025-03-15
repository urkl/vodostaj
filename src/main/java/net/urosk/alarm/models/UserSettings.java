package net.urosk.alarm.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String settingKey; // Ključ nastavitve

    private String settingValueString;
    private Boolean settingValueBoolean;
    private Double settingValueNumber; // Število (uporabimo Double, da podpira vse tipe števil)

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
