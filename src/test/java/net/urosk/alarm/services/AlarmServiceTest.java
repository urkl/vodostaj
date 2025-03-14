package net.urosk.alarm.services;


import net.urosk.alarm.models.User;
import net.urosk.alarm.models.Alarm;
import net.urosk.alarm.models.TriggeredAlarm;
import net.urosk.alarm.models.WaterLevel;
import net.urosk.alarm.repositories.AlarmRepository;
import net.urosk.alarm.repositories.TriggeredAlarmRepository;
import net.urosk.alarm.repositories.WaterLevelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;
    @Mock
    private TriggeredAlarmRepository triggeredAlarmRepository;
    @Mock
    private WaterLevelRepository waterLevelRepository;
    @Mock
    private MessengerService messengerService;

    @InjectMocks
    private AlarmService alarmService;

    // Minimalna implementacija testnega uporabnika
    private static class TestUser {
        private String id;

        public TestUser(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    private Alarm sampleAlarm;
    private WaterLevel sampleWaterLevel;

    @BeforeEach
    public void setUp() {
        // Inicializacija testnega alarma
        sampleAlarm = new Alarm();
        sampleAlarm.setStationId("station1");
        sampleAlarm.setStationName("Postaja 1");
        sampleAlarm.setAlertThresholdFlow(100.0);
        sampleAlarm.setAlertThresholdLevel(50.0);
        // Nastavimo testnega uporabnika
        sampleAlarm.setUser(new User("user1","email"));

        // Inicializacija testne meritve
        sampleWaterLevel = new WaterLevel();
        sampleWaterLevel.setFlow(150.0);
        sampleWaterLevel.setLevel(60.0);
        sampleWaterLevel.setDate(LocalDateTime.now());
    }

    // ******************** Testi za metodo checkFlowAlarms ********************

    // Primer: Če je zadnja sprožena vrednost enaka trenutni vrednosti pretoka, ne pošljemo obvestila.
    @Test
    public void testCheckFlowAlarms_NoNotificationForSameValue() {
        when(alarmRepository.findAlarmsOverThresholdForFlow())
                .thenReturn(Collections.singletonList(sampleAlarm));
        when(waterLevelRepository.findTopByStationIdOrderByDateDesc(sampleAlarm.getStationId()))
                .thenReturn(sampleWaterLevel);
        // Zadnji sproženi alarm ima isto merjeno vrednost pretoka
        TriggeredAlarm lastTriggered = new TriggeredAlarm();
        lastTriggered.setMeasuredValue(sampleWaterLevel.getFlow());
        when(triggeredAlarmRepository.findTopByAlarmOrderByTriggeredAtDesc(sampleAlarm))
                .thenReturn(Optional.of(lastTriggered));

        alarmService.checkFlowAlarms();

        verify(messengerService, never()).sendNotification(any(), anyString());
        verify(triggeredAlarmRepository, never()).save(any(TriggeredAlarm.class));
    }

    // Primer: Če ni prejšnjega sproženega alarma, pošljemo obvestilo in shranimo nov triggered alarm.
    @Test
    public void testCheckFlowAlarms_SendNotificationForDifferentValue() {
        when(alarmRepository.findAlarmsOverThresholdForFlow())
                .thenReturn(Collections.singletonList(sampleAlarm));
        when(waterLevelRepository.findTopByStationIdOrderByDateDesc(sampleAlarm.getStationId()))
                .thenReturn(sampleWaterLevel);
        when(triggeredAlarmRepository.findTopByAlarmOrderByTriggeredAtDesc(sampleAlarm))
                .thenReturn(Optional.empty());

        alarmService.checkFlowAlarms();

        String expectedMessage = "⚠ ALARM za postajo " + sampleAlarm.getStationName() +
                                 ": 💧 pretok presega " + sampleAlarm.getAlertThresholdFlow() +
                                 " (trenutno: " + sampleWaterLevel.getFlow() + " m³/s)";
        verify(messengerService, times(1)).sendNotification(sampleAlarm, expectedMessage);
        verify(triggeredAlarmRepository, times(1)).save(argThat(ta ->
                ta.getMeasuredValue() == sampleWaterLevel.getFlow() &&
                "Spremenjena vrednost".equals(ta.getNote())
        ));
    }

    // Primer: Ko pride do izjeme pri pošiljanju obvestila, triggered alarm se ne shrani.
    @Test
    public void testCheckFlowAlarms_ExceptionInNotification() {
        when(alarmRepository.findAlarmsOverThresholdForFlow())
                .thenReturn(Collections.singletonList(sampleAlarm));
        when(waterLevelRepository.findTopByStationIdOrderByDateDesc(sampleAlarm.getStationId()))
                .thenReturn(sampleWaterLevel);
        when(triggeredAlarmRepository.findTopByAlarmOrderByTriggeredAtDesc(sampleAlarm))
                .thenReturn(Optional.empty());
        doThrow(new RuntimeException("Napaka v messengerService"))
                .when(messengerService).sendNotification(any(), anyString());

        alarmService.checkFlowAlarms();

        verify(triggeredAlarmRepository, never()).save(any(TriggeredAlarm.class));
    }

    // ******************** Testi za metodo checkLevelAlarms ********************

    // Primer: Če je zadnja sprožena vrednost enaka trenutni vrednosti nivoja, ne pošljemo obvestila.
    @Test
    public void testCheckLevelAlarms_NoNotificationForSameValue() {
        when(alarmRepository.findAlarmsOverThresholdForLevel())
                .thenReturn(Collections.singletonList(sampleAlarm));
        when(waterLevelRepository.findTopByStationIdOrderByDateDesc(sampleAlarm.getStationId()))
                .thenReturn(sampleWaterLevel);
        // Zadnji sproženi alarm ima isto merjeno vrednost nivoja
        TriggeredAlarm lastTriggered = new TriggeredAlarm();
        lastTriggered.setMeasuredValue(sampleWaterLevel.getLevel());
        when(triggeredAlarmRepository.findTopByAlarmOrderByTriggeredAtDesc(sampleAlarm))
                .thenReturn(Optional.of(lastTriggered));

        alarmService.checkLevelAlarms();

        verify(messengerService, never()).sendNotification(any(), anyString());
        verify(triggeredAlarmRepository, never()).save(any(TriggeredAlarm.class));
    }

    // Primer: Če ni prejšnjega sproženega alarma, pošljemo obvestilo in shranimo nov triggered alarm.
    @Test
    public void testCheckLevelAlarms_SendNotificationForDifferentValue() {
        when(alarmRepository.findAlarmsOverThresholdForLevel())
                .thenReturn(Collections.singletonList(sampleAlarm));
        when(waterLevelRepository.findTopByStationIdOrderByDateDesc(sampleAlarm.getStationId()))
                .thenReturn(sampleWaterLevel);
        when(triggeredAlarmRepository.findTopByAlarmOrderByTriggeredAtDesc(sampleAlarm))
                .thenReturn(Optional.empty());

        alarmService.checkLevelAlarms();

        String expectedMessage = "⚠ ALARM za postajo " + sampleAlarm.getStationName() +
                                 ": 📏 nivo presega " + sampleAlarm.getAlertThresholdLevel() +
                                 " (trenutno: " + sampleWaterLevel.getLevel() + " cm)";
        verify(messengerService, times(1)).sendNotification(sampleAlarm, expectedMessage);
        verify(triggeredAlarmRepository, times(1)).save(argThat(ta ->
                ta.getMeasuredValue() == sampleWaterLevel.getLevel() &&
                "Spremenjena vrednost".equals(ta.getNote())
        ));
    }

    // Primer: Ko pride do izjeme pri pošiljanju obvestila za nivo, triggered alarm se ne shrani.
    @Test
    public void testCheckLevelAlarms_ExceptionInNotification() {
        when(alarmRepository.findAlarmsOverThresholdForLevel())
                .thenReturn(Collections.singletonList(sampleAlarm));
        when(waterLevelRepository.findTopByStationIdOrderByDateDesc(sampleAlarm.getStationId()))
                .thenReturn(sampleWaterLevel);
        when(triggeredAlarmRepository.findTopByAlarmOrderByTriggeredAtDesc(sampleAlarm))
                .thenReturn(Optional.empty());
        doThrow(new RuntimeException("Napaka v messengerService"))
                .when(messengerService).sendNotification(any(), anyString());

        alarmService.checkLevelAlarms();

        verify(triggeredAlarmRepository, never()).save(any(TriggeredAlarm.class));
    }

    // ******************** Testi za ostale metode ********************

    @Test
    public void testGetAllAlarms() {
        List<Alarm> alarms = Arrays.asList(sampleAlarm, new Alarm());
        when(alarmRepository.findAll()).thenReturn(alarms);

        List<Alarm> result = alarmService.getAllAlarms();

        assertEquals(alarms, result);
    }

    @Test
    public void testFindTop100ByUserId() {
        List<Alarm> alarms = Arrays.asList(sampleAlarm, new Alarm());
        when(alarmRepository.findTop100ByUserId("user1")).thenReturn(alarms);

        List<Alarm> result = alarmService.findTop100ByUserId("user1");

        assertEquals(alarms, result);
        verify(alarmRepository, times(1)).findTop100ByUserId("user1");
    }

    @Test
    public void testSaveAlarm() {
        when(alarmRepository.save(sampleAlarm)).thenReturn(sampleAlarm);

        Alarm result = alarmService.saveAlarm(sampleAlarm);

        assertEquals(sampleAlarm, result);
        verify(alarmRepository, times(1)).save(sampleAlarm);
    }

    @Test
    public void testDeleteAlarm_ExistingAlarm() {
        when(alarmRepository.findById(1L)).thenReturn(Optional.of(sampleAlarm));

        alarmService.deleteAlarm(1L);

        verify(triggeredAlarmRepository, times(1)).deleteAllByAlarm(sampleAlarm);
        verify(alarmRepository, times(1)).delete(sampleAlarm);
    }

    @Test
    public void testDeleteAlarm_NonExistingAlarm() {
        when(alarmRepository.findById(1L)).thenReturn(Optional.empty());

        alarmService.deleteAlarm(1L);

        verify(triggeredAlarmRepository, never()).deleteAllByAlarm(any());
        verify(alarmRepository, never()).delete(any());
    }

    @Test
    public void testFindByUserIdAndByStationId() {
        List<Alarm> alarms = Arrays.asList(sampleAlarm, new Alarm());
        when(alarmRepository.findByUserIdAndStationId("user1", "station1"))
                .thenReturn(alarms);

        List<Alarm> result = alarmService.findByUserIdAndByStationId("user1", "station1");

        assertEquals(alarms, result);
        verify(alarmRepository, times(1)).findByUserIdAndStationId("user1", "station1");
    }
}
