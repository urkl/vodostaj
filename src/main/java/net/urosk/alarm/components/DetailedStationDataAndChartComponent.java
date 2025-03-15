package net.urosk.alarm.components;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.Annotations;
import com.github.appreciated.apexcharts.config.annotations.AnnotationLabel;
import com.github.appreciated.apexcharts.config.annotations.AnnotationStyle;
import com.github.appreciated.apexcharts.config.annotations.YAxisAnnotations;
import com.github.appreciated.apexcharts.config.annotations.builder.YAxisAnnotationsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.tooltip.builder.YBuilder;
import com.github.appreciated.apexcharts.config.xaxis.builder.LabelsBuilder;
import com.github.appreciated.apexcharts.config.yaxis.Title;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import net.urosk.alarm.models.Alarm;
import net.urosk.alarm.models.Station;
import net.urosk.alarm.models.WaterLevel;
import net.urosk.alarm.services.AlarmService;
import net.urosk.alarm.services.StationsService;
import net.urosk.alarm.services.UserService;
import net.urosk.alarm.services.WaterLevelService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.urosk.alarm.lib.UiUtils.*;
import static net.urosk.alarm.lib.Utils.getCssClassForFlowString;

public class DetailedStationDataAndChartComponent extends VerticalLayout {

    VerticalLayout chartContainer = new VerticalLayout();
    Checkbox showAlarms = new Checkbox("Prikaži visokovodne nivoje");
    private ApexCharts apexCharts;
    private boolean showLast24Hours = false;
    private Button toggleButton;

    public DetailedStationDataAndChartComponent(
            String stationId,
            WaterLevelService waterLevelService,
            StationsService stationsService,
            AlarmService alarmService,
            UserService userService
    ) {

        setPadding(false);
        setSpacing(false);
        setMargin(false);
        // Retrieve current water level and station data
        WaterLevel level = waterLevelService.getCurrentByStationId(stationId);
        Station station = stationsService.getStationFromId(stationId);

        double flow = level.getFlow();
        String cssClass = getCssClassForFlowString(level, flow);

        var flowsAndLevels = flowContainer(station);

        // Display basic water level data
        add(
                new Html("<h3>Podatki o vodostaju " + level.getName() + "</h3>"),
                //new Html("<p class='compact-paragraph'><b>Postaja:</b> " + level.getName() + "</p>"),
                new Html("<p class='compact-paragraph'><b>Reka:</b> " + level.getRiver() + "</p>"),
                new Html("<p class='compact-paragraph'><b>Datum meritve:</b> " + getFormattedDate(level.getDate()) + "</p>"),
                new Html("<p class='compact-paragraph'><b>Vodostaj:</b> " + getFormatedNumber(level.getLevel()) + " cm</p>"),
                // Flow with CSS color
                new Html("<p class='compact-paragraph'><b>Pretok:</b> <span class='" + cssClass + "'>"
                        + getFormatedNumber(flow) + " m³/s</span></p>"),
                new Html("<p class='compact-paragraph'><b>Temperatura:</b> " + getFormatedNumber(level.getTemperature()) + " °C</p>"),
                new Html("<p class='compact-paragraph'><b>Trenutni vodostaj:</b> " + level.getCurrentLevelTitle() + "</p>"),
                new Hr(),

                flowsAndLevels,
           //     new NativeLabel("Zadnji pretoki: " + station.getFlowHistory().toString()),
                new Hr()
        );

        // Retrieve historical water level data (last 300 measurements)
        List<WaterLevel> waterLevels = waterLevelService.getLastXWaterLevelsForStation(stationId, 300);
        Collections.reverse(waterLevels);

        // Extract timestamps and data for chart
        List<String> timestamps = waterLevels.stream()
                .map(wl -> getFormattedDate(wl.getDate()))
                .collect(Collectors.toList());

        List<Double> waterLevelsLevels = waterLevels.stream()
                .map(WaterLevel::getLevel)
                .toList();

        List<Double> waterLevelsFlow = waterLevels.stream()
                .map(WaterLevel::getFlow)
                .toList();


        // Get user alarms for the specific station
        List<Alarm> userAlarmsForStationId = alarmService.findByUserIdAndByStationId(
                userService.getLoggedInUser().getId(), stationId
        );



        // Ustvari gumb za preklop
        toggleButton = new Button("Prikaži zadnjih 24 ur", event -> {
            showLast24Hours = !showLast24Hours;
            // Posodobitev grafa s pravimi podatki
            updateChart(getFilteredList(waterLevelsLevels), getFilteredList(waterLevelsFlow),
                    getFilteredList(timestamps), userAlarmsForStationId, level);


            toggleButton.setText(showLast24Hours ? "Prikaži celotno zgodovino" : "Prikaži zadnjih 24 ur");


        });
        showAlarms.setValue(true);
        showAlarms.addValueChangeListener(e -> {

            updateChart(
                    waterLevelsLevels, waterLevelsFlow,
                    timestamps, userAlarmsForStationId, level);

        });


        HorizontalLayout buttonContainer = new HorizontalLayout();
        buttonContainer.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        buttonContainer.add(toggleButton,showAlarms);


        add(buttonContainer);
        chartContainer.setSizeFull();
        chartContainer.setPadding(false);
        chartContainer.setMargin(false);
        chartContainer.setSpacing(false);
        add(chartContainer);
        // Create and display initial chart
        updateChart(
                waterLevelsLevels, waterLevelsFlow,
                timestamps, userAlarmsForStationId, level);

    }

    private <T> List<T> getFilteredList(List<T> fullList) {
        if (showLast24Hours && fullList.size() > 48) {
            return fullList.subList(fullList.size() - 48, fullList.size());
        }
        return fullList; // Vrne celoten seznam, če je prikaz v načinu celotne zgodovine
    }

    private void updateChart(

            List<Double> waterLevelsLevels,
            List<Double> waterLevelsFlow,

            List<String> timestamps,
            List<Alarm> userAlarmsForStationId,
            WaterLevel currentLevel
    ) {
        // Remove existing chart if it exists
        if (apexCharts != null) {
            chartContainer.remove(apexCharts);
        }

        // Poiščemo max vrednost iz podatkov
        double maxFlowData = waterLevelsFlow.stream().max(Double::compareTo).orElse(0.0);
        double maxLevelData = waterLevelsLevels.stream().max(Double::compareTo).orElse(0.0);



// Max flow je največja vrednost med podatki in alarmnim pragom + 10 % rezerve
        double maxFlowY = Math.max(maxFlowData, currentLevel.getFlow3()) * 1.1;

// Max level je max vrednost iz podatkov + 10 % rezerve
        double maxLevelY = maxLevelData * 1.1;

        // Prepare data series
        List<Series<Double>> series = new ArrayList<>();

        series.add(new Series<>("Pretok (m³/s)", waterLevelsFlow.toArray(new Double[0])));
        series.add(new Series<>("Vodostaj (cm)", waterLevelsLevels.toArray(new Double[0])));


        // Create Y-axis annotations
        List<YAxisAnnotations> yAxisAnnotations = new ArrayList<>();

        // Add user alarms
        for (Alarm alarm : userAlarmsForStationId) {
            if (alarm.getAlertThresholdFlow() != 0) {
                yAxisAnnotations.add(createFlowAnnotation(
                        alarm.getAlertThresholdFlow(),
                        "Alarm - pretok (m³/s)",
                        "red",
                        false,"left",0
                ));
            } else if (alarm.getAlertThresholdLevel() != 0) {
                yAxisAnnotations.add(createFlowAnnotation(
                        alarm.getAlertThresholdLevel(),
                        "Alarm - nivo (cm)",
                        "red",
                        false,"left",1
                ));
            }
        }

        if (currentLevel.getFlow1() > 0 && showAlarms.getValue()) {

            // First threshold (blue/primary)
            yAxisAnnotations.add(createFlowAnnotation(
                    currentLevel.getFlow1(),
                    "Prvi visokovodni pretok (m³/s)",
                    "#3f51b5", // text-primary
                    true,"right",0
            ));

            // Second threshold (yellow/warning)
            yAxisAnnotations.add(createFlowAnnotation(
                    currentLevel.getFlow2(),
                    "Drugi visokovodni pretok (m³/s)",
                    "#ffeb3b", // text-warning
                    true,"right",0
            ));

            // Third threshold (red/error)
            yAxisAnnotations.add(createFlowAnnotation(
                    currentLevel.getFlow3(),
                    "Tretji visokovodni pretok (m³/s)",
                    "#f44336", // text-error
                    true,"right",0
            ));



        }
        // Create annotations config
        Annotations annotations = new Annotations();
        annotations.setYaxis(yAxisAnnotations);


        Title titleFlow = new Title();
        titleFlow.setText("Pretok (m³/s)");
        var flowAxis = YAxisBuilder.get()
                .withOpposite(false) // Primarna os (leva)
                .withTitle(titleFlow)
                .withMax(maxFlowY)
                .build();


        Title titleLevel = new Title();
        titleLevel.setText("Višina vode (m)");
        var levelAxis = YAxisBuilder.get()
                .withMax(maxLevelY)
                .withOpposite(true) // Sekundarna os (desna)
                .withTitle(titleLevel)
                .build();
        BigDecimal tickAmount = BigDecimal.valueOf(Math.min(20, timestamps.size() / 20)); // Prikaže največ 10 oznak

        if(isMobile()){
            tickAmount = BigDecimal.valueOf(Math.min(5, timestamps.size() / 5)); // Prikaže največ 10 oznak
        }


//        ApexCharts chart = ApexChartsBuilder.get()
//                .withYaxis(leftAxis, rightAxis) // Nastavi obe osi
//                .build();
        // Build chart
        ApexChartsBuilder chartBuilder = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.LINE)
                        .withToolbar(ToolbarBuilder.get()
                                .withShow(false).build())
                        .build())

                .withStroke(StrokeBuilder.get()
                        .withCurve(Curve.SMOOTH)
                        .build())
                .withSeries(series.toArray(new Series[0]))
                .withXaxis(XAxisBuilder.get()
                        .withCategories(timestamps.toArray(new String[0]))
                        .withTickAmount(tickAmount)

                        .withLabels(LabelsBuilder.get()
                                .withRotate(-40d) // Zavrtimo oznake za boljšo berljivost
                                //.withTrim(true) // Skrijemo oznake, ki so preblizu
                        //        .withMaxHeight(50d) // Omeji višino oznak
                                .build())
                        .build())
                .withYaxis(flowAxis, levelAxis)
                .withAnnotations(annotations)
                .withTooltip(TooltipBuilder.get()
                        .withY(YBuilder.get()
                                .withFormatter("""
                                        function(value, { seriesIndex, dataPointIndex, w }) {
                                            var unit = [" cm", " m³/s", " °C"];
                                            var formattedValue = new Intl.NumberFormat('sl-SI', {
                                                minimumFractionDigits: 2,
                                                maximumFractionDigits: 2
                                            }).format(value) + unit[seriesIndex];
                                            return formattedValue;
                                        }
                                        """)
                                .build())
                        .build());

        if (apexCharts != null) {
            remove(apexCharts);
        }
        apexCharts = chartBuilder.build();
        chartContainer.removeAll();
        chartContainer.add(apexCharts);
    }

    private YAxisAnnotations createFlowAnnotation(double value, String label, String color, boolean isDashed , String labelPosition, int yAxisIndex) {

        double offsetX = labelPosition.equals("left") ? 60.0 : 0.0; // Premik na desno, če je na levi strani

        AnnotationLabel annotationLabel = new AnnotationLabel();
        annotationLabel.setText(label + ": " + getFormatedNumber(value));

        AnnotationStyle annotationStyle = new AnnotationStyle();
        annotationStyle.setColor(color);

        annotationStyle.setCssClass("flow-annotation");
        annotationLabel.setStyle(annotationStyle);
        annotationLabel.setBorderWidth(0d); // Odstrani okvir
        annotationLabel.setBorderColor(null); // Ali nastavi na `null`, da se ne uporablja
        annotationLabel.setPosition(labelPosition);
        annotationLabel.setOffsetX(offsetX);



        YAxisAnnotationsBuilder builder = YAxisAnnotationsBuilder.get()
                .withY(value)
          //      .withOpacity(0.0)
             //   .withFillColor("red")
                .withYAxisIndex((double)yAxisIndex)
                //.withOffsetX(offsetX)
                .withBorderColor(color)
                .withLabel(annotationLabel);


        if (isDashed) {
            builder.withStrokeDashArray(5.0);

        }

        return builder.build();
    }

}