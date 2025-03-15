package net.urosk.alarm.views;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import net.urosk.alarm.components.DetailedStationDataAndChartComponent;
import net.urosk.alarm.models.Station;
import net.urosk.alarm.models.User;
import net.urosk.alarm.models.WaterLevel;
import net.urosk.alarm.services.AlarmService;
import net.urosk.alarm.services.StationsService;
import net.urosk.alarm.services.UserService;
import net.urosk.alarm.services.WaterLevelService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.urosk.alarm.lib.UiUtils.hr;

@PageTitle("Merilne postaje")
@PermitAll
@Route(value = "moje-reke", layout = MainLayout.class)
public class MyStationView extends AbstractView {

    //private final Grid<Station> stationGrid;
    private final MultiSelectComboBox<Station> stationSelector;
    private final StationsService stationsService;
    private final UserService userService;
    private final User currentUser;
    private final WaterLevelService waterLevelService;
    private final AlarmService alarmService;
    private final VerticalLayout container = new VerticalLayout();

    public MyStationView(AlarmService alarmService, UserService userService, StationsService stationsService, WaterLevelService waterLevelService) {
        this.alarmService = alarmService;
        this.stationsService = stationsService;
        this.userService = userService;
        this.waterLevelService = waterLevelService;
        this.currentUser = userService.getLoggedInUser();

        var allStations = stationsService.getAllStations();
        stationSelector = new MultiSelectComboBox<>("Izberi postaje");
        stationSelector.setWidth("100%");
        stationSelector.setItems(allStations);
        stationSelector.setItemLabelGenerator(Station::getName);
        stationSelector.addValueChangeListener(e -> {
            updateView();
            saveSelectedStations();
        });
        stationSelector.setSelectedItemsOnTop(true);
        stationSelector.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);

        container.setPadding(false);
        container.setSpacing(false);
        container.setWidth("100%");
        container.setMargin(false);
        add(hr(), stationSelector, hr(), container);

        if (currentUser != null && currentUser.getSelectedStationIds() != null && !currentUser.getSelectedStationIds().isEmpty()) {
            List<String> stationIds = Arrays.stream(currentUser.getSelectedStationIds().split(",")).toList();

            Set<Station> selectedStations = allStations.stream()
                    .filter(station -> stationIds.stream().anyMatch(id -> id.equals(station.getId())))
                    .collect(Collectors.toSet());

            stationSelector.setValue(selectedStations);
        }
    }

    public void addStation(Station station) {
        List<WaterLevel> waterLevels = waterLevelService.getLastXWaterLevelsForStation(station.getId(), 300);
        Collections.reverse(waterLevels);


        DetailedStationDataAndChartComponent detailedStationDataAndChartComponent = new DetailedStationDataAndChartComponent(
                station.getId(),
                waterLevelService,
                stationsService,
                alarmService,
                userService
        );


        container.add(detailedStationDataAndChartComponent);
        container.add(new Hr());
    }

    private void updateView() {
        Set<Station> selectedStations = stationSelector.getValue();
        container.removeAll();
        selectedStations.forEach(this::addStation);


    }

    private void saveSelectedStations() {
        if (currentUser != null) {
            String selectedStationIds = stationSelector.getValue().stream()
                    .map(Station::getId)
                    .collect(Collectors.joining(","));
            currentUser.setSelectedStationIds(selectedStationIds);
            userService.saveUser(currentUser);
        }
    }
}
