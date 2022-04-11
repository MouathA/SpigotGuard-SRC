package xyz.yooniks.spigotguard.api.inventory;

import org.bukkit.event.inventory.*;
import java.util.function.*;
import org.bukkit.entity.*;

public interface PhasmatosClickableInventory
{
    void onClick(final InventoryClickEvent p0);
    
    PhasmatosClickableInventory addItemAction(final int p0, final Consumer<Player> p1);
}
