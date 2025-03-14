package net.urosk.alarm.repositories;

import net.urosk.alarm.models.PushSubscription;
import net.urosk.alarm.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, String> {
    List<PushSubscription> findByUser_Id(
            String userId);

    Optional<PushSubscription> findByUserAndDeviceName(User user, String deviceName);

}