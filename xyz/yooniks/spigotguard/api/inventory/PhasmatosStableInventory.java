package xyz.yooniks.spigotguard.api.inventory;

import org.bukkit.entity.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
import java.util.*;

public class PhasmatosStableInventory implements PhasmatosInventory
{
    private final String name;
    private final Inventory inventory;
    
    @Override
    public int getSize() {
        return this.inventory.getSize();
    }
    
    @Override
    public void open(final Player player) {
        player.openInventory(this.inventory);
    }
    
    @Override
    public PhasmatosInventory addItem(final int n, final ItemStack itemStack) {
        this.inventory.setItem(n, itemStack);
        return this;
    }
    
    public PhasmatosStableInventory(final String name, final int n) {
        this.inventory = Bukkit.createInventory((InventoryHolder)null, n, name);
        this.name = name;
    }
    
    @Override
    public Map<Integer, ItemStack> getItems() {
        final HashMap<Integer, ItemStack> hashMap = new HashMap<Integer, ItemStack>();
        for (int i = 0; i < this.inventory.getSize(); ++i) {
            final ItemStack item = this.inventory.getItem(i);
            if (item != null) {
                hashMap.put(i, item);
            }
        }
        return hashMap;
    }
    
    @Override
    public String getTitle() {
        return this.name;
    }
}
