package xyz.yooniks.spigotguard.thread;

import xyz.yooniks.spigotguard.user.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import java.util.*;

public class SpigotGuardUninjector implements Runnable
{
    private final List<User> users;
    
    public SpigotGuardUninjector(final List<User> users) {
        this.users = users;
    }
    
    @Override
    public void run() {
        SpigotGuardLogger.log(Level.INFO, "Uninjecting packet listeners of online users..", new Object[0]);
        for (final User user : this.users) {
            if (user.getPacketInjector() == null) {
                continue;
            }
            user.getPacketInjector().uninjectListener();
        }
        SpigotGuardLogger.log(Level.INFO, "Uninjected packet listeners of online users!", new Object[0]);
    }
}
