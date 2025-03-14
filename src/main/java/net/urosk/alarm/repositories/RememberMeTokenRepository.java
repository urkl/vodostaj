package net.urosk.alarm.repositories;

import net.urosk.alarm.models.RememberMeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RememberMeTokenRepository extends JpaRepository<RememberMeToken, String> {
    RememberMeToken findBySeries(String series);
    void deleteByUsername(String username);
}
