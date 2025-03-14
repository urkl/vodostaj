package net.urosk.alarm.config;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import net.urosk.alarm.components.OAuth2AuthenticationSuccessHandler;
import net.urosk.alarm.repositories.JpaTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {
    private static final int REMEMBER_ME_DURATION = 31536000; // Neskončno (1 leto)
    private static final String REMEMBER_ME_KEY = "emkwgrvawmgfkwrgevaerqgkjgelrngerogerokgerogerok";
    private static final String LOGIN_URL = "/login";

    private final UserDetailsService userDetailsService;
    private final JpaTokenRepository tokenRepository;
    public SecurityConfig(UserDetailsService userDetailsService, JpaTokenRepository tokenRepository) {

        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
    }


    @Bean
    public RememberMeServices rememberMeServices() {
        System.out.println("✅ Using TokenBasedRememberMeServices...");

        PersistentTokenBasedRememberMeServices rememberMe = new PersistentTokenBasedRememberMeServices(
                REMEMBER_ME_KEY, // Poskrbite za varnost!
                userDetailsService,
                tokenRepository
        );
        rememberMe.setTokenValiditySeconds(30 * 24 * 60 * 60); // 30 dni
        rememberMe.setAlwaysRemember(true);

        return rememberMe;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session
                        .sessionFixation().none() // Ne ustvarjaj nove seje po prijavi
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS) // Ohrani sejo
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(new OidcUserService()))
                        .successHandler(new OAuth2AuthenticationSuccessHandler(rememberMeServices())) // Aktiviraj Remember Me po OAuth2 prijavi
                        .loginPage(LOGIN_URL)
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")  // Pot za odjavo
                        .logoutSuccessUrl(LOGIN_URL + "?logout")  // Kam preusmeri po odjavi
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key(REMEMBER_ME_KEY)
                        .rememberMeServices(rememberMeServices())
                        .userDetailsService(userDetailsService)
                        .tokenValiditySeconds(REMEMBER_ME_DURATION)
                        .alwaysRemember(true)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/VAADIN/push/**"),
                                new AntPathRequestMatcher("/images/**"),
                                new AntPathRequestMatcher("/frontend/**"),
                                new AntPathRequestMatcher("/VAADIN/**"),
                                new AntPathRequestMatcher("/manifest.webmanifest"),
                                new AntPathRequestMatcher("/sw.js"),
                                new AntPathRequestMatcher("/service-worker.js"),
                                new AntPathRequestMatcher("/offline.html"),
                                new AntPathRequestMatcher("/offline-stub.html"),
                                new AntPathRequestMatcher("/sw-runtime-resources-precache.js"),
                                new AntPathRequestMatcher("/swagger-ui/")
                        ).permitAll()
                );

        // Prepreči, da Vaadin anonimizira uporabnika
        super.configure(http);
    }

}