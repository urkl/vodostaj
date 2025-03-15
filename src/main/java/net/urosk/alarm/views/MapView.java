package net.urosk.alarm.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import net.urosk.alarm.components.DetailedStationDataAndChartComponent;
import net.urosk.alarm.lib.Trend;
import net.urosk.alarm.lib.Utils;
import net.urosk.alarm.models.Station;
import net.urosk.alarm.models.WaterLevel;
import net.urosk.alarm.services.*;
import software.xdev.vaadin.maps.leaflet.MapContainer;
import software.xdev.vaadin.maps.leaflet.basictypes.*;
import software.xdev.vaadin.maps.leaflet.layer.LLayerGroup;
import software.xdev.vaadin.maps.leaflet.layer.raster.LTileLayer;
import software.xdev.vaadin.maps.leaflet.layer.ui.LMarker;
import software.xdev.vaadin.maps.leaflet.layer.vector.LPolygon;
import software.xdev.vaadin.maps.leaflet.map.LMap;
import software.xdev.vaadin.maps.leaflet.registry.LComponentManagementRegistry;
import software.xdev.vaadin.maps.leaflet.registry.LDefaultComponentManagementRegistry;

import java.util.List;


@PageTitle("Zemljevid Slovenije")
@PermitAll
@Route(value = "zemljevid", layout = MainLayout.class)
public class MapView extends AbstractView {

    private static final String ID = "leaflet-maps-view";
    private final StationsService stationsService;
    private final UtilService utilService;
    private final WaterLevelService waterLevelService;
    private final AlarmService alarmService;
    private final UserService userService;
    LComponentManagementRegistry reg;
    LMap map;
    VerticalLayout container;

    public MapView(StationsService stationsService, UtilService utilService, WaterLevelService waterLevelService, AlarmService alarmService, UserService userService) {
        this.stationsService = stationsService;
        this.utilService = utilService;
        this.waterLevelService = waterLevelService;
        this.alarmService = alarmService;
        this.userService = userService;
        setPadding(false);

        Button btnAlarmniVodotoki = new Button("Alarmni vodotoki", e -> {
            showAlarms();
        });
        Button btnTrend = new Button("Trend", e -> {

            showTrends();
        }
        );

        HorizontalLayout buttonLayout = new HorizontalLayout(btnAlarmniVodotoki, btnTrend);

        add(buttonLayout);

        this.setId(ID);

        container = new VerticalLayout();
        container.setSpacing(false);
        container.setPadding(false);
        container.setMargin(false);
        container.setSizeFull();

        add(container);
        showAlarms();


    }

    private void showTrends() {

        renderMap();

        List<Station> stations = stationsService.getStationCache();
        addStationsToMapWithTrendsIndicator(stations);


        fitMapToStations(stations);
    }

    void renderMap() {
        container.removeAll();
        reg = new LDefaultComponentManagementRegistry(this);
        final MapContainer mapContainer = new MapContainer(reg);
        map = mapContainer.getlMap();

        mapContainer.setSizeFull();

        map.addLayer(new LTileLayer(reg, "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"));

        // Nastavi pogled na celotno Slovenijo
        map.fitBounds(new LLatLngBounds(reg,
                new LLatLng(reg, 45.42, 13.38),  // Jugozahod (Piran)
                new LLatLng(reg, 46.88, 16.61)   // Severo-vzhod (Lendava)
        ));

        // Naredimo poligon
        final LPolygon polygonNOC = new LPolygon(
                reg, new LLatLng(reg, 46.674883, 14.159098),
                new LLatLng(reg, 46.675719, 14.160248),
                new LLatLng(reg, 46.676080, 14.159985),
                new LLatLng(reg, 46.675750, 14.158008),
                new LLatLng(reg, 46.675306, 14.158499)
        );
        polygonNOC.bindPopup("Kr neki").bindTooltip("različne točke");


        final LLayerGroup lLayerGroupPlaces = new LLayerGroup(reg).addLayer(polygonNOC);
        map.addLayer(lLayerGroupPlaces);


        container.add(mapContainer);
    }

    private void showAlarms() {


        renderMap();

        List<Station> stations = stationsService.getStationCache();
        addStationsToMapWithAlarmLevelFlows(stations);


        fitMapToStations(stations);

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

    }

    private void addStationsToMapWithTrendsIndicator(List<Station> stations) {
        for (Station station : stations) {


            Trend trend = Utils.getFlowTrend(station.getFlowHistory());
            LIcon icon = getIconForTrend(trend);

            String trendTitle = "Trend: " + Utils.getTrendTitle(trend);

            final String clickFuncReference = map.clientComponentJsAccessor() + ".openPopup";
            reg.execJs(clickFuncReference + "=e => $0.$server.openPopup($1,$2);", this, station.getId(), station.getName());
            // Ustvari marker z ustrezno ikono
            var marker = new LMarker(reg, new LLatLng(reg, station.getLatitude(), station.getLongitude()))
                    .setIcon(icon)
                    .bindTooltip(station.getName() + " (" + station.getRiver() + ") " + trendTitle)
                    .addTo(map);
            marker.on("click", clickFuncReference);
        }
    }

