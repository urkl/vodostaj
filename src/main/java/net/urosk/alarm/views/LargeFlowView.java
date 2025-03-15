package net.urosk.alarm.views;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import net.urosk.alarm.components.DetailedStationDataAndChartComponent;
import net.urosk.alarm.components.SparklineChart;
import net.urosk.alarm.components.TrendIndicator;
import net.urosk.alarm.models.Station;
import net.urosk.alarm.models.WaterLevel;
import net.urosk.alarm.services.AlarmService;
import net.urosk.alarm.services.StationsService;
import net.urosk.alarm.services.UserService;
import net.urosk.alarm.services.WaterLevelService;

import java.util.Collections;
import java.util.List;

import static net.urosk.alarm.lib.UiUtils.getFormatedNumber;
import static net.urosk.alarm.lib.UiUtils.hr;

@PageTitle("Vse reke")
@PermitAll
@Route(value = "vse-reke", layout = MainLayout.class)
public class LargeFlowView extends AbstractView {

    public LargeFlowView(StationsService stationsService, WaterLevelService waterLevelService, AlarmService alarmService, UserService userService) {
        setSizeFull();
        // ✅ Opis pogleda
        Paragraph description = new Paragraph(
                "Ta pogled prikazuje trenutne pretoke in višine rek." +
                        "Postaje so prikazane z alarmnimi nivoji, kar omogoča hitro oceno stanja. " +
                        "Pretoki so vizualno označeni glede na resnost preseganja mejnega nivoja: "
        );

        Div level0 = new Div(new Span("Normalni pretok (Nivoji so v mejah normale)"));
        level0.getElement().getThemeList().add("badge success");

        Div level1 = new Div(new Span("Prvi visokovodni pretok (Lokalno razlivanje izven rečnega korita)"));
        level1.getElement().getThemeList().add("badge primary");

        Div level2 = new Div(new Span("Drugi visokovodni pretok (Pojavijo se prve poplave)"));
        level2.getElement().getThemeList().add("badge warning");

        Div level3 = new Div(new Span("Tretji visokovodni pretok (Poplave na širšem območju)"));
        level3.getElement().getThemeList().add("badge error");

        Div container = new Div(description, level0, level1, level2, level3);
        container.getStyle().set("margin-bottom", "15px");


        Checkbox showOnlyOverFlow = new Checkbox("Prikaži samo postaje z alarmnimi nivoji");

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.add(showOnlyOverFlow);


        Grid<Station> overFlowStationsGrid = new Grid<>(Station.class, false);

        overFlowStationsGrid.addColumn(Station::getName).setHeader("Ime postaje").setAutoWidth(true);
        overFlowStationsGrid.addComponentColumn(s -> {
            String imagePath;
            String tooltip;

            if ("reka".equals(s.getType())) {
                imagePath = "frontend/images/river.png"; // Pot do tvoje slike reke
                tooltip = "Reka";
            } else {
                imagePath = "frontend/images/lake.png"; // Pot do tvoje slike jezera
                tooltip = "Jezero";
            }

            Image icon = new Image(imagePath, tooltip);
            icon.setWidth("30px"); // Nastavi velikost ikone
            icon.setHeight("30px");
            icon.getElement().setAttribute("title", tooltip); // Tooltip za več info

            return icon;
        }).setHeader("Tip vode").setWidth("60px");


        overFlowStationsGrid.addColumn(new ComponentRenderer<>(this::getStyledFlowComponent))
                .setHeader("Pretok [m³/s]")
                .setAutoWidth(true);

        overFlowStationsGrid.addColumn(new ComponentRenderer<>(station ->
                        new SparklineChart(station.getFlowHistory())
                )).setHeader("Trend pretoka (5)").setAutoWidth(true)
                .setTooltipGenerator(station -> "Trend pretoka za postajo " + station.getName() + " Zadnjih 5 meritev");

        overFlowStationsGrid.addColumn(new ComponentRenderer<>(station ->
                        new TrendIndicator(station.getFlowHistory()))).setHeader("Trend pretoka (2)").setAutoWidth(true)
                .setTooltipGenerator(station -> "Trend pretoka za postajo " + station.getName() + " Zadnji 2 meritvi");


        overFlowStationsGrid.addColumn(f -> getFormatedNumber(f.getTempCurrentLevel())).setHeader("Višina [cm]").setAutoWidth(true);

        overFlowStationsGrid.setItemDetailsRenderer(new ComponentRenderer<>(station -> {
            Div detailsLayout = new Div();
            detailsLayout.getStyle().set("padding", "10px");
            detailsLayout.getStyle().set("background", "var(--lumo-contrast-10pct)");
            detailsLayout.getStyle().set("border-radius", "5px");
            detailsLayout.getStyle().set("margin", "10px 0");

            // Glavni naslov s poudarkom
            Span header = new Span("Podrobnosti za postajo: " + station.getName());
            header.getStyle().set("font-weight", "bold");
            header.getStyle().set("margin-bottom", "8px");
            detailsLayout.add(header);

            // Vrstice z alarmnimi podatki z barvnimi badge elementi
            detailsLayout.add(createDetailRow("Pretok", getFormatedNumber(station.getTempCurrentFlow()) + " m³/s", "var(--lumo-success-color)"));
            detailsLayout.add(createDetailRow("Višina", getFormatedNumber(station.getTempCurrentLevel()) + " cm", "var(--lumo-success-color)"));

            detailsLayout.add(createDetailRow("Nivo alarma 1 - pretok", getFormatedNumber(station.getFlow1()) + " m³/s", "var(--lumo-primary-color)"));
            detailsLayout.add(createDetailRow("Nivo alarma 2 - pretok", getFormatedNumber(station.getFlow2()) + " m³/s", "var(--lumo-warning-color)"));
            detailsLayout.add(createDetailRow("Nivo alarma 3 - pretok", getFormatedNumber(station.getFlow3()) + " m³/s", "var(--lumo-error-color)"));

            detailsLayout.add(createDetailRow("Nivo alarma 1 - višina", getFormatedNumber(station.getLevel1()) + " cm", "var(--lumo-primary-color)"));
            detailsLayout.add(createDetailRow("Nivo alarma 2 - višina", getFormatedNumber(station.getLevel2()) + " cm", "var(--lumo-warning-color)"));
            detailsLayout.add(createDetailRow("Nivo alarma 3 - višina", getFormatedNumber(station.getLevel3()) + " cm", "var(--lumo-error-color)"));

            List<WaterLevel> waterLevels = waterLevelService.getLastXWaterLevelsForStation(station.getId(), 300);
            Collections.reverse(waterLevels);


            DetailedStationDataAndChartComponent chart = new DetailedStationDataAndChartComponent(
                    station.getId(),
                    waterLevelService,
                    stationsService,
                    alarmService,
                    userService
            );


            detailsLayout.add(chart);

            return detailsLayout;
        }));


        overFlowStationsGrid.setItems(stationsService.getStationCache());

        showOnlyOverFlow.addValueChangeListener(e -> {
            List<Station> stations = stationsService.getStationCache();
            if (e.getValue()) {
                stations.removeIf(s -> s.getTempCurrentFlow() <= s.getFlow1());
            }
            overFlowStationsGrid.setItems(stations);
        });

        Details details = new Details("Opis", container);
        details.setOpened(true);

        add(details, hr(), toolbar, overFlowStationsGrid);


    }


