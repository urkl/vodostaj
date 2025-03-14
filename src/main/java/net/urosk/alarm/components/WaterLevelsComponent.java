package net.urosk.alarm.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.renderer.NumberRenderer;
import net.urosk.alarm.models.Alarm;
import net.urosk.alarm.models.WaterLevel;
import net.urosk.alarm.services.WaterLevelService;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;

import static net.urosk.alarm.lib.Constants.SLOVENIAN_FORMATTER;
import static net.urosk.alarm.lib.UiUtils.getDecimalFormat;

public class WaterLevelsComponent extends Grid<WaterLevel> {
    public WaterLevelsComponent(WaterLevelService waterLevelService, String stationId, Alarm alarm) {
        setSizeFull();


        addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        configureBeanType(WaterLevel.class, false);
        addColumn(waterLevel -> {
            LocalDateTime date = waterLevel.getDate();
            return date.format(SLOVENIAN_FORMATTER);
        }).setHeader("Čas meritve").setFlexGrow(1).setResizable(true);

        addComponentColumn(waterLevel -> {
            double level = waterLevel.getLevel();
            DecimalFormat decimalFormat = getDecimalFormat(); // Pridobi format
            String formattedLevel = decimalFormat.format(level); // Formatiraj številko

            Div div = new Div();
            div.setText(String.valueOf(formattedLevel));
            if (level > alarm.getAlertThresholdLevel()) {
                div.getStyle().set("color", "red"); // Pobarvaj z rdečo, če je pretok večji od praga
            }
            return div;
        }).setHeader("Višina vode [cm]").setFlexGrow(1).setResizable(true);;
        addColumn(new NumberRenderer<>(WaterLevel::getFlow, getDecimalFormat())).setHeader("Pretok [m³/s]").setFlexGrow(1).setResizable(true);;
        addColumn(new NumberRenderer<>(WaterLevel::getTemperature, getDecimalFormat())).setHeader("Temperatura [°C]").setFlexGrow(1).setResizable(true);;


        List<WaterLevel> levels = waterLevelService.getAllWaterLevels(stationId);


        setItems(levels);
        setMinHeight("300px");
        setSizeFull();

    }


}