    private void addStationsToMapWithAlarmLevelFlows(List<Station> stations) {
        for (Station station : stations) {

            // Določi ustrezno ikono glede na stanje vode
            LIcon icon = getIconForFlow(
                    station.getLastWaterLevel().getFlow(),
                    station.getLastWaterLevel().getFlow1(),
                    station.getLastWaterLevel().getFlow2(),
                    station.getLastWaterLevel().getFlow3()
            );

            final String clickFuncReference = map.clientComponentJsAccessor() + ".openPopup";
            reg.execJs(clickFuncReference + "=e => $0.$server.openPopup($1,$2);", this, station.getId(), station.getName());
            // Ustvari marker z ustrezno ikono
            var marker = new LMarker(reg, new LLatLng(reg, station.getLatitude(), station.getLongitude()))
                    .setIcon(icon)
                    .bindTooltip(station.getName() + " (" + station.getRiver() + ")")
                    .addTo(map);
            marker.on("click", clickFuncReference);
        }
    }

    private LIcon getCustomMarkerIcon(String iconPath) {
        LIconOptions options = new LIconOptions();

        // Pot do ikone, relativna do strežnika (npr. /icons/)
        options.setIconUrl("frontend/images/markers/" + iconPath);
        LPoint iconSize = new LPoint(reg, 25, 41);
        options.setIconSize(iconSize); // Velikost markerja
        LPoint iconAnchor = new LPoint(reg, 12, 41);
        options.setIconAnchor(iconAnchor); // Središče markerja
        LPoint popupAnchor = new LPoint(reg, 1, -34);
        options.setPopupAnchor(popupAnchor); // Pozicija popupa


        return new LIcon(reg, options);
    }

    private LIcon getIconForFlow(double flow, double flow1, double flow2, double flow3) {
        String iconFileName;

        if (flow < flow1 || flow == 0) {
            iconFileName = "marker_success_50pct.png"; // Zelena (normalno)
        } else if (flow >= flow1 && flow < flow2) {
            iconFileName = "marker_primary_50pct.png"; // Modra (povišan vodostaj)
        } else if (flow >= flow2 && flow < flow3) {
            iconFileName = "marker_warning_50pct.png"; // Oranžna (kritično)
        } else {
            iconFileName = "marker_danger_50pct.png"; // Rdeča (poplave)
        }

        return getCustomMarkerIcon(iconFileName);
    }

    private LIcon getIconForTrend(Trend trend) {
        String iconFileName;

        switch (trend) {
            case RISING:
                iconFileName = "marker_danger_50pct.png";
                break;
            case FALLING:
                iconFileName = "marker_success_50pct.png";
                break;
            default:
                iconFileName = "marker_primary_50pct.png";
        }

        return getCustomMarkerIcon(iconFileName);
    }


    @ClientCallable
    public void openPopup(String stationId, String stationName) {
        WaterLevel level = waterLevelService.getCurrentByStationId(stationId);
        Station station = stationsService.getStationFromId(stationId);
        if (level == null) {
            Notification.show("Podatki niso na voljo.", 3000, Notification.Position.MIDDLE);
            return;
        }

        Dialog popupDialog = new Dialog();
        popupDialog.setCloseOnEsc(true);
        popupDialog.setCloseOnOutsideClick(true);
        popupDialog.setDraggable(true);
        popupDialog.setResizable(true);
        popupDialog.setMinHeight("500px");
        popupDialog.setMinWidth("400px");

        popupDialog.getHeader().add(new Button(VaadinIcon.CLOSE.create(), event -> popupDialog.close()));
        popupDialog.getFooter().add(new Button(VaadinIcon.CLOSE.create(), event -> popupDialog.close()));

        DetailedStationDataAndChartComponent detailedStationDataAndChartComponent = new DetailedStationDataAndChartComponent(
                stationId,

                waterLevelService,
                stationsService,
                alarmService,
                userService
        );


        popupDialog.add(detailedStationDataAndChartComponent);


        popupDialog.open();
    }


    private void fitMapToStations(List<Station> stations) {
        if (stations.isEmpty()) {
            map.fitBounds(new LLatLngBounds(reg,
                    new LLatLng(reg, 45.42, 13.38),  // Southwest (Piran)
                    new LLatLng(reg, 46.88, 16.61)   // Northeast (Lendava)
            ));
            return;
        }

        // Initialize min/max with the first station's coordinates
        double minLat = stations.get(0).getLatitude();
        double minLng = stations.get(0).getLongitude();
        double maxLat = minLat;
        double maxLng = minLng;

        // Find the actual min/max coordinates
        for (Station station : stations) {
            double lat = station.getLatitude();
            double lon = station.getLongitude();

            minLat = Math.min(minLat, lat);
            minLng = Math.min(minLng, lon);
            maxLat = Math.max(maxLat, lat);
            maxLng = Math.max(maxLng, lon);
        }


        LLatLng corner1 = new LLatLng(reg, minLat, minLng);
        LLatLng corner2 = new LLatLng(reg, maxLat, maxLng);

        // Apply new bounds
        map.fitBounds(new LLatLngBounds(reg, corner1, corner2));
    }


}
