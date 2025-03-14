package net.urosk.alarm.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import net.urosk.alarm.models.TriggeredAlarm;
import net.urosk.alarm.repositories.TriggeredAlarmRepository;
import net.urosk.alarm.services.UserService;

import java.util.List;

import static net.urosk.alarm.lib.UiUtils.getFormatedNumber;
import static net.urosk.alarm.lib.UiUtils.getFormattedDate;


@PageTitle("Sproženi alarmi")
@PermitAll
@Route(value = "sprozeni-alarmi", layout = MainLayout.class)  // <--- DODANO layout

public class TriggeredAlarmView extends AbstractView {

    public TriggeredAlarmView(TriggeredAlarmRepository triggeredAlarmRepository, UserService userService) {


        List<TriggeredAlarm> allTriggered = triggeredAlarmRepository.findTop100ByUserIdOrderByTriggeredAtDesc(userService.getLoggedInUser().getId());

        Grid<TriggeredAlarm> grid = new Grid<>(TriggeredAlarm.class, false);
        grid.addColumn(TriggeredAlarm::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(ta -> getFormatedNumber(ta.getAlarm().getAlertThresholdLevel())).setHeader("Nivo alarma - višina [cm]").setAutoWidth(true);
        grid.addColumn(ta -> getFormatedNumber(ta.getAlarm().getAlertThresholdFlow())).setHeader("Nivo alarma - pretok [m³/s]").setAutoWidth(true);
        grid.addColumn(ta -> getFormatedNumber(ta.getMeasuredValue())).setHeader("Izmerjena vrednost").setAutoWidth(true);
        grid.addColumn(ta -> ta.getAlarm().getStationName()).setHeader("Postaja").setAutoWidth(true);
        grid.addColumn(waterLevel -> getFormattedDate(waterLevel.getTriggeredAt())).setHeader("Sprožen ob");
        grid.addColumn(TriggeredAlarm::getNote).setHeader("Opombe").setAutoWidth(true);
        grid.addColumn(TriggeredAlarm::getError).setHeader("Napaka:").setAutoWidth(true);
        grid.setItems(allTriggered);
        grid.setSizeFull();


        add(new Hr(), grid);
    }
}
