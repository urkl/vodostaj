package net.urosk.alarm.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import net.urosk.alarm.components.ClientTimer;
import net.urosk.alarm.components.WaterLevelsComponent;
import net.urosk.alarm.lib.UiUtils;
import net.urosk.alarm.models.Alarm;
import net.urosk.alarm.models.Station;
import net.urosk.alarm.services.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.List;
import java.util.Locale;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_SUCCESS;
import static net.urosk.alarm.lib.UiUtils.*;
import static net.urosk.alarm.views.AlarmView.NotificationMethod.email;
import static net.urosk.alarm.views.AlarmView.NotificationMethod.telegram;


@PageTitle("Moji alarmi")
@PermitAll
@Route(value = "moji-alarmi", layout = MainLayout.class)  // <--- DODANO layout
public class AlarmView extends AbstractView {
    private static final String ICON_SIZE = "32px";
    private static final String ICON_COLOR = "var(--lumo-body-text-color)";
    private final AlarmService alarmService;
    private final UserService userService;
    private final Grid<Alarm> alarmGrid;
    private final WaterLevelService waterLevelService;
    private final MessengerService messengerService;
    private final StationsService stationsService;
    private final Div levelHelpContainer = new Div();
    VerticalLayout waterLevelsContainer = new VerticalLayout();
    ComboBox<Station> waterLevelComboBox = new ComboBox<>("Vodostaj");
    RadioButtonGroup<NotificationMethod> notificationMethodGroup = new RadioButtonGroup<>();
    RadioButtonGroup<String> alarmTypeGroup = new RadioButtonGroup<>();
    NumberField alarmThresholdField = new NumberField("Nivo alarma");
    TextField token = new TextField("Token");

