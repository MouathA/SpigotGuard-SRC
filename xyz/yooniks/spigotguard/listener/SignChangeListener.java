package xyz.yooniks.spigotguard.listener;

import org.bukkit.event.block.*;
import xyz.yooniks.spigotguard.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class SignChangeListener implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent signChangeEvent) {
        SpigotGuardPlugin.getInstance().getSpigotGuardClassLoaded().getSerializable();
        final Player player = signChangeEvent.getPlayer();
        final String[] lines = signChangeEvent.getLines();
        for (int length = lines.length, i = 0; i < length; ++i) {
            if (lines[i].length() >= 46) {
                signChangeEvent.setCancelled(true);
                SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(player.getName()).append(" -> Too long sign line!")), new Object[0]);
                return;
            }
        }
    }
}
