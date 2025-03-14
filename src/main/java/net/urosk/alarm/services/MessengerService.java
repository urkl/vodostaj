package net.urosk.alarm.services;

import net.urosk.alarm.models.Alarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MessengerService {

    RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private SendGridService sendGridService;
    @Autowired
    PushNotificationService pushNotificationService;
    @Value("${telegram.bot.token}")
    private String BOT_TOKEN;


    public void sendNotification(Alarm alarm, String message) {

        switch(alarm.getNotificationMethod()) {
            case telegram:
                sendTelegram(alarm.getChatId(), message);
                break;
            case mobile:
                sendMobile(alarm, message);
                break;
            case email:
                sendEmail(alarm, message);
                break;
            default:
                System.err.println("Unsupported notification method: " + alarm.getNotificationMethod());
        }


    }

    private void sendEmail(Alarm alarm, String message) {

        sendGridService.sendEmail(alarm.getChatId(), message, message);

    }

    public void sendTelegram(String chatId, String message) {
        String telegramApiUrl = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
        String requestUrl = telegramApiUrl + "?chat_id=" + chatId + "&text=" + message;

        restTemplate.getForObject(requestUrl, String.class);
    }

    /**
     * Pošlje push obvestilo na mobilne naprave in brskalnike prek Firebase Cloud Messaging (FCM).
     * Predpostavlja, da so v konfiguraciji MOBILE_PUSH_API_URL in MOBILE_PUSH_API_KEY pravilno nastavljeni.
     *
     * @param deviceToken registracijski token naprave
     * @param message     vsebina sporočila
     */
    private void sendMobile(Alarm alarm, String message) {


        pushNotificationService.sendPushMessageToUser(alarm.getUser().getId(), "Alarm "+alarm.getStationName(), message);
    }
}
