package net.urosk.alarm.components;
import com.vaadin.flow.component.UI;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Broadcaster class for sending messages to the UI.
 * This class is thread-safe.
 * It uses a ConcurrentHashMap to store the user's UI and a listener for each user.
 *
 *
 */
public class Broadcaster {
    private static final ConcurrentHashMap<String, UI> userUIMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, BiConsumer<String, String>> listeners = new ConcurrentHashMap<>();

    public static synchronized void register(String userId, UI ui, BiConsumer<String, String> listener) {
        userUIMap.put(userId, ui);
        listeners.put(userId, listener);
    }

    public static synchronized void unregister(String userId) {
        userUIMap.remove(userId);
        listeners.remove(userId);
    }

    public static synchronized void broadcast(String userId, String message) {
        UI ui = userUIMap.get(userId);
        if (ui != null) {
            ui.access(() -> {
                if (listeners.containsKey(userId)) {
                    listeners.get(userId).accept(userId, message);
                }
            });
        }
    }
}
