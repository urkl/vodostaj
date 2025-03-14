package net.urosk.alarm.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.HashMap;
import java.util.Map;

public class ToggleButtonGroup extends HorizontalLayout {

    @FunctionalInterface
    public interface ToggleListener {
        /**
         * Metoda, ki se pokliče ob spremembi izbire.
         * @param selectedOption Trenutno izbrana možnost.
         */
        void onToggle(String selectedOption);
    }

    private final Map<String, Button> buttons = new HashMap<>();
    private String selectedOption;
    private final ToggleListener listener;

    /**
     * Konstruktor, ki sprejme callback listener in poljubno število možnosti.
     *
     * @param listener Callback funkcija, ki se sproži ob preklopu.
     * @param options Možnosti, ki jih bo prikazal toggle (npr. "Današnji dan", "5 dni").
     */
    public ToggleButtonGroup(ToggleListener listener, String... options) {
        this.listener = listener;
        for (String option : options) {
            Button btn = new Button(option);
            // Dodaj dogodek za klik
            btn.addClickListener(e -> {
                setSelectedOption(option);
                if (this.listener != null) {
                    this.listener.onToggle(option);
                }
            });
            buttons.put(option, btn);
            add(btn);
        }
    }

    /**
     * Nastavi izbrano možnost in ustrezno posodobi vizualno označitev.
     *
     * @param option Izbrana možnost.
     */
    public void setSelectedOption(String option) {
        if (selectedOption != null && buttons.containsKey(selectedOption)) {
            // Odstrani primarno stilizacijo od prejšnje izbire
            buttons.get(selectedOption).removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
        selectedOption = option;
        if (buttons.containsKey(option)) {
            // Označi trenutno izbrani gumb kot aktiven
            buttons.get(option).addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
    }

    /**
     * Vrne trenutno izbrano možnost.
     *
     * @return Trenutno izbrana možnost.
     */
    public String getSelectedOption() {
        return selectedOption;
    }
}
