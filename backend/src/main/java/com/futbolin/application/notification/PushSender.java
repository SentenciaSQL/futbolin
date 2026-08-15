package com.futbolin.application.notification;

import java.util.Map;

public interface PushSender {
    void send(String deviceToken, String title, String body, Map<String, String> data);
}
