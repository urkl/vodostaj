package net.urosk.alarm.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;

@Route("login")
@PageTitle("Prijava - Vodostaji")
@AnonymousAllowed
public class LoginView extends AbstractView {

    public LoginView() {

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-view");


        // Naslov
        NativeLabel title = new NativeLabel("Spremljanje vodostajev");
        title.getStyle().set("font-size", "2rem").set("font-weight", "bold");

        // Gumb za Google prijavo
        Button loginButton = new Button("🔑 Prijavi se z Googlom", e -> {
            getUI().ifPresent(ui -> ui.getPage().setLocation("/oauth2/authorization/google"));
        });
        loginButton.getStyle()
                .set("background-color", "#4285F4")
                .set("color", "white")
                .set("padding", "10px 20px")
                .set("border-radius", "5px")
                .set("font-size", "1.2rem");

        NativeLabel urosk = new NativeLabel("By Urosk.NET 2025");
        urosk.getStyle().set("font-size", "1rem");

        add(title,  loginButton ,urosk );
    }


    @PostConstruct
    public void init() {

        //UI.getCurrent().getSession().setLocale(new Locale("sl", "SI"));

    }


}
