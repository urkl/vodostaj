package net.urosk.alarm.views;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import net.urosk.alarm.components.Broadcaster;
import net.urosk.alarm.config.GlobalErrorHandler;
import net.urosk.alarm.services.UserService;

import java.util.HashMap;
import java.util.Map;

public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final H3 title;
    private final Map<Class<?>, String> titleMap = new HashMap<>();

    public MainLayout(UserService userService) {
        this.userService = userService;
        this.title = new H3("Alarmi");

        VaadinSession.getCurrent().setErrorHandler(new GlobalErrorHandler());
        Broadcaster.register(userService.getLoggedInUser().getId(), UI.getCurrent(), this::showAlertNotification);

        createHeader();
        setupTitleMap();

        // Registracija Service Workerja
        UI.getCurrent().getPage().executeJs(
                "if ('serviceWorker' in navigator) {" +
                        "    navigator.serviceWorker.register('/sw.js')" +
                        "    .then(function(registration) {" +
                        "        console.log('Service Worker registered with scope:', registration.scope);" +
                        "    })" +
                        "    .catch(function(err) {" +
                        "        console.error('Service Worker registration failed:', err);" +
                        "    });" +
                        "}"
        );

        UI.getCurrent().getPage().executeJs(
                "if (Notification.permission !== 'granted') {" +
                        "   Notification.requestPermission();" +
                        "}"
        );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (userService.getLoggedInUser() != null) {
            Broadcaster.unregister(userService.getLoggedInUser().getId());
        }
    }

    public void showAlertNotification(String userId, String message) {
        Notification notification = new Notification();
        notification.setPosition(Notification.Position.TOP_START);
        notification.add(new Span("📢 " + message));
        notification.setDuration(10000);
        notification.setAssertive(true);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.open();
    }

    private void createHeader() {
        Avatar avatar = new Avatar();
        avatar.setName(userService.getLoggedInUser().getName());

        Button logoutButton = new Button("Odjava", event -> userService.logout());
        logoutButton.getStyle()
                .set("background-color", "#FF6347")
                .set("color", "white")
                .set("padding", "10px 20px")
                .set("border-radius", "5px")
                .set("margin-right", "10px")
                .set("font-size", "1.2rem");

        HorizontalLayout header = new HorizontalLayout( );

        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getElement().setAttribute("theme", "dark");
        header.setPadding(true);
        header.setSpacing(true);
        header.setMargin(false);
        header.add(avatar, title);

        SideNav nav = new SideNav();
        nav.setClassName(LumoUtility.Padding.SMALL);
        nav.getElement().setAttribute("theme", "dark");

        SideNavItem item0 = new SideNavItem("Vodostaj", DashboardView.class);
        nav.addItem(item0);


        SideNavItem item1 = new SideNavItem("Moji alarmi", AlarmView.class);
        item1.getElement().setAttribute("title", "Nastavi ali spremeni alarme za vodostaje rek.");

        nav.addItem(item1);

        SideNavItem item3 = new SideNavItem("Moje reke", MyStationView.class);
        item3.getElement().setAttribute("title", "Shrani svoje najljubše reke za hitrejši dostop.");

        nav.addItem(item3);

        SideNavItem item4 = new SideNavItem("Zemljevid", MapView.class);
        item4.getElement().setAttribute("title", "Oglej si vodostaje rek na zemljevidu.");

        nav.addItem(item4);

        SideNavItem item5 = new SideNavItem("Vse reke", LargeFlowView.class);
        item5.getElement().setAttribute("title", "Preglej vodostaje vseh rek na enem mestu.");

        nav.addItem(item5);

        SideNavItem item6 = new SideNavItem("Obvestila", PushNotificationView.class);
        item6.getElement().setAttribute("title", "Omogoči push obvestila za pravočasna opozorila.");

        nav.addItem(item6);

        SideNavItem item2 = new SideNavItem("Sproženi alarmi", TriggeredAlarmView.class);
        item2.getElement().setAttribute("title", "Preglej katere tvoje reke so presegle nastavljene nivoje.");

        nav.addItem(item2);



        nav.addItem(new SideNavItem(""));
        SideNavItem helpItem = new SideNavItem("Pomoč", HelpView.class, new RouteParameters("md", "help"));
        helpItem.getElement().setAttribute("title", "Kako uporabljati aplikacijo Vodostaj.");

        nav.addItem(helpItem);

        SideNavItem telegramItem = new SideNavItem("Telegram", HelpView.class, new RouteParameters("md", "telegram_user_guide"));
        telegramItem.getElement().setAttribute("title", "Kako prejemati obvestila prek Telegrama.");

        nav.addItem(telegramItem);


        nav.addItem(new SideNavItem(""));
        nav.addItem(new SideNavItem("","/login?logout", logoutButton));

        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("theme", "dark");

        toggle.getElement().setAttribute("theme", "dark");
        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);
        scroller.getElement().setAttribute("theme", "dark");

        addToDrawer(scroller);
        addToNavbar(toggle, header);
        setPrimarySection(Section.DRAWER);
    }

    private void setupTitleMap() {
        titleMap.put(AlarmView.class, "Moji alarmi");
        titleMap.put(TriggeredAlarmView.class, "Sproženi alarmi");
        titleMap.put(MyStationView.class, "Moje reke");
        titleMap.put(MapView.class, "Zemljevid");
        titleMap.put(LargeFlowView.class, "Vse reke");
        titleMap.put(HelpView.class, "Pomoč");
        titleMap.put(PushNotificationView.class, "Obvestila");
        titleMap.put(DashboardView.class, "Vodostaj");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String newTitle = titleMap.getOrDefault(event.getNavigationTarget(), "Alarmi");
        title.setText(newTitle);
    }
}
