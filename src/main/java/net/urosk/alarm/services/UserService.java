package net.urosk.alarm.services;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import lombok.Getter;
import net.urosk.alarm.models.User;
import net.urosk.alarm.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    @Getter
    private final UserSettingsService userSettingsService;

    public UserService(UserRepository userRepository, UserSettingsService userSettingsService) {
        this.userRepository = userRepository;
        this.userSettingsService = userSettingsService;
    }

    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            // OAuth2 prijava (Google)
            return userRepository.findById(oidcUser.getSubject())
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setId(oidcUser.getSubject());
                        newUser.setName(oidcUser.getFullName());
                        newUser.setEmail(oidcUser.getEmail());
                        return userRepository.save(newUser);
                    });
        } else if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User userDetails) {
            // Remember Me prijava (Spring Security UserDetails)
            return userRepository.findById(userDetails.getUsername()).orElse(null);
        }

        return null;
    }

    public void logout() {
        UI.getCurrent().getPage().setLocation("/logout");
        SecurityContextHolder.clearContext();
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
    }

    public void saveUser(User user) {


        userRepository.save(user);

    }

}
