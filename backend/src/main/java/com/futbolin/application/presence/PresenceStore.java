package com.futbolin.application.presence;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface PresenceStore {
    void heartbeat(UUID userId);
    void offline(UUID userId);
    boolean isOnline(UUID userId);
    Set<UUID> onlineAmong(Collection<UUID> ids);
}
