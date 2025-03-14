package net.urosk.alarm.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.server.Command;

@Tag("div")
public class ClientTimer extends Component {

    public ClientTimer(int intervalSeconds, Command callback) {
        getElement().executeJs(
                "const timer = setInterval(() => { this.dispatchEvent(new Event('trigger-callback')); }, $0 * 1000);"
                + "this.__timer = timer;",
                intervalSeconds
        );

        getElement().addEventListener("trigger-callback", event ->
                getUI().ifPresent(ui -> ui.access(callback))
        );
    }

    public void stopTimer() {
        getElement().executeJs("clearInterval(this.__timer);");
    }
}
