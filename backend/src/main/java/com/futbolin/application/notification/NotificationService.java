package com.futbolin.application.notification;

import com.futbolin.data.entity.DeviceTokenEntity;
import com.futbolin.data.entity.NotificationEntity;
import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.data.repository.DeviceTokenRepository;
import com.futbolin.data.repository.NotificationRepository;
import com.futbolin.data.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifications;
    private final DeviceTokenRepository devices;
    private final UserProfileRepository profiles;
    private final PushSender pushSender;

    public NotificationService(
            NotificationRepository notifications,
            DeviceTokenRepository devices,
            UserProfileRepository profiles,
            PushSender pushSender
    ) {
        this.notifications = notifications;
        this.devices = devices;
        this.profiles = profiles;
        this.pushSender = pushSender;
    }

    @Transactional
    public void registerDevice(UUID userId, String token, String platform) {
        DeviceTokenEntity entity = devices.findByToken(token).orElseGet(DeviceTokenEntity::new);
        entity.setUserId(userId);
        entity.setToken(token);
        entity.setPlatform(platform == null ? "ANDROID" : platform.toUpperCase());
        devices.save(entity);
    }

    @Transactional
    public void notify(UUID userId, String type, String titleEs, String titleEn, String bodyEs, String bodyEn) {
        NotificationEntity n = new NotificationEntity();
        n.setUserId(userId);
        n.setType(type);
        n.setTitleEs(titleEs);
        n.setTitleEn(titleEn);
        n.setBodyEs(bodyEs);
        n.setBodyEn(bodyEn);
        notifications.save(n);
        for (DeviceTokenEntity device : devices.findByUserId(userId)) {
            pushSender.send(device.getToken(), titleEs, bodyEs, Map.of("type", type));
        }
    }

    public List<NotificationEntity> list(UUID userId) {
        return notifications.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Scheduled(cron = "0 0 12 * * *")
    @Transactional
    public void dailyRewardReminders() {
        LocalDate today = LocalDate.now();
        Instant since = Instant.now().minus(20, ChronoUnit.HOURS);
        for (UserProfileEntity profile : profiles.findAll()) {
            if (today.equals(profile.getLastDailyClaim())) {
                continue;
            }
            if (notifications.countByUserIdAndTypeAndCreatedAtAfter(profile.getUserId(), "DAILY_REWARD", since) > 0) {
                continue;
            }
            notify(profile.getUserId(), "DAILY_REWARD",
                    "Recompensa diaria disponible", "Daily reward available",
                    "Entra y reclama tu racha.", "Come back and claim your streak.");
        }
        log.debug("Daily reward reminders processed");
    }
}