    public AlarmView(AlarmService alarmService, UserService userService, StationsService stationsService, WaterLevelService waterLevelService, MessengerService messengerService) {
        this.alarmService = alarmService;
        this.userService = userService;
        this.stationsService = stationsService;
        this.waterLevelService = waterLevelService;
        this.messengerService = messengerService;
        getElement().setAttribute("theme", "light");
        setSizeFull();

        setPadding(true);
        setSpacing(false);

        alarmGrid = new Grid<>(Alarm.class, false);

        waterLevelComboBox.setWidth("20%");
        waterLevelComboBox.setItemLabelGenerator(Station::getName);
        waterLevelComboBox.setPlaceholder("Izberi vodostaj");
        List<Station> stations = stationsService.getAllStations();
        waterLevelComboBox.setItems(stations);
        waterLevelComboBox.addValueChangeListener(event -> {

            if (event.getValue() != null) {
                levelHelpContainer.removeAll();
                Station station = event.getValue();
                VerticalLayout vl = new VerticalLayout();
                var hl = flowContainer(station);
                vl.setPadding(true);
                vl.add(hl);

                levelHelpContainer.add(vl);
            } else {
                levelHelpContainer.removeAll();
            }
        });

        alarmTypeGroup.setLabel("Izberi tip alarma:");
        alarmTypeGroup.setItems("level", "flow");
        alarmTypeGroup.setValue("level");
        alarmTypeGroup.setItemLabelGenerator(item -> switch (item) {
            case "level" -> "Višina [cm]";
            case "flow" -> "Pretok [m³/s]";
            default -> "Unknown";
        });

        notificationMethodGroup.setLabel("Izberi način obveščanja:");
        notificationMethodGroup.setItems(NotificationMethod.values());


        notificationMethodGroup.setItemLabelGenerator(item -> switch (item) {
            case telegram -> "Telegram";
            case mobile -> "Mobilne naprave in brskalniki";
            case email -> "Email";
            default -> "Unknown";
        });
        notificationMethodGroup.setValue(telegram);
        notificationMethodGroup.setRenderer(new ComponentRenderer<>(item -> {
            SvgIcon icon = switch (item) {
                case telegram -> LineAwesomeIcon.TELEGRAM.create();
                case mobile -> LineAwesomeIcon.MOBILE_SOLID.create();
                case email -> LineAwesomeIcon.ENVELOPE.create();
                default -> LineAwesomeIcon.QUESTION_CIRCLE.create();
            };
            icon.setSize("30px");
            icon.setTooltipText(item.name());
            return icon;
        }));
        notificationMethodGroup.addValueChangeListener(event -> {

            token.setReadOnly(false);
            if (event.getValue() == telegram) {
                token.setPlaceholder("Vnesi svoj Telegram token");
            } else if (event.getValue() == email) {
                token.setPlaceholder("Vnesi svoj email");
            } else {
                token.setPlaceholder("Token se pridobi avtomatsko");
                token.setReadOnly(true);
            }
        });


        token.setPlaceholder("Vnesi svoj token / email");
        token.setRequiredIndicatorVisible(true);


        alarmThresholdField.setPlaceholder("Vnesi nivo alarma (npr. 300)");


        Button saveButtonAsNew = new Button("Shrani kot Novega", event -> save(true));

        HorizontalLayout buttonLayout = new HorizontalLayout( saveButtonAsNew);
        buttonLayout.setWidthFull(); // Zavzame širino FormLayout, a gumb ne
        buttonLayout.setJustifyContentMode(JustifyContentMode.START); // Gumb poravna desno


        saveButtonAsNew.setWidth("auto");
        saveButtonAsNew.addThemeVariants(LUMO_SUCCESS, LUMO_PRIMARY);
        FormLayout inputLayout = new FormLayout();

        inputLayout.setResponsiveSteps(
                // V širini 0px (najmanjši ekrani) bo vse v eni koloni
                new FormLayout.ResponsiveStep("0", 1),
                // Nad 600px se bo obrazec razdelil v dve koloni
                new FormLayout.ResponsiveStep("600px", 2),
                // Nad 900px pa v tri (ali štiri) kolone
                new FormLayout.ResponsiveStep("900px", 20));

        inputLayout.add(waterLevelComboBox, 4);
        inputLayout.add(alarmTypeGroup, 2);
        inputLayout.add(alarmThresholdField, 2);
        inputLayout.add(notificationMethodGroup, 2);
        inputLayout.add(token, 4);
        inputLayout.add(buttonLayout, 4);


        alarmGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        alarmGrid.addColumn(Alarm::getStationName).setHeader("Vodostaj").setFlexGrow(1).setResizable(true);
        alarmGrid.addColumn(new TextRenderer<>(e -> e.getAlertThresholdLevel() != 0 ? "Višina: " + UiUtils.getFormatedNumber(e.getAlertThresholdLevel()) + " cm" : "Pretok: " + UiUtils.getFormatedNumber(e.getAlertThresholdFlow()) + " m³/s")).setAutoWidth(true).setHeader("Nivo alarma").setFlexGrow(1).setResizable(true);
        alarmGrid.addItemClickListener(event -> edit(event.getItem()));
        alarmGrid.addComponentColumn(alarm -> {
            NotificationMethod notificationMethod = alarm.getNotificationMethod();
            if (notificationMethod == null) {
                return new H2(); // ali prazna komponenta
            }

            switch (notificationMethod) {
                case telegram -> {
                    SvgIcon svgIcon = LineAwesomeIcon.TELEGRAM.create();
                    svgIcon.setSize(ICON_SIZE);
                    svgIcon.getStyle().set("color", ICON_COLOR);

                    svgIcon.setTooltipText("Telegram");
                    return svgIcon;
                }
                case mobile -> {
                    SvgIcon svgIcon = LineAwesomeIcon.MOBILE_SOLID.create();
                    svgIcon.setSize(ICON_SIZE);
                    svgIcon.getStyle().set("color", ICON_COLOR);

                    svgIcon.setTooltipText("Mobilne naprave in brskalniki");
                    return svgIcon;
                }

                case email -> {
                    SvgIcon svgIcon = LineAwesomeIcon.ENVELOPE.create();
                    svgIcon.setSize(ICON_SIZE);
                    svgIcon.getStyle().set("color", ICON_COLOR);

                    svgIcon.setTooltipText("Email");
                    return svgIcon;
                }
                default -> {
                    H2 text = new H2(StringUtils.capitalize(notificationMethod.name()));
                    text.getStyle().set("color", ICON_COLOR);
                    return text;
                }
            }
        }).setHeader("Metoda obveščanja").setFlexGrow(1).setResizable(true);
        alarmGrid.addColumn(Alarm::getChatId).setHeader("Token/Email").setFlexGrow(1).setResizable(true);
        alarmGrid.addComponentColumn(this::createActions).setHeader("Dejanja").setFlexGrow(1).setResizable(true);
        alarmGrid.setWidthFull();
        //alarmGrid.setHeight("50%");

        alarmGrid.addSelectionListener(event -> {

            if (event.getFirstSelectedItem().isPresent()) {

                showWaterLevels(event.getFirstSelectedItem().get());
            } else {
                waterLevelsContainer.removeAll();
            }
        });
        refreshAlarms();

        //waterLevelsContainer.setWidthFull();
        waterLevelsContainer.setSizeFull();

        waterLevelsContainer.setPadding(false);
        waterLevelsContainer.setSpacing(false);

        waterLevelsContainer.setVisible(false);
        add(hr(), inputLayout, levelHelpContainer, alarmGrid, waterLevelsContainer);

        ClientTimer timer = new ClientTimer(60, () -> alarmGrid.getSelectedItems().stream().findFirst().ifPresent(this::showWaterLevels));
        add(timer);

        // Glavni grid
        alarmGrid.setSizeFull();
        alarmGrid.setMinHeight("300px");


// Grid, ki se odpre na klik
        waterLevelsContainer.setSizeFull();


        //alarmGrid.setAllRowsVisible(true);
        // waterLevelsContainer.setAllRowsVisible(true);
    }

