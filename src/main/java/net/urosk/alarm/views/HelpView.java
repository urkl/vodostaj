package net.urosk.alarm.views;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import net.urosk.alarm.services.UtilService;


@PageTitle("Pomoč")
@PermitAll
@Route(value = "help/:md", layout = MainLayout.class)
public class HelpView extends AbstractView implements BeforeEnterObserver {


    private final UtilService utilService;

    public HelpView(UtilService utilService) {
        this.utilService = utilService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setMargin(false);

    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String mdParam = event.getRouteParameters().get("md").orElse("default");

        removeAll();
        add(utilService.getHtmlElementFromMarkdown(mdParam + ".md"));

    }


}
