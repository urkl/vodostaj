package net.urosk.alarm.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import net.urosk.alarm.services.SendGridService;
import net.urosk.alarm.services.UserService;
import net.urosk.alarm.services.UtilService;

@PageTitle("Vodostaj")
@PermitAll
@Route(value = "", layout = MainLayout.class)  // <--- DODANO layout
public class DashboardView extends AbstractView {


    public DashboardView(UtilService utilService, UserService userService , SendGridService sendGridService) {

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setMargin(true);

        add(utilService.getHtmlElementFromMarkdown("dashboard.md"));


        TextArea mailMeForMoreInfo = new TextArea();
        mailMeForMoreInfo.setWidth("100%");
        mailMeForMoreInfo.setLabel("Za več informacij mi pišite na:");
        Button sendMailButton = new Button("Pošlji");
        sendMailButton.addClickListener(event -> {

            if(mailMeForMoreInfo.isEmpty()){
                Notification notification = new Notification("Vnesite vaše vprašanje.", 3000);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
                return;
            }

            sendGridService.sendEmail(userService.getLoggedInUser().getEmail(), "Vprašanje glede vodostaja", mailMeForMoreInfo.getValue());
            Notification notification = new Notification("Vaše vprašanje je bilo poslano.", 3000);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.open();
            mailMeForMoreInfo.clear();
        });
        sendMailButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(mailMeForMoreInfo, sendMailButton);


    }


}
