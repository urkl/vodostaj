package net.urosk.alarm.services;

import net.urosk.alarm.models.User;
import net.urosk.alarm.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Iskanje uporabnika: " + username);
        User user = userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Uporabnik ni bil najden: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getId(),  // Google ID kot username
                "",  // Geslo ni pomembno za OAuth2
                List.of(new SimpleGrantedAuthority("USER"))); // Privzema, da je vsak uporabnik "USER"
    }
}