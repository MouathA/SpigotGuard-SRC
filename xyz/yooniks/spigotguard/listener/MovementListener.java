package xyz.yooniks.spigotguard.listener;

import org.bukkit.event.player.*;
import xyz.yooniks.spigotguard.config.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import org.bukkit.*;
import org.bukkit.event.*;

public class MovementListener implements Listener
{
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent playerMoveEvent) {
        final Location to = playerMoveEvent.getTo();
        final Chunk chunk = to.getChunk();
        if ((Settings.IMP.POSITION_CHECKS.PREVENT_MOVING_INTO_UNLOADED_CHUNKS && !chunk.isLoaded()) || !to.getWorld().isChunkLoaded(chunk)) {
            playerMoveEvent.setCancelled(true);
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Prevented player ").append(playerMoveEvent.getPlayer().getName()).append(" from moving into unloaded chunk.")), new Object[0]);
        }
    }
}
