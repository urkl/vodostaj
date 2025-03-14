package net.urosk.alarm;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@PWA(name = "Urosk Alarm vodostajev", shortName = "Vodostaj")
@Theme("urosk" )

@Push(PushMode.AUTOMATIC)  // Omogoči WebSockets (Push)
public class AlarmApplication implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(AlarmApplication.class, args);
    }
}
