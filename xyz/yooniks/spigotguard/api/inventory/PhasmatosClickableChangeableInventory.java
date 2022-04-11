package xyz.yooniks.spigotguard.api.inventory;

import java.util.function.*;
import org.bukkit.entity.*;
import java.util.*;
import org.bukkit.event.inventory.*;

public abstract class PhasmatosClickableChangeableInventory extends PhasmatosChangeableInventory implements PhasmatosClickableInventory
{
    private final Map<Integer, Consumer<Player>> itemActions;
    
    public PhasmatosClickableChangeableInventory(final String s, final int n) {
        super(s, n);
        this.itemActions = new HashMap<Integer, Consumer<Player>>();
    }
    
    @Override
    public void onClick(final InventoryClickEvent inventoryClickEvent) {
        if (!(inventoryClickEvent.getWhoClicked() instanceof Player)) {
            return;
        }
        inventoryClickEvent.setCancelled(true);
        if (this.itemActions.containsKey(inventoryClickEvent.getSlot())) {
            this.itemActions.get(inventoryClickEvent.getSlot()).accept((Player)inventoryClickEvent.getWhoClicked());
        }
    }
    
    @Override
    public PhasmatosClickableInventory addItemAction(final int n, final Consumer<Player> consumer) {
        this.itemActions.put(n, consumer);
        return this;
    }
}