    public void save(boolean asNew) {
        Station selectedWaterLevel = waterLevelComboBox.getValue();
        Double alarmThresholdValue = alarmThresholdField.getValue();


        if (!validateInput(selectedWaterLevel, alarmThresholdValue, notificationMethodGroup.getValue())) {
            return;
        }


        Alarm alarm = new Alarm();
        alarm.setStationId(selectedWaterLevel.getId());
        alarm.setStationName(selectedWaterLevel.getName());
        alarm.setUser(userService.getLoggedInUser());

        if (alarmTypeGroup.getValue().equalsIgnoreCase("level")) {
            alarm.setAlertThresholdLevel(alarmThresholdValue);
        } else {
            alarm.setAlertThresholdFlow(alarmThresholdValue);
        }
        alarm.setNotificationMethod(notificationMethodGroup.getValue());
        alarm.setChatId(token.getValue());

        if (!alarmGrid.getSelectedItems().isEmpty() && !asNew) {
            alarm.setId(alarmGrid.getSelectedItems().iterator().next().getId());
        }

        Alarm saved = alarmService.saveAlarm(alarm);
        refreshAlarms();

        alarmGrid.select(saved);
        showSuccessNotification("Alarm je bil uspešno shranjen.");
    }

    private boolean validateInput(Station selectedWaterLevel, Double alarmThresholdValue, NotificationMethod notificationMethod) {

        if (selectedWaterLevel == null) {
            error("Prosimo, izberite vodostaj.");
            return false;
        }

        if (alarmThresholdValue == null || alarmThresholdValue.isNaN()) {
            error("Prosimo, vnesite nivo alarma.");
            return false;
        }
        if (telegram == notificationMethod && token.getValue().length() < 5) {
            error("Prosimo, vnesite Telegram token.");
            return false;
        }

        if (email == notificationMethod && !EmailValidator.getInstance().isValid(token.getValue())) {
            error("Prosimo, vnesite veljaven e-poštni naslov.");
            return false;
        }


        return true;
    }

    private void edit(Alarm item) {

        Station station = stationsService.getStationFromId(item.getStationId());
        waterLevelComboBox.setValue(station);
        if (item.getAlertThresholdLevel() != 0) {
            alarmTypeGroup.setValue("level");
            alarmThresholdField.setValue(item.getAlertThresholdLevel());
        } else {
            alarmTypeGroup.setValue("flow");
            alarmThresholdField.setValue(item.getAlertThresholdFlow());
        }
        notificationMethodGroup.setValue(item.getNotificationMethod());
        token.setValue(item.getChatId());
    }

    private void showWaterLevels(Alarm alarm) {
        // Naredite nekaj, ko je izbran alarm
        waterLevelsContainer.setVisible(true);
        WaterLevelsComponent waterLevelsComponent = new WaterLevelsComponent(waterLevelService, alarm.getStationId(), alarm);
        waterLevelsContainer.removeAll();
        waterLevelsContainer.add(new H2(alarm.getStationName()), waterLevelsComponent);

        waterLevelsContainer.add(waterLevelsComponent);
    }

    private HorizontalLayout createActions(Alarm alarm) {
        Button deleteButton = new Button("Izbriši");
        deleteButton.addClickListener(event -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Potrditev brisanja");
            dialog.setText("Ali res želite izbrisati alarm za postajo: " + alarm.getStationName() + "?");
            // Nastavimo gumbe in njihovo obnašanje
            dialog.setCancelable(true);
            dialog.setCancelText("Prekliči");

            dialog.setConfirmText("Izbriši");
            dialog.addConfirmListener(e -> {
                alarmService.deleteAlarm(alarm.getId());
                refreshAlarms();
            });

            dialog.open();
        });

        Button sendTestMsgButton = new Button("Test");
        sendTestMsgButton.setTooltipText("Pošlji testno sporočilo");
        sendTestMsgButton.addClickListener(event -> {
            // Pošljemo testno sporočilo
            messengerService.sendNotification(alarm, "Testno sporočilo");
        });

        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.add(deleteButton);
        actionsLayout.add(sendTestMsgButton);
        return actionsLayout;
    }

    // Osveži prikaz v Gridu
    private void refreshAlarms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            String userId = oidcUser.getSubject(); // ali oidcUser.getEmail(), odvisno od tega, kaj želite uporabiti za identifikacijo uporabnika

            // Uporabite userId, da pridobite alarme za tega uporabnika
            List<Alarm> userAlarms = alarmService.findTop100ByUserId(userId);
            alarmGrid.setItems(userAlarms);
        } else {
            // Morda želite zabeležiti napako ali narediti nekaj drugega, če je uporabnik anonimen ali nepričakovane vrste
            alarmGrid.setItems(List.of()); // prazen seznam
        }
    }

    @PostConstruct
    public void init() {
        final UI ui = UI.getCurrent();
        if (ui != null) {
            ui.setLocale(new Locale("sl", "SI"));

        }
    }

    public enum NotificationMethod {
        telegram, mobile, email
    }
}
