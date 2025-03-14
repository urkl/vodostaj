package net.urosk.alarm.components;


import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.SvgIcon;
import net.urosk.alarm.lib.Trend;
import net.urosk.alarm.lib.Utils;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.List;

import static net.urosk.alarm.lib.Utils.getTrendIcon;

public class TrendIndicator extends Div {

    /**
     * Konstruktor, ki prejme seznam vrednosti in na podlagi zadnjih dveh meritev prikaže
     * ustrezno LineAwesome ikono:
     * - Puščica gor, če se vrednost povečuje.
     * - Puščica dol, če se vrednost zmanjšuje.
     * - Vodoravna puščica (ali nič), če ni spremembe.
     *
     * @param values seznam vrednosti (npr. zadnjih 5 pretokov ali višin)
     */
    public TrendIndicator(List<Double> values) {
        // Nastavimo dimenzije indikatorja
        setWidth("30px");
        setHeight("30px");



        add((getTrendIcon(values)));

    }


    /**
     * Ustvari komponento z ikono glede na podani CSS razred in tooltip.
     *
     * @param iconClass CSS razred ikone (LineAwesome)
     * @param tooltip   opis, ki se prikaže ob hoverju
     * @return komponenta z ikono
     */
    private Div createIcon(String iconClass, String tooltip) {
        Div icon = new Div();
        icon.getElement().setProperty("innerHTML", "<i class=\"" + iconClass + "\" title=\"" + tooltip + "\"></i>");
        // Po želji prilagodite stil ikone
        icon.getStyle().set("font-size", "24px");
        icon.getStyle().set("line-height", "30px");
        return icon;
    }
}
