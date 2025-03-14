package net.urosk.alarm.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "remember_me_tokens")
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

    // Getterji in setterji
    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTokenValue() { return tokenValue; }
    public void setTokenValue(String tokenValue) { this.tokenValue = tokenValue; }

    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
}
