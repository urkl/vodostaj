package net.urosk.alarm.services;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.webpush.WebPush;
import com.vaadin.flow.server.webpush.WebPushKeys;
import com.vaadin.flow.server.webpush.WebPushMessage;
import com.vaadin.flow.server.webpush.WebPushSubscription;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.urosk.alarm.lib.Message;
import net.urosk.alarm.lib.UiUtils;
import net.urosk.alarm.models.PushSubscription;
import net.urosk.alarm.models.User;
import net.urosk.alarm.repositories.PushSubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@Slf4j
public class WebPushService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    WebPush webPush;
    @Value("${vapid.public.key}")
    private String publicKey;
    @Value("${vapid.private.key}")
    private String privateKey;
    @Value("${vapid.subject}")
    private String subject;

    public WebPushService(PushSubscriptionRepository pushSubscriptionRepository) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
    }

    @PostConstruct
    private void init() {

        webPush = new WebPush(publicKey, privateKey, subject);
    }

    public String getBaseUrl() {
        HttpServletRequest request = VaadinServletRequest.getCurrent();
        if (request != null) {
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int port = request.getServerPort();
            boolean isDefaultPort = (port == 80 && "http".equals(scheme)) ||
                    (port == 443 && "https".equals(scheme));
            return scheme + "://" + serverName + (isDefaultPort ? "" : ":" + port);
        }
        return "Unknown";
    }

    public PushSubscription store(User user, WebPushSubscription subscription, String deviceName) {
        Optional<PushSubscription> existingSubscription = pushSubscriptionRepository.findByUserAndDeviceName(user, deviceName);

        if (existingSubscription.isPresent()) {
            PushSubscription subscriptionToUpdate = existingSubscription.get();
            subscriptionToUpdate.setEndpoint(subscription.endpoint());
            subscriptionToUpdate.setP256dhKey(subscription.keys().p256dh());
            subscriptionToUpdate.setAuthKey(subscription.keys().auth());

           return pushSubscriptionRepository.save(subscriptionToUpdate);
        } else {
            PushSubscription newSubscription = new PushSubscription();
            newSubscription.setUser(user);
            newSubscription.setEndpoint(subscription.endpoint());
            newSubscription.setP256dhKey(subscription.keys().p256dh());
            newSubscription.setAuthKey(subscription.keys().auth());
            newSubscription.setDeviceName(deviceName);
            return pushSubscriptionRepository.save(newSubscription);
        }



    }


    public void sendPushMessageToUser(String userId, String title, String message) {
        List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUser_Id(userId);
        if (subscriptions.isEmpty()) {
            log.info("No subscriptions found for user {}", userId);
            return;
        }

        for (PushSubscription subscriptionData : subscriptions) {
            WebPushKeys webPushKeys = new WebPushKeys(
                    subscriptionData.getP256dhKey(), // Pazljivo: preveri format
                    subscriptionData.getAuthKey()
            );

            WebPushSubscription subscription = new WebPushSubscription(
                    subscriptionData.getEndpoint(), webPushKeys
            );

            Message ms = new Message(title, message);
            ms.setUrl(getBaseUrl() + "/moji-alarmi");

            try {

                WebPushMessage webPushMessage = new WebPushMessage(title, ms.getBody());
                webPush.sendNotification(subscription, webPushMessage);
                log.info("Push notification sent to endpoint: {}", subscription.endpoint());
            } catch (Exception e) {
                log.error("Error sending push notification to endpoint: {}", subscription.endpoint(), e);
                UiUtils.error("Napaka! " + e.getMessage());
            }
        }
    }


    public void deleteSubscription(PushSubscription pushSubscription) {
        unsubscribe(UI.getCurrent(), pushSubscription.getUser(), pushSubscription.getDeviceName());
        pushSubscriptionRepository.delete(pushSubscription);
    }

    public List<PushSubscription> getSubscriptionsForUser(User loggedInUser) {
        return pushSubscriptionRepository.findByUser_Id(loggedInUser.getId());
    }


    public void unsubscribe(UI ui, User loggedInUser, String deviceName) {
        webPush.unsubscribe(ui, receiver -> {
            Optional<PushSubscription> existingSubscription = pushSubscriptionRepository.findByUserAndDeviceName(loggedInUser, deviceName);
            existingSubscription.ifPresent(pushSubscriptionRepository::delete);
        });
    }

    public void subscribe(UI ui, User loggedInUser, String deviceName, Consumer<PushSubscription> callback) {


        webPush.subscribe(ui, receiver -> {
          var s=  store(loggedInUser, receiver, deviceName);
            callback.accept(s);

        });
    }
}
