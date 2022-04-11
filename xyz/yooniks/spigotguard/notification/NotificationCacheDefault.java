package xyz.yooniks.spigotguard.notification;

import java.util.*;
import xyz.yooniks.spigotguard.event.*;
import com.google.common.cache.*;
import java.util.concurrent.*;

public class NotificationCacheDefault implements NotificationCache
{
    private final Cache<UUID, ExploitDetails> hackers;
    
    @Override
    public void removeCache(final UUID uuid) {
        this.hackers.invalidate((Object)uuid);
    }
    
    public NotificationCacheDefault() {
        this.hackers = (Cache<UUID, ExploitDetails>)CacheBuilder.newBuilder().expireAfterWrite(10L, TimeUnit.MINUTES).build();
    }
    
    @Override
    public void addCache(final UUID uuid, final ExploitDetails exploitDetails) {
        this.hackers.put((Object)uuid, (Object)exploitDetails);
    }
    
    @Override
    public ExploitDetails findCache(final UUID uuid) {
        return (ExploitDetails)this.hackers.getIfPresent((Object)uuid);
    }
}
