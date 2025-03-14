package net.urosk.alarm.services;


import lombok.extern.slf4j.Slf4j;
import net.urosk.alarm.components.Broadcaster;
import net.urosk.alarm.models.Alarm;
import net.urosk.alarm.models.TriggeredAlarm;
import net.urosk.alarm.models.WaterLevel;
import net.urosk.alarm.repositories.AlarmRepository;
import net.urosk.alarm.repositories.TriggeredAlarmRepository;
import net.urosk.alarm.repositories.WaterLevelRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@EnableScheduling
@Slf4j
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final TriggeredAlarmRepository triggeredAlarmRepository;
    private final WaterLevelRepository waterLevelRepository;
    private final MessengerService messengerService;

    public AlarmService(AlarmRepository alarmRepository, TriggeredAlarmRepository triggeredAlarmRepository, WaterLevelRepository waterLevelRepository, MessengerService messengerService) {
        this.alarmRepository = alarmRepository;
        this.triggeredAlarmRepository = triggeredAlarmRepository;
        this.waterLevelRepository = waterLevelRepository;
        this.messengerService = messengerService;
    }

    @Scheduled(fixedRate = 600000)
    public void checkAlarms() {

        checkFlowAlarms();
        checkLevelAlarms();


    }

    public void checkFlowAlarms() {

        List<Alarm> alarmsToTriggerForFlow = alarmRepository.findAlarmsOverThresholdForFlow(); // ena poizvedba, glej prejšnje korake

        for (Alarm alarm : alarmsToTriggerForFlow) {

            WaterLevel waterLevel = waterLevelRepository.findTopByStationIdOrderByDateDesc(alarm.getStationId());

            // Zabeležimo sprožen dogodek
            TriggeredAlarm ta = new TriggeredAlarm();
            ta.setAlarm(alarm);
            ta.setTriggeredAt(LocalDateTime.now());
            ta.setMeasuredValue(waterLevel.getFlow());

// Sporočilo za PRETOK (flow) z ikono 💧
            String message = "⚠ ALARM za postajo " + alarm.getStationName() +
                    ": 💧 pretok presega " + alarm.getAlertThresholdFlow() +
                    " (trenutno: " + waterLevel.getFlow() + " m³/s)";

            Optional<TriggeredAlarm> lastAlarmOpt = triggeredAlarmRepository.findTopByAlarmOrderByTriggeredAtDesc(alarm);

            try {
                if (lastAlarmOpt.isPresent() && lastAlarmOpt.get().getMeasuredValue() == waterLevel.getFlow()) {
                    ta.setNote("Enaka vrednost, ni obveščanja.. ");
                } else {
                    ta.setNote("Spremenjena vrednost");
                    messengerService.sendNotification(alarm, message);
                    Broadcaster.broadcast(alarm.getUser().getId(), message);
                    triggeredAlarmRepository.save(ta);
                }

            } catch (Exception e) {
                log.error("Napaka pri obveščanju: " + e.getMessage());
                ta.setError(e.getMessage());
            }


        }
    }

    public void checkLevelAlarms() {

        List<Alarm> alarmsToTriggerForLevel = alarmRepository.findAlarmsOverThresholdForLevel(); // ena poizvedba, glej prejšnje korake

        for (Alarm alarm : alarmsToTriggerForLevel) {

            WaterLevel lastWaterLevelForStation = waterLevelRepository.findTopByStationIdOrderByDateDesc(alarm.getStationId());

            // Zabeležimo sprožen dogodek
            TriggeredAlarm ta = new TriggeredAlarm();
            ta.setAlarm(alarm);
            ta.setTriggeredAt(LocalDateTime.now());
            ta.setMeasuredValue(lastWaterLevelForStation.getLevel());


// Sporočilo za VIŠINO (level) z ikono 📏
            String message = "⚠ ALARM za postajo " + alarm.getStationName() +
                    ": 📏 nivo presega " + alarm.getAlertThresholdLevel() +
                    " (trenutno: " + lastWaterLevelForStation.getLevel() + " cm)";

            Optional<TriggeredAlarm> lastAlarmOpt = triggeredAlarmRepository.findTopByAlarmOrderByTriggeredAtDesc(alarm);

            try {
                if (lastAlarmOpt.isPresent() && lastAlarmOpt.get().getMeasuredValue() == lastWaterLevelForStation.getLevel()) {
                    ta.setNote("Enaka vrednost, ni obveščanja.. ");
                } else {
                    ta.setNote("Spremenjena vrednost");
                    messengerService.sendNotification(alarm, message);
                    Broadcaster.broadcast(alarm.getUser().getId(), message);
                    triggeredAlarmRepository.save(ta);
                }

            } catch (Exception e) {
                log.error("Napaka pri obveščanju: " + e.getMessage());
                ta.setError(e.getMessage());
            }


        }
    }

    public List<Alarm> getAllAlarms() {
        return alarmRepository.findAll();
    }

    public List<Alarm> findTop100ByUserId(String userId) {
        return alarmRepository.findTop100ByUserId(userId);
    }

    public Alarm saveAlarm(Alarm alarm) {
        return alarmRepository.save(alarm);
    }

    @Transactional
    public void deleteAlarm(Long alarmId) {
        // Najprej pridobimo alarm, če obstaja
        Alarm alarm = alarmRepository.findById(alarmId).orElse(null);
        if (alarm != null) {
            // Pobrišemo vse povezane zapise v TriggeredAlarm
            triggeredAlarmRepository.deleteAllByAlarm(alarm);

            // Pobrišemo alarm
            alarmRepository.delete(alarm);
        }
    }


    public List<Alarm> findByUserIdAndByStationId(String userId, String stationId) {
        return alarmRepository.findByUserIdAndStationId(userId, stationId);
    }
}
