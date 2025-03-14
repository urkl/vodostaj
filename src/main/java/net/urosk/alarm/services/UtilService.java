package net.urosk.alarm.services;

import com.vaadin.flow.component.Html;
import lombok.extern.slf4j.Slf4j;
import net.urosk.alarm.lib.Trend;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UtilService {
    private static final Set<Extension> EXTENSIONS = Set.of(TablesExtension.create());


    public Html getHtmlElementFromMarkdown(String markdownPath) {

        String markdown = getHtmlStringFromMarkdown(markdownPath);


        // Pravilna pot do CSS-ja v Vaadin aplikaciji (brez /static/)
        String cssLink = "<link rel=\"stylesheet\" type=\"text/css\" href=\"static/md.css\">";

        // Ustvari HTML komponento s povezanim CSS-jem
        Html content = new Html("<div class='markdown-content' style='padding: 20px;'>" + cssLink + markdown + "</div>");

        return content;
    }

    public String getHtmlStringFromMarkdown(String markdownPath) {
        try {
            // Preberi Markdown datoteko
            Resource resource = new ClassPathResource("static/" + markdownPath);
            String markdown;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                markdown = reader.lines().collect(Collectors.joining("\n"));
            }


            // Pretvori Markdown v HTML s podporo za tabele
            Parser parser = Parser.builder()
                    .extensions(EXTENSIONS)
                    .build();
            HtmlRenderer renderer = HtmlRenderer.builder()
                    .extensions(EXTENSIONS)
                    .build();

            return renderer.render(parser.parse(markdown));

        } catch (Exception e) {
            log.error("Napaka pri branju Markdown datoteke: " + markdownPath, e);
            throw new RuntimeException("Napaka pri branju Markdown datoteke: " + markdownPath);
        }
    }






}
