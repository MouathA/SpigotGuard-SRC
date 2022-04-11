package xyz.yooniks.spigotguard.api.inventory;

import org.bukkit.entity.*;
import org.bukkit.*;
import java.util.function.*;
import org.bukkit.inventory.*;
import java.util.*;

public abstract class PhasmatosChangeableInventory implements PhasmatosInventory
{
    private final String title;
    private final Map<Integer, ItemStack> items;
    private final int size;
    
    @Override
    public String getTitle() {
        return this.title;
    }
    
    @Override
    public void open(final Player player) {
        final Inventory inventory = Bukkit.createInventory((InventoryHolder)null, this.size, this.title);
        this.items.forEach((BiConsumer<? super Integer, ? super ItemStack>)this::lambda$open$0);
        player.openInventory(inventory);
    }
    
    @Override
    public Map<Integer, ItemStack> getItems() {
        return new HashMap<Integer, ItemStack>(this.items);
    }
    
    public PhasmatosChangeableInventory(final String title, final int size) {
        this.items = new HashMap<Integer, ItemStack>();
        this.title = title;
        this.size = size;
    }
    
    public abstract ItemStack updateItem(final ItemStack p0, final int p1, final Player p2);
    
    private void lambda$open$0(final Inventory inventory, final Player player, final Integer n, ItemStack updateItem) {
        inventory.setItem((int)n, updateItem = this.updateItem(updateItem, n, player));
    }
    
    @Override
    public int getSize() {
        return this.size;
    }
    
    @Override
    public PhasmatosInventory addItem(final int n, final ItemStack itemStack) {
        this.items.put(n, itemStack);
        return this;
    }
}
