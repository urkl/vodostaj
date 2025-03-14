package net.urosk.alarm.components;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RememberMeServices rememberMeServices;

    public OAuth2AuthenticationSuccessHandler(RememberMeServices rememberMeServices) {
        this.rememberMeServices = rememberMeServices;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        System.out.println("✅ OAuth2 prijava uspešna, shranjujem uporabnika: " + authentication.getName());

        // Nastavimo uporabnika v SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Aktiviramo Remember Me
        rememberMeServices.loginSuccess(request, response, authentication);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );
        }
        // Izpišemo trenutno stanje prijave
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("🔐 Prijavljen uporabnik v SecurityContextHolder: " + auth.getName());
        } else {
            System.out.println("❌ SecurityContextHolder je še vedno prazen!");
        }

        // Preusmerimo na začetno stran
        response.sendRedirect("/");
    }
}
