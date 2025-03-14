package net.urosk.alarm.lib;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Constants {
    public static final String ARSO_XML_URL = "https://www.arso.gov.si/xml/vode/hidro_podatki_zadnji.xml";
    //public static final String ARSO_XML_URL = "https://dostop.rtvslo.si/vodostaj/hidro_podatki_zadnji.xml";
    public static final NumberFormat SLOVENIAN_FORMAT = NumberFormat.getNumberInstance(new Locale("sl", "SI"));

    public static final DateTimeFormatter SLOVENIAN_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", new Locale("sl", "SI"));
}
