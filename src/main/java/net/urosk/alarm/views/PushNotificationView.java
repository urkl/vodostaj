package net.urosk.alarm.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import net.urosk.alarm.models.PushSubscription;
import net.urosk.alarm.services.UserService;
import net.urosk.alarm.services.UtilService;
import net.urosk.alarm.services.WebPushService;
import org.springframework.beans.factory.annotation.Value;


@Route(value = "obvestila", layout = MainLayout.class)
@PermitAll
//@JsModule("./js/push-notifications.js") // Pot do JavaScript datoteke
@Slf4j
public class PushNotificationView extends VerticalLayout {

    private final WebPushService webPushService;

    private final UserService userService;


    TextField deviceName = new TextField("Ime naprave");
    Grid<PushSubscription> grid = new Grid<>();
    @Value("${vapid.public.key}")
    private String publicKey;

    public PushNotificationView(UtilService utilService, WebPushService webPushService, UserService userService) {

        this.webPushService = webPushService;
        this.userService = userService;


        setSpacing(true);
        setPadding(true);
        Button subscribeButton = new Button("Registriraj to napravo za obvestila");
        subscribeButton.addClickListener(event -> subscribeToPush());

        Button sendTestButton = new Button("Pošlji testno obvestilo");
        sendTestButton.addClickListener(event -> {
            webPushService.sendPushMessageToUser(userService.getLoggedInUser().getId(), "Vodostaj test", "Test sporočilo iz Urosk.NET vodostaj aplikacije"); // Primer userId = "123"

        });

        deviceName.setValue("Moj telefon");

        add(utilService.getHtmlElementFromMarkdown("push-messages.md"));


        add(new H4("Moje naprave"));

        grid.addColumn(PushSubscription::getDeviceName).setHeader("Naprava");
        grid.addComponentColumn((ValueProvider<PushSubscription, Component>) pushSubscription -> {
            Button delete = new Button("Izbriši");
            delete.addClickListener(event -> {
                webPushService.deleteSubscription(pushSubscription);
                refreshGrid();
            });
            return delete;
        }).setAutoWidth(false).setFlexGrow(0);
        refreshGrid();

        add(deviceName, subscribeButton, sendTestButton, grid);

    }

    void refreshGrid() {
        grid.setItems(webPushService.getSubscriptionsForUser(userService.getLoggedInUser()));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

    }

    private void subscribeToPush() {

        UI.getCurrent().getUI().ifPresent(ui ->{
            webPushService.subscribe(ui, userService.getLoggedInUser(), deviceName.getValue(),p->refreshGrid());
        });




    }


}