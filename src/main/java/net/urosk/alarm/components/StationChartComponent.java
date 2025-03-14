package net.urosk.alarm.components;

import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.Annotations;
import com.github.appreciated.apexcharts.config.annotations.AnnotationLabel;
import com.github.appreciated.apexcharts.config.annotations.YAxisAnnotations;
import com.github.appreciated.apexcharts.config.annotations.builder.YAxisAnnotationsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.builder.LabelsBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.tooltip.builder.YBuilder;
import com.github.appreciated.apexcharts.config.xaxis.labels.builder.DatetimeFormatterBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import net.urosk.alarm.lib.UiUtils;
import net.urosk.alarm.models.Alarm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StationChartComponent extends ApexChartsBuilder {

    public StationChartComponent(String stationName, List<Double> levels, List<Double> flows, List<Double> temperatures, List<String> timestamps, List<Alarm> userAlarms) {

        // Ustvarite serije podatkov
        Series<Double> levelSeries = new Series<>("Vodostaj", levels.toArray(new Double[0]));
        Series<Double> flowSeries = new Series<>("Pretok", flows.toArray(new Double[0]));
        Series<Double> temperatureSeries = new Series<>("Temperatura", temperatures.toArray(new Double[0]));


        List<YAxisAnnotations> anot = new ArrayList<>();

        userAlarms.forEach(alarm -> {


            if (alarm.getAlertThresholdFlow() != 0) {
                var f = attachLevelAnnotations(alarm.getAlertThresholdFlow(), "Alarm - pretok");
                anot.add(f);
            } else if ( alarm.getAlertThresholdLevel() != 0) {
                var l = attachLevelAnnotations(alarm.getAlertThresholdLevel(), "Alarm - nivo");
                anot.add(l);
            }

        });
        withTooltip(TooltipBuilder.get()
                .withY(YBuilder.get()
                        .withFormatter("""
                function(value, { seriesIndex, dataPointIndex, w }) {
                    var unit = [" cm", " m³/s", " °C"];
                    var formattedValue = new Intl.NumberFormat('sl-SI', {
                        minimumFractionDigits: 2, 
                        maximumFractionDigits: 2 
                    }).format(value) + unit[seriesIndex];

                   

                    return formattedValue ;
                }
                """) // Slovenska lokalizacija števil + datumov
                        .build())
                .build());

        Annotations annotations = new Annotations();
        annotations.setYaxis(anot);
        withAnnotations(annotations);

        withChart(ChartBuilder.get()
                .withType(Type.LINE)
                .build())
                .withStroke(StrokeBuilder.get()
                        .withCurve(Curve.SMOOTH)
                        .build())
                .withSeries(levelSeries, flowSeries, temperatureSeries)
                .withXaxis(XAxisBuilder.get()
                        .withCategories(timestamps.toArray(new String[0]))
                        .build())
                .withYaxis(YAxisBuilder.get().build());


    }

    private YAxisAnnotations attachLevelAnnotations(double level, String title) {


        AnnotationLabel annotationLabel = new AnnotationLabel();
        //annotationLabel.setBorderColor("#FF0000");
        annotationLabel.setText(title + ": " + UiUtils.getFormatedNumber(level));


        YAxisAnnotations alarmLine = YAxisAnnotationsBuilder.get()
                .withY(level)
               .withBorderColor("#FF0000")  // Rdeča črta
                .withLabel(annotationLabel)
                .withStrokeDashArray(5.0) // Črtkana črta
                .build();


        return alarmLine;


    }
}
