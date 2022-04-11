package xyz.yooniks.spigotguard.listener;

import org.bukkit.event.player.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import org.bukkit.util.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class MoveListenerTests implements Listener
{
    @EventHandler
    private void onMove(final PlayerMoveEvent playerMoveEvent) {
        final Player player = playerMoveEvent.getPlayer();
        if (playerMoveEvent.getFrom().distance(playerMoveEvent.getTo()) < 0.0) {
            player.teleport(playerMoveEvent.getFrom());
            player.kickPlayer("MoveExploit #1");
            SpigotGuardLogger.log(Level.WARNING, String.valueOf(new StringBuilder().append("Exploit detected, (Move #1 - player: ").append(player.getName()).append(")")), new Object[0]);
            return;
        }
        if (playerMoveEvent.getFrom().distance(playerMoveEvent.getTo()) > 18.0) {
            player.teleport(playerMoveEvent.getFrom());
            player.kickPlayer("MoveExploit #2");
            SpigotGuardLogger.log(Level.WARNING, String.valueOf(new StringBuilder().append("Exploit detected, (Move #2 - player: ").append(player.getName()).append(")")), new Object[0]);
            return;
        }
        try {
            player.getPlayer().getWorld().getBlockAt(NumberConversions.floor(player.getLocation().getX()), NumberConversions.floor(player.getLocation().getY()), NumberConversions.floor(player.getLocation().getZ()));
        }
        catch (Exception ex) {
            player.teleport(playerMoveEvent.getFrom());
            player.kickPlayer("MoveExploit #3");
            SpigotGuardLogger.log(Level.WARNING, String.valueOf(new StringBuilder().append("Exploit detected, (Move #3 - player: ").append(player.getName()).append(")")), new Object[0]);
        }
    }
}
