package com.futbolin.application.notification;

import com.futbolin.core.props.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LoggingPushSender implements PushSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingPushSender.class);
    private final AppProperties properties;

    public LoggingPushSender(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public void send(String deviceToken, String title, String body, Map<String, String> data) {
        if (properties.fcm() != null && properties.fcm().projectId() != null && !properties.fcm().projectId().isBlank()) {
            log.info("FCM project {} would push '{}' to token {}… data={}",
                    properties.fcm().projectId(), title, deviceToken.substring(0, Math.min(12, deviceToken.length())), data);
            return;
        }
        log.info("Push (in-app only) title={} tokenPrefix={} data={}", title,
                deviceToken.substring(0, Math.min(8, deviceToken.length())), data);
    }
}
