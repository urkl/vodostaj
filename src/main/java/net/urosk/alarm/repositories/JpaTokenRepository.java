package net.urosk.alarm.repositories;

import net.urosk.alarm.models.RememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class JpaTokenRepository implements PersistentTokenRepository {

    private final RememberMeTokenRepository tokenRepository;

    public JpaTokenRepository(RememberMeTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    @Transactional
    public void createNewToken(PersistentRememberMeToken token) {
        tokenRepository.save(new RememberMeToken(
                token.getSeries(),
                token.getUsername(),
                token.getTokenValue(),
                LocalDateTime.now()
        ));
    }

    @Override
    @Transactional
    public void updateToken(String series, String tokenValue, java.util.Date lastUsed) {
        Optional<RememberMeToken> tokenOpt = tokenRepository.findById(series);
        tokenOpt.ifPresent(token -> {
            token.setTokenValue(tokenValue);
            token.setLastUsed(LocalDateTime.now());
            tokenRepository.save(token);
        });
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String series) {
        RememberMeToken token = tokenRepository.findBySeries(series);
        if (token != null) {
            return new PersistentRememberMeToken(
                    token.getUsername(),
                    token.getSeries(),
                    token.getTokenValue(),
                    java.sql.Timestamp.valueOf(token.getLastUsed())
            );
        }
        return null;
    }

    @Override
    @Transactional
    public void removeUserTokens(String username) {
        tokenRepository.deleteByUsername(username);
    }
}
