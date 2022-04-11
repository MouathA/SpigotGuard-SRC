package xyz.yooniks.spigotguard.notification;

import java.util.*;
import xyz.yooniks.spigotguard.event.*;

public interface NotificationCache
{
    ExploitDetails findCache(final UUID p0);
    
    void addCache(final UUID p0, final ExploitDetails p1);
    
    void removeCache(final UUID p0);
}