    private Div getStyledFlowComponent(Station station) {
        Div div = new Div();
        double flow = station.getTempCurrentFlow();
        double flow1 = station.getFlow1();
        double flow2 = station.getFlow2();

        div.setText(getFormatedNumber(flow) + " m³/s");

        if (flow <= flow1) {
            div.getElement().getThemeList().add("badge success"); // 🟢 Zeleno - pod 1. nivojem
        } else if (flow > flow1 && flow <= flow2) {
            div.getElement().getThemeList().add("badge primary"); // 🔵 Modro - pod 1. nivojem
        } else if (flow > flow2 && flow <= station.getFlow3()) {
            div.getElement().getThemeList().add("badge warning"); // 🟠 Oranžno - med 1. in 2. nivojem
        } else {
            div.getElement().getThemeList().add("badge error");   // 🔴 Rdeče - nad 2. nivojem
        }

        return div;
    }


    private Div createDetailRow(String label, String value, String badgeColor) {
        Div row = new Div();
        row.getStyle().set("margin", "4px 0");

        Span badge = new Span(label + ": ");
        badge.getStyle().set("padding", "2px 6px");
        badge.getStyle().set("border-radius", "4px");
        badge.getStyle().set("background-color", badgeColor);
        badge.getStyle().set("color", "white");
        badge.getStyle().set("font-weight", "bold");
        badge.getStyle().set("margin-right", "6px");

        Span valueSpan = new Span(value);
        row.add(badge, valueSpan);
        return row;
    }


}
