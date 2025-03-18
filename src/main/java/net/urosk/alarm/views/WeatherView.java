package net.urosk.alarm.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.Getter;
import net.urosk.alarm.models.WeatherObservation;
import net.urosk.alarm.services.UserService;
import net.urosk.alarm.services.UtilService;
import net.urosk.alarm.services.WeatherService;
import org.apache.commons.lang3.StringUtils;
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

import static net.urosk.alarm.views.WeatherView.WeatherViewType.TEMPERATURE;

@PageTitle("Vreme Slovenije")
@PermitAll
@Route(value = "vreme", layout = MainLayout.class)
public class WeatherView extends AbstractView {

    private static final String ID = "leaflet-weather-view";
    private final UtilService utilService;
    private final UserService userService;
    private final WeatherService weatherService;
    RadioButtonGroup<WeatherViewType> views = new RadioButtonGroup<>();
    LComponentManagementRegistry reg;
    LMap map;
    VerticalLayout container;
    List<WeatherObservation> observations;

    public WeatherView(WeatherService weatherService, UtilService utilService, UserService userService) {
        this.weatherService = weatherService;
        this.utilService = utilService;


        this.userService = userService;
        setPadding(false);


        this.setId(ID);

        container = new VerticalLayout();
        container.setSpacing(false);
        container.setPadding(false);
        container.setMargin(false);
        container.setSizeFull();

        views.setItems(WeatherViewType.values());
        views.setItemLabelGenerator(WeatherViewType::getTitle);
        views.addValueChangeListener(e -> showWeather(e.getValue()));


        add(views);

        add(container);
        showWeather(TEMPERATURE);


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

    private void showWeather(WeatherViewType viewType) {


        renderMap();

        observations = weatherService.findLatestObservations();
        addStationsToMapWithAlarmLevelFlows(viewType, observations);


        fitMapToStations(observations);

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
//
//            getElement().executeJs(
//                    "window.openPopup = function(stationId, stationName) {" +
//                            "    $0.$server.openPopup(stationId, stationName);" +
//                            "};", getElement()
//            );
    }

    private void addStationsToMapWithAlarmLevelFlows(WeatherViewType viewType,List<WeatherObservation> weatherObservations) {
        for (WeatherObservation weatherObservation : weatherObservations) {
            String weatherIconUrl = "frontend/images/weather/sun.png";
           //double temperature = weatherObservation.getTemperature();

            String val = " "; // Privzeta vrednost

            switch (viewType) {
                case TEMPERATURE -> val = (weatherObservation.getTemperature() != null)
                        ? weatherObservation.getTemperature() + " °C"
                        : "N/A";
                case PRECIPITATION -> val = (weatherObservation.getPrecipitation() != null)
                        ? weatherObservation.getPrecipitation() + " mm"
                        : "0 mm"; // Padavine so lahko 0 mm
                case WIND -> val = (weatherObservation.getWindSpeed() != null)
                        ? weatherObservation.getWindSpeed() + " km/h"
                        : "0 km/h"; // Privzeta hitrost vetra je 0
                case HUMIDITY -> val = (weatherObservation.getHumidity() != null)
                        ? weatherObservation.getHumidity() + " %"
                        : "0 %"; // Privzeta vlažnost je 0 %
            }



            // Pravilno ustvarimo HTML s klikom na span
            String htmlContent = "<div class='custom-weather-marker' onclick='openPopup(\""
                    + weatherObservation.getId() + "\", \"" + weatherObservation.getStationTitle() + "\")'>" +
                    "<span >" + val + "</span>"+
                    "<img src='" + weatherIconUrl + "' />" + // Ikona
                    "</div>";

            LDivIconOptions options = new LDivIconOptions();
            options.setHtml(htmlContent);
            options.setClassName("custom-weather-marker");
            options.setIconSize(new LPoint(reg, 32, 50)); // Velikost markerja (ikona + temperatura)
            options.setIconAnchor(new LPoint(reg, 16, 50)); // Postavitev markerja

            LIcon icon = new LDivIcon(reg, options);

            // Dodamo marker
            var marker = new LMarker(reg, new LLatLng(reg, weatherObservation.getLatitude(), weatherObservation.getLongitude()))
                    .setIcon(icon)
                    .bindTooltip(weatherObservation.getStationTitle() + " " + weatherObservation.getTemperature() + "°C")
                    .addTo(map);

            // Klik na marker odpira popup
            final String clickFuncReference = map.clientComponentJsAccessor() + ".openPopup";
            reg.execJs(clickFuncReference + "=e => $0.$server.openPopup($1,$2);", this,
                    weatherObservation.getId().toString(), weatherObservation.getStationName());
            marker.on("click", clickFuncReference);
        }

    }

    @ClientCallable
    public void openPopup(String stationId, String stationName) {

        WeatherObservation observation = observations.stream().filter(obs -> obs.getStationName().equals(stationName)).findFirst().orElse(null);
        if (observation == null) {
            return;
        }

        Dialog popupDialog = new Dialog();
        popupDialog.getHeader().add(new H4(observation.getStationTitle()));
        popupDialog.setCloseOnEsc(true);
        popupDialog.setCloseOnOutsideClick(true);
        popupDialog.setDraggable(true);
        popupDialog.setResizable(true);

        popupDialog.setMinHeight("500px");
        popupDialog.setMinWidth("400px");

        VerticalLayout popupContent = new VerticalLayout();
        popupContent.setPadding(true);
        popupContent.setSpacing(true);
        popupContent.setMargin(true);
        popupContent.setSizeFull();

        if (StringUtils.isNotEmpty(observation.getWebcamEast() )) {
            popupContent.add(new Span("Pogled iz vzhoda"));
            Image webCam = new Image(weatherService.getWebcamUrlBase() + "/" + observation.getWebcamEast()
                    , "Webcam pogled Vzhod");
            popupContent.add(webCam);
        }

        if (StringUtils.isNotEmpty(observation.getWebcamWest())) {
            popupContent.add(new Span("Pogled iz zahoda"));
            Image webCam = new Image(weatherService.getWebcamUrlBase() + "/" + observation.getWebcamWest()
                    , "Webcam pogled Zahod");
            popupContent.add(webCam);
        }

        if (StringUtils.isNotEmpty(observation.getWebcamNorth())) {
            popupContent.add(new Span("Pogled iz severa"));
            Image webCam = new Image(weatherService.getWebcamUrlBase() + "/" + observation.getWebcamNorth()
                    , "Webcam pogled Sever");
            popupContent.add(webCam);
        }

        if (StringUtils.isNotEmpty(observation.getWebcamSouth())) {
            popupContent.add(new Span("Pogled iz juga"));
            Image webCam = new Image(weatherService.getWebcamUrlBase() + "/" + observation.getWebcamSouth()
                    , "Webcam pogled Jug");
            popupContent.add(webCam);
        }


        popupDialog.add(popupContent);


        popupDialog.getHeader().add(new Button(VaadinIcon.CLOSE.create(), event -> popupDialog.close()));
        popupDialog.getFooter().add(new Button(VaadinIcon.CLOSE.create(), event -> popupDialog.close()));


        popupDialog.open();
    }

    private void fitMapToStations(List<WeatherObservation> stations) {
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
        for (WeatherObservation station : stations) {
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


    @Getter
    public enum WeatherViewType {

        TEMPERATURE("Temperatura"),
        PRECIPITATION("Padavine"),
        WIND("Veter"),
        HUMIDITY("Vlaga"),
        CAMERA("Kamera");

        private final String title;

        WeatherViewType(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }


}
