package net.urosk.alarm.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.urosk.alarm.models.User;
import net.urosk.alarm.repositories.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationSuccessListener {

    private final UserRepository userRepository;

    @EventListener
    public void onAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();

        // Preverimo, ali je uporabnik prijavljen prek OIDC (Google) ali drugega vira
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            String userId = oidcUser.getSubject();
            String email = oidcUser.getEmail();
            String fullName = oidcUser.getFullName();

            // Preverimo, če uporabnik že obstaja v bazi
            userRepository.findById(userId).ifPresentOrElse(
                    existingUser -> {
                        // Uporabnik že obstaja
                        log.info("Uporabnik že obstaja: " + existingUser.getEmail());
                    },
                    () -> {
                        // Shrani novega uporabnika
                        User newUser = new User();
                        newUser.setId(userId);
                        newUser.setEmail(email);
                        newUser.setName(fullName);
                        userRepository.save(newUser);
                        log.info("Shranjeni nov uporabnik: " + fullName + " (" + email + ")");
                    }
            );
        } else if (authentication.getPrincipal() instanceof UserDetails userDetails) {

            String username = userDetails.getUsername();

            userRepository.findById(username).ifPresentOrElse(
                    existingUser -> {
                        // Uporabnik že obstaja
                        log.info("Uporabnik že obstaja: " + existingUser.getName());
                    },
                    () -> {
                        // TODO: Shrani novega uporabnika

                    }
            );
        }
    }
}
