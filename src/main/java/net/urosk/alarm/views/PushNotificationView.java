package net.urosk.alarm.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import elemental.json.JsonObject;
import elemental.json.JsonString;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import net.urosk.alarm.models.User;
import net.urosk.alarm.services.PushNotificationService;
import net.urosk.alarm.services.UserService;
import net.urosk.alarm.services.UtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


@Route(value = "obvestila", layout = MainLayout.class)
@PermitAll
@JsModule("./js/push-notifications.js") // Pot do JavaScript datoteke
@Slf4j
public class PushNotificationView extends VerticalLayout {
    @Autowired
    PushNotificationService pushNotificationService;
    @Autowired
    UserService userService;


    TextField deviceName = new TextField("Ime naprave");
    @Value("${vapid.public.key}")
    private String publicKey;

    public PushNotificationView(UtilService utilService) {

        setSpacing(true);
        setPadding(true);
        Button subscribeButton = new Button("Registriraj to napravo za obvestila");
        subscribeButton.addClickListener(event -> subscribeToPush());

        Button sendTestButton = new Button("Pošlji testno obvestilo");


        sendTestButton.addClickListener(event -> {
            pushNotificationService.sendPushMessageToUser(userService.getLoggedInUser().getId(), "Vodostaj test", "Test sporočilo iz Urosk.NET vodostaj aplikacije"); // Primer userId = "123"
        });

        deviceName.setValue("Moj telefon");

        add(utilService.getHtmlElementFromMarkdown("push-messages.md"));
        add(deviceName, subscribeButton, sendTestButton);

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

    }

    private void subscribeToPush() {

        // Posredujemo tudi referenco na element te komponente
        UI.getCurrent().getPage().executeJs("window.subscribeToPush($0, $1)", publicKey, getElement());

    }

    @ClientCallable
    public String requestPushSubscription(JsonObject subscriptionData) {
        try {
            User user = userService.getLoggedInUser(); // Pridobite trenutno prijavljenega uporabnika
            if (user == null) {
                return "Uporabnik ni prijavljen";
            }
            String endpoint = ((JsonString) subscriptionData.get("endpoint")).getString();

            JsonObject keys = subscriptionData.getObject("keys");
            String p256dh = ((JsonString) keys.get("p256dh")).getString();
            String auth = ((JsonString) keys.get("auth")).getString();
            pushNotificationService.saveSubscription(user, endpoint, p256dh, auth, deviceName.getValue()

            );
            return "Subscription saved successfully";

        } catch (Exception e) {
            log.error("Error saving subscription", e);
            return "Subscription failed: " + e.getMessage(); // Return a detailed error message
        }
    }


}