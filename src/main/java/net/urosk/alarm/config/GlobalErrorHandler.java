package net.urosk.alarm.config;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class GlobalErrorHandler implements ErrorHandler {


    @Override
    public void error(ErrorEvent event) {
        Throwable throwable = event.getThrowable();
        throwable.printStackTrace();

        log.error("Error during request", throwable);
        String errorMessage = "⚠ VAADIN ERROR: " + throwable.getMessage();


        Notification.show(errorMessage, 5000, Notification.Position.MIDDLE);
    }



}