package xyz.yooniks.spigotguard.notification;

import xyz.yooniks.spigotguard.event.*;
import java.util.*;

public class NotificationCache_1_7 implements NotificationCache
{
    private final Map<UUID, ExploitDetails> hackers;
    
    @Override
    public void addCache(final UUID uuid, final ExploitDetails exploitDetails) {
        this.hackers.put(uuid, exploitDetails);
    }
    
    public NotificationCache_1_7() {
        this.hackers = new HashMap<UUID, ExploitDetails>();
    }
    
    @Override
    public void removeCache(final UUID uuid) {
        this.hackers.remove(uuid);
    }
    
    @Override
    public ExploitDetails findCache(final UUID uuid) {
        return this.hackers.get(uuid);
    }
}
