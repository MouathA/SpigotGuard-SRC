package xyz.yooniks.spigotguard.api.inventory;

import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import java.util.*;

public interface PhasmatosInventory
{
    void open(final Player p0);
    
    PhasmatosInventory addItem(final int p0, final ItemStack p1);
    
    Map<Integer, ItemStack> getItems();
    
    int getSize();
    
    String getTitle();
}
