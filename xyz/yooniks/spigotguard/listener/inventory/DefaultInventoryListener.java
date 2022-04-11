package xyz.yooniks.spigotguard.listener.inventory;

import org.bukkit.event.inventory.*;
import org.bukkit.entity.*;
import xyz.yooniks.spigotguard.config.*;
import xyz.yooniks.spigotguard.api.inventory.*;
import org.bukkit.event.*;

public class DefaultInventoryListener implements Listener
{
    @EventHandler
    public void onClick(final InventoryClickEvent inventoryClickEvent) {
        if (!(inventoryClickEvent.getWhoClicked() instanceof Player) || inventoryClickEvent.getView() == null) {
            return;
        }
        final String title = inventoryClickEvent.getInventory().getTitle();
        if (title == null || title.length() <= 0) {
            return;
        }
        if (Integer.valueOf(MessageHelper.colored(Settings.IMP.INVENTORIES.RECENT_DETECTIONS_INVENTORY.NAME).toUpperCase().hashCode()).equals(title.toUpperCase().hashCode())) {
            inventoryClickEvent.setCancelled(true);
        }
        if (title.length() >= 7 && MessageHelper.colored(Settings.IMP.INVENTORIES.PLAYER_INFO_INVENTORY.NAME).contains(title.substring(0, 7))) {
            inventoryClickEvent.setCancelled(true);
        }
    }
}
