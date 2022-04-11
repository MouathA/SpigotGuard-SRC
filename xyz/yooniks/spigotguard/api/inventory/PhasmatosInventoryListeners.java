package xyz.yooniks.spigotguard.api.inventory;

import org.bukkit.event.inventory.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.event.*;

public class PhasmatosInventoryListeners implements Listener
{
    private final PhasmatosInventoryAPI inventoryAPI;
    
    @EventHandler
    public void onClick(final InventoryClickEvent inventoryClickEvent) {
        if (!(inventoryClickEvent.getWhoClicked() instanceof Player)) {
            return;
        }
        final Player player = (Player)inventoryClickEvent.getWhoClicked();
        final Inventory inventory = inventoryClickEvent.getInventory();
        if (inventory == null || inventory.equals(player.getInventory())) {
            return;
        }
        final PhasmatosInventory byTitleAndSize = this.inventoryAPI.findByTitleAndSize(inventoryClickEvent.getView().getTitle(), inventory.getSize());
        if (byTitleAndSize instanceof PhasmatosClickableInventory) {
            ((PhasmatosClickableInventory)byTitleAndSize).onClick(inventoryClickEvent);
        }
    }
    
    public PhasmatosInventoryListeners(final PhasmatosInventoryAPI inventoryAPI) {
        this.inventoryAPI = inventoryAPI;
    }
}
