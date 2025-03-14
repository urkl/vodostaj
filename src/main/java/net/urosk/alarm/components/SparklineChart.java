package net.urosk.alarm.components;


import com.vaadin.flow.component.html.Div;

import java.util.List;
import java.util.OptionalDouble;

public class SparklineChart extends Div {

    /**
     * Konstruktor, ki prejme seznam vrednosti (npr. pretokov ali višin) in ustvari SVG sparkline.
     *
     * @param values seznam vrednosti, ki jih želimo prikazati
     */
    public SparklineChart(List<Double> values) {
        // Nastavimo privzete dimenzije grafa
        setWidth("100px");
        setHeight("30px");

        // Ustvarimo SVG vsebino in jo vstavimo v komponento
        String svgContent = createSparklineSVG(values, 100, 30);
        getElement().setProperty("innerHTML", svgContent);
    }

    /**
     * Ustvari SVG vsebino za sparkline.
     *
     * @param values seznam vrednosti za graf
     * @param width širina SVG elementa
     * @param height višina SVG elementa
     * @return niz z vsebino SVG elementa
     */
    private String createSparklineSVG(List<Double> values, int width, int height) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        // Izračun minimuma in maksimuma za normalizacijo
        OptionalDouble minOpt = values.stream().mapToDouble(Double::doubleValue).min();
        OptionalDouble maxOpt = values.stream().mapToDouble(Double::doubleValue).max();
        double min = minOpt.orElse(0);
        double max = maxOpt.orElse(0);
        double range = max - min;
        if (range == 0) {
            range = 1; // Izognemo se deljenju z 0, če so vse vrednosti enake
        }

        int numPoints = values.size();
        double spacing = (double) width / (numPoints - 1);
        StringBuilder pointsBuilder = new StringBuilder();

        // Preračun vsake vrednosti v koordinati (x, y)
        for (int i = 0; i < numPoints; i++) {
            double value = values.get(i);
            // Normaliziramo vrednost med 0 in 1
            double normalized = (value - min) / range;
            // Inverzija y-osi, ker 0 je na vrhu SVG-ja
            double y = height - (normalized * height);
            double x = i * spacing;
            pointsBuilder.append(x).append(",").append(y).append(" ");
        }
        String points = pointsBuilder.toString().trim();

        // Ustvarimo SVG element s polyline, ki prikazuje linijski graf
        return "<svg width=\"" + width + "\" height=\"" + height + "\" xmlns=\"http://www.w3.org/2000/svg\">" +
                "<polyline fill=\"none\" stroke=\"black\" stroke-width=\"1\" points=\"" + points + "\" />" +
                "</svg>";
    }
}
