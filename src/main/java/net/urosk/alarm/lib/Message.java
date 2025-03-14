package net.urosk.alarm.lib;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Message class representing a notification payload.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {

    private String title;       // Title of the message
    private String body;        // Body content of the message
    private String icon;        // Icon URL for the message
    private String badge;       // Badge URL for the message
    private String url;         // URL to open when clicked
    private long timestamp;     // Timestamp when the message was sent

    public Message(String title, String message) {
        this.title = title;
        this.body = message;
        this.icon = "images/icon.png";
        this.badge = "images/badge.png";
        this.url = "https://vodostaj.urosk.net/moji-alarmi";
        this.timestamp = new Date().getTime();
    }

    /**
     * Convenience method to create a payload for the push notification.
     * @return the notification payload in JSON format
     */
    public String toJson() {
        return "{"
                + "\"title\": \"" + title + "\","
                + "\"body\": \"" + body + "\","
                + "\"icon\": \"" + icon + "\","
                + "\"badge\": \"" + badge + "\","
                + "\"url\": \"" + url + "\","
                + "\"timestamp\": " + timestamp
                + "}";
    }
}
