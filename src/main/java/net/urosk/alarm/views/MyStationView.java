package net.urosk.alarm.views;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
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

import static com.vaadin.flow.component.grid.GridVariant.*;
import static net.urosk.alarm.lib.UiUtils.hr;

@PageTitle("Merilne postaje")
@PermitAll
@Route(value = "moje-reke", layout = MainLayout.class)
public class MyStationView extends AbstractView {

    private final Grid<Station> stationGrid;
    private final MultiSelectComboBox<Station> stationSelector;
    private final StationsService stationsService;
    private final UserService userService;
    private final User currentUser;

    public MyStationView(AlarmService alarmService, UserService userService, StationsService stationsService, WaterLevelService waterLevelService) {
        this.stationsService = stationsService;
        this.userService = userService;
        this.currentUser = userService.getLoggedInUser();

        var allStations = stationsService.getAllStations();
        stationSelector = new MultiSelectComboBox<>("Izberi postaje");
        stationSelector.setWidth("100%");
        stationSelector.setItems(allStations);
        stationSelector.setItemLabelGenerator(Station::getName);
        stationSelector.addValueChangeListener(e -> {
            updateGrid();
            saveSelectedStations();
        });
        stationSelector.setSelectedItemsOnTop(true);
        stationSelector.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);


        stationGrid = new Grid<>(Station.class, false);
        stationGrid.addThemeVariants(LUMO_COLUMN_BORDERS, LUMO_WRAP_CELL_CONTENT, LUMO_COMPACT);

        stationGrid.addComponentColumn(station -> {
            VerticalLayout layout = new VerticalLayout();
            layout.setPadding(false);
            layout.setSpacing(false);
            layout.setMargin(false);
            List<WaterLevel> waterLevels = waterLevelService.getLastXWaterLevelsForStation(station.getId(), 300);
            Collections.reverse(waterLevels);


            DetailedStationDataAndChartComponent detailedStationDataAndChartComponent = new DetailedStationDataAndChartComponent(
                    station.getId(),
                              waterLevelService,
                    stationsService,
                    alarmService,
                    userService
            );


            layout.add(detailedStationDataAndChartComponent);
            //return apexCharts;
            return layout;
        }).setAutoWidth(true).setHeader("Graf višin");

        stationGrid.setWidthFull();
        stationGrid.setItems(List.of()); // Privzeto brez prikaza podatkov

        add(hr(), stationSelector, stationGrid);

        if (currentUser != null && currentUser.getSelectedStationIds() != null && !currentUser.getSelectedStationIds().isEmpty()) {
            List<String> stationIds = Arrays.stream(currentUser.getSelectedStationIds().split(",")).toList();

            Set<Station> selectedStations = allStations.stream()
                    .filter(station -> stationIds.stream().anyMatch(id -> id.equals(station.getId())))
                    .collect(Collectors.toSet());

            stationSelector.setValue(selectedStations);
        }
    }

    private void updateGrid() {
        Set<Station> selectedStations = stationSelector.getValue();
        stationGrid.setItems(selectedStations);

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
