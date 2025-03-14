package net.urosk.alarm.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import net.urosk.alarm.lib.Message;
import net.urosk.alarm.models.PushSubscription;
import net.urosk.alarm.models.User;
import net.urosk.alarm.repositories.PushSubscriptionRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Urgency;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;
import java.util.Optional;

@Service
public class PushNotificationService {
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
    private final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Value("${vapid.public.key}")
    private String publicKey;
    @Value("${vapid.private.key}")
    private String privateKey;

    public PushNotificationService(PushSubscriptionRepository pushSubscriptionRepository) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        // Register Bouncy Castle provider if not already registered
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }


    ObjectMapper mapper = new ObjectMapper();


    public void saveSubscription(User user, String endpoint, String p256dhKey, String authKey, String deviceName) {
        Optional<PushSubscription> existingSubscription = pushSubscriptionRepository.findByUserAndDeviceName(user, deviceName);

        if (existingSubscription.isPresent()) {
            // Naročnina za tega uporabnika in napravo že obstaja - posodobite obstoječo
            PushSubscription subscriptionToUpdate = existingSubscription.get();
            subscriptionToUpdate.setEndpoint(endpoint);
            subscriptionToUpdate.setP256dhKey(p256dhKey);
            subscriptionToUpdate.setAuthKey(authKey);
            // deviceName ostane enak, saj je pogoj za unikatnost

            pushSubscriptionRepository.save(subscriptionToUpdate);

        } else {
            // Naročnina za tega uporabnika in napravo še ne obstaja - ustvarite novo
            PushSubscription newSubscription = new PushSubscription();
            newSubscription.setUser(user);
            newSubscription.setEndpoint(endpoint);
            newSubscription.setP256dhKey(p256dhKey);
            newSubscription.setAuthKey(authKey);
            newSubscription.setDeviceName(deviceName);

            pushSubscriptionRepository.save(newSubscription);

        }
    }


    public void sendPushMessageToUser(String userId, String title, String message) {
        List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUser_Id(userId);
        if (subscriptions.isEmpty()) {
            logger.info("No subscriptions found for user {}", userId);
            return;
        }

        PushService pushService;
        try {
            pushService = new PushService(publicKey, privateKey);
        } catch (GeneralSecurityException e) {
            logger.error("Error initializing PushService", e);
            return;
        }

        for (PushSubscription subscriptionData : subscriptions) {
            Subscription subscription = new Subscription(
                    subscriptionData.getEndpoint(),
                    new Subscription.Keys(subscriptionData.getP256dhKey(), subscriptionData.getAuthKey())
            );
            Message ms= new Message(title, message);
            ms.setUrl(getBaseUrl()+ "/moji-alarmi");

            try {
                String msg = mapper.writeValueAsString(ms);
                Notification notification = new Notification(subscription, msg, Urgency.HIGH);

                pushService.send(notification);
                logger.info("Push notification sent to endpoint: {}", subscription.endpoint);
            } catch (Exception e) {
                logger.error("Error sending push notification to endpoint: {}", subscription.endpoint, e);
            }

        }
    }
}