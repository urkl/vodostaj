package net.urosk.alarm.lib;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import jakarta.servlet.http.HttpServletRequest;
import net.urosk.alarm.models.Station;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class UiUtils {

    public static final String FLOW_UNIT = "[m3/s]";
    public static final String LEVEL_UNIT = "[cm]";

    public static Component getTitle(String titleString) {
        H2 title = new H2(titleString);
        title.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "bold")
                .set("margin", "1em");
        return title;
    }

    public static String getFormattedDate(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("sl-SI"));
        return date.format(formatter);
    }

    public static Hr hr() {
        Hr hr = new Hr();
        //   hr.getStyle().set("margin", "1em");
        return hr;
    }

    public static String getFormatedNumber(Double number) {
        if (number == null) {
            return "";
        }
        if (number == 0) {
            return "0";
        }
        DecimalFormat decimalFormat = getDecimalFormat();
        return decimalFormat.format(number);
    }

    public static void error(String msg) {

        Notification n = new Notification();
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        n.setDuration(6000);

        Span label = new Span(msg);
        label.getStyle().set("font-size", "var(--lumo-font-size-xl)");
        n.setPosition(Notification.Position.TOP_STRETCH);
        n.add(label);

        n.open();
    }

    public static void success(String msg) {
        Notification n = new Notification();
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        n.setDuration(3000);
        Span label = new Span(msg);
        label.getStyle().set("font-size", "var(--lumo-font-size-xl)");
        n.setPosition(Notification.Position.MIDDLE);
        n.add(label);

        n.open();
    }

    public static void longSuccess(String msg) {
        Notification n = new Notification();
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        n.setDuration(6000);
        Span label = new Span(msg);
        label.getStyle().set("font-size", "var(--lumo-font-size-xl)");
        n.setPosition(Notification.Position.MIDDLE);
        n.add(label);

        n.open();
    }

    public static void info(String msg) {
        Notification n = new Notification();
        n.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        n.setDuration(3000);
        Span label = new Span(msg);
        label.getStyle().set("font-size", "var(--lumo-font-size-xl)");
        n.setPosition(Notification.Position.MIDDLE);
        n.add(label);

        n.open();
    }

    public static void showSuccessNotification(String message) {

        Notification notification = new Notification(message, 3000);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.open();
    }

    public static DecimalFormat getDecimalFormat() {


        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("sl-SI"));

        symbols.setGroupingSeparator(' ');

        return new DecimalFormat("#,##0.00", symbols);
    }





        public static boolean isMobile() {
            HttpServletRequest request = (HttpServletRequest) VaadinService.getCurrentRequest();
            if (request == null) {
                return false;
            }
            String userAgent = request.getHeader("User-Agent");
            return userAgent != null && userAgent.matches(".*(Mobi|Android|iPhone|iPad).*");
        }

    public static FlexLayout flowContainer(Station station) {
        FlexLayout container = new FlexLayout();
        container.setWidthFull();
        container.setFlexWrap(FlexLayout.FlexWrap.WRAP); // Omogoči prelom v vertikalni način, če zmanjka prostora
        container.setJustifyContentMode(FlexLayout.JustifyContentMode.CENTER); // Sredinska poravnava
        container.getElement().getStyle().set("gap", "1em");

        if ("reka".equals(station.getType())) {
            container.add(createBadge(station.getFlow1(), FLOW_UNIT, "Prvi visokovodni pretok (Lokalno razlivanje izven rečnega korita)", "primary"));
            container.add(createBadge(station.getFlow2(), FLOW_UNIT, "Drugi visokovodni pretok (Pojavijo se prve poplave)", "warning"));
            container.add(createBadge(station.getFlow3(), FLOW_UNIT, "Tretji visokovodni pretok (Poplave na širšem območju)", "error"));
        } else if ("jezero".equals(station.getType())) {
            container.add(createBadge(station.getLevel1(), LEVEL_UNIT, "Prvi visokovodni nivo (Lokalno razlivanje izven rečnega korita)", "primary"));
            container.add(createBadge(station.getLevel2(), LEVEL_UNIT, "Drugi visokovodni nivo (Pojavijo se prve poplave)", "warning"));
            container.add(createBadge(station.getLevel3(), LEVEL_UNIT, "Tretji visokovodni nivo (Poplave na širšem območju)", "error"));
        }

        //container.add(new Span("Trenutni pretok: " + getFormatedNumber(station.getTempCurrentFlow()) + " " + FLOW_UNIT));
        //container.add(new Span("Trenutna višina: " + getFormatedNumber(station.getTempCurrentLevel()) + " " + LEVEL_UNIT));

        return container;
    }

    private static Div createBadge(double value, String unit, String description, String theme) {
        Div badge = new Div(new Span(getFormatedNumber(value) + " " + unit + " " + description));
        badge.getElement().getThemeList().add("badge " + theme);

        // CSS za prelom besedila
        badge.getStyle().set("word-wrap", "break-word"); // Prelomi dolg tekst
        badge.getStyle().set("white-space", "normal"); // Dovoli več vrstic
        badge.getStyle().set("max-width", "100%"); // Naj se prilagodi širini ekrana
        badge.getStyle().set("padding", "10px"); // Lepši izgled
        badge.getStyle().set("border-radius", "8px"); // Mehkejši robovi
        badge.getStyle().set("text-align", "center"); // Sredinska poravnava besedila

        return badge;
    }

}

