package net.urosk.alarm.services;

import net.urosk.alarm.models.User;
import net.urosk.alarm.models.UserSettings;
import net.urosk.alarm.repositories.UserRepository;
import net.urosk.alarm.repositories.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class UserSettingsService {

    private final UserSettingsRepository settingsRepository;

    public UserSettingsService(UserSettingsRepository settingsRepository, UserRepository userRepository) {
        this.settingsRepository = settingsRepository;
    }

    /**
     * Shrani String nastavitev.
     */
    @Transactional
    public void saveSettingString(User user, String key, String value) {
        UserSettings settings = getOrCreateSettings(user, key);
        settings.setSettingValueString(value);
        settings.setSettingValueBoolean(null);
        settings.setSettingValueNumber(null);
        settingsRepository.save(settings);
    }

    /**
     * Shrani Boolean nastavitev.
     */
    @Transactional
    public void saveSettingBoolean(User user, String key, boolean value) {
        UserSettings settings = getOrCreateSettings(user, key);
        settings.setSettingValueString(null);
        settings.setSettingValueBoolean(value);
        settings.setSettingValueNumber(null);
        settingsRepository.save(settings);
    }

    /**
     * Shrani Number nastavitev.
     */
    @Transactional
    public void saveSettingNumber(User user, String key, double value) {
        UserSettings settings = getOrCreateSettings(user, key);
        settings.setSettingValueString(null);
        settings.setSettingValueBoolean(null);
        settings.setSettingValueNumber(value);
        settingsRepository.save(settings);
    }

    /**
     * Pridobi String nastavitev (Optional).
     */
    @Transactional(readOnly = true)
    public Optional<String> getSettingString(User user, String key) {
        return settingsRepository.findByUserAndSettingKey(user, key)
                .map(UserSettings::getSettingValueString);
    }

    /**
     * Pridobi Boolean nastavitev (Optional).
     */
    @Transactional(readOnly = true)
    public Optional<Boolean> getSettingBoolean(User user, String key) {
        return settingsRepository.findByUserAndSettingKey(user, key)
                .map(UserSettings::getSettingValueBoolean);
    }

    /**
     * Pridobi Number nastavitev (Optional).
     */
    @Transactional(readOnly = true)
    public Optional<Double> getSettingNumber(User user, String key) {
        return settingsRepository.findByUserAndSettingKey(user, key)
                .map(UserSettings::getSettingValueNumber);
    }

    /**
     * Helper metoda za pridobivanje nastavitev ali njihovo ustvarjanje.
     */
    private UserSettings getOrCreateSettings(User user, String key) {

        return settingsRepository.findByUserAndSettingKey(user, key)
                .orElseGet(() -> {
                    UserSettings settings = new UserSettings();
                    settings.setUser(user);
                    settings.setSettingKey(key);
                    return settings;
                });
    }



}
