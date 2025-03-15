package net.urosk.alarm.repositories;

import net.urosk.alarm.models.User;
import net.urosk.alarm.models.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByUserAndSettingKey(User user, String settingKey);
}
