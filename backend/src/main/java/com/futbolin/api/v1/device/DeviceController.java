package com.futbolin.api.v1.device;

import com.futbolin.application.notification.NotificationService;
import com.futbolin.core.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final NotificationService notifications;

    public DeviceController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @PostMapping
    public Map<String, Object> register(@AuthenticationPrincipal UserPrincipal principal,
                                        @RequestBody Map<String, String> body) {
        notifications.registerDevice(principal.id(), body.get("token"), body.get("platform"));
        return Map.of("status", "REGISTERED");
    }
}
