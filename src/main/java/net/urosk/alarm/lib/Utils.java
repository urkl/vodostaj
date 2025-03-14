package net.urosk.alarm.lib;

import com.vaadin.flow.component.icon.SvgIcon;
import net.urosk.alarm.models.WaterLevel;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.List;

import static net.urosk.alarm.lib.Trend.FALLING;
import static net.urosk.alarm.lib.Trend.RISING;

public class Utils {
    public static  String getCssClassForFlowString(WaterLevel level, double flow) {
        double flow1 = level.getFlow1();
        double flow2 = level.getFlow2();
        double flow3 = level.getFlow3();
        // Določi ustrezno Lumo CSS klaso glede na vrednost
        String cssClass;
        if (level.getFlow() < flow1) {
            cssClass = "text-success"; // Zelena
        } else if (flow >= flow1 && flow < flow2) {
            cssClass = "text-primary"; // Modra
        } else if (flow >= flow2 && flow < flow3) {
            cssClass = "text-warning"; // Oranžna
        } else {
            cssClass = "text-error"; // Rdeča
        }
        return cssClass;
    }
    public static Trend getFlowTrend(List<Double> flowHistory) {
        if (flowHistory == null || flowHistory.size() < 2) {
            return Trend.STABLE; // Not enough data to determine a trend
        }


        // Zadnji dve vrednosti
        double prevValue = flowHistory.get(flowHistory.size() - 2);
        double lastValue = flowHistory.getLast();

        if (lastValue == 0) {
            return Trend.STABLE;
        }

        if (prevValue == lastValue) {
            return Trend.STABLE;
        } else if (prevValue > lastValue) {
            return FALLING;
        } else if (prevValue < lastValue) {
            return RISING;
        }

        return Trend.STABLE;
    }

    public static SvgIcon getTrendIcon(List<Double> values) {
        Trend trend = Utils.getFlowTrend(values);
        SvgIcon ico;
        switch (trend) {
            case RISING:
                ico = getUpTrendIcon();
                break;
            case FALLING:
                ico = getDownTrendIcon();
                break;
            default:
                ico = getEqualTrendIcon();
        }

        return ico;

    }

    public static SvgIcon getEqualTrendIcon() {
        SvgIcon ico;
        // Vrednost je enaka – vodoravna puščica
        ico = LineAwesomeIcon.ARROW_RIGHT_SOLID.create();
        ico.setTooltipText("Ni spremembe");
        ico.setColor("var(--lumo-primary-color)");
        return ico;
    }

    public static SvgIcon getUpTrendIcon() {
        SvgIcon ico;
        // Vrednost narašča – puščica gor
        ico = LineAwesomeIcon.ARROW_UP_SOLID.create();
        ico.setTooltipText("Narašča");
        ico.setColor("var(--lumo-error-color)");
        return ico;
    }

    public static SvgIcon getDownTrendIcon() {
        SvgIcon ico;
        // Vrednost narašča – puščica dol
        ico = LineAwesomeIcon.ARROW_DOWN_SOLID.create();
        ico.setTooltipText("Pada");
        ico.setColor("var(--lumo-success-color)");
        return ico;
    }

    public static String getTrendTitle(Trend trend) {
        return switch (trend) {
            case RISING -> "Narašča";
            case FALLING -> "Pada";
            default -> "Ni spremembe";
        };

    }

}
