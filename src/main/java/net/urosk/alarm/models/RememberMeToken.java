package net.urosk.alarm.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "remember_me_tokens")
@Getter
@Setter
public class RememberMeToken {

    @Id
    private String series; // Unikatni identifikator

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String tokenValue;

    @Column(nullable = false)
    private LocalDateTime lastUsed;

    public RememberMeToken() {
    }

    public RememberMeToken(String series, String username, String tokenValue, LocalDateTime lastUsed) {
        this.series = series;
        this.username = username;
        this.tokenValue = tokenValue;
        this.lastUsed = lastUsed;
    }

}
