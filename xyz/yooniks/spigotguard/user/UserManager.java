package xyz.yooniks.spigotguard.user;

import org.bukkit.entity.*;
import java.util.concurrent.*;
import java.util.*;

public class UserManager
{
    private final Map<UUID, User> userMap;
    
    public User findByUuid(final UUID uuid) {
        return this.userMap.get(uuid);
    }
    
    public UserManager() {
        this.userMap = new ConcurrentHashMap<UUID, User>();
    }
    
    public void removeUser(final UUID uuid) {
        this.userMap.remove(uuid);
    }
    
    private static boolean lambda$findByName$0(final String s, final User user) {
        return Integer.valueOf(s.toUpperCase().hashCode()).equals(user.getName().toUpperCase().hashCode());
    }
    
    public void addUser(final User user) {
        if (this.findByName(user.getName()) != null) {
            return;
        }
        this.userMap.put(user.getUuid(), user);
    }
    
    public User findByName(final String s) {
        return this.getUsers().stream().filter(UserManager::lambda$findByName$0).findFirst().orElse(null);
    }
    
    public User findOrCreate(final Player player) {
        User user = this.userMap.get(player.getUniqueId());
        if (user == null) {
            this.userMap.put(player.getUniqueId(), user = new User(player.getName(), player.getAddress().getAddress().getHostAddress(), player.getUniqueId()));
        }
        return user;
    }
    
    public List<User> getUsers() {
        return new CopyOnWriteArrayList<User>(this.userMap.values());
    }
}
