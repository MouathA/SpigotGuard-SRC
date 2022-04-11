package xyz.yooniks.spigotguard.api.inventory;

import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import java.util.*;

public class PhasmatosBukkitInventoryAPI implements PhasmatosInventoryAPI
{
    private final List<PhasmatosInventory> inventories;
    
    @Override
    public void addInventory(final PhasmatosInventory phasmatosInventory) {
        this.inventories.add(phasmatosInventory);
    }
    
    private static boolean lambda$findByTitleAndSize$1(final String s, final int n, final PhasmatosInventory phasmatosInventory) {
        return Integer.valueOf(s.toUpperCase().hashCode()).equals(phasmatosInventory.getTitle().toUpperCase().hashCode()) && phasmatosInventory.getSize() == n;
    }
    
    @Override
    public PhasmatosInventory findByTitle(final String s) {
        return this.inventories.stream().filter(PhasmatosBukkitInventoryAPI::lambda$findByTitle$0).findFirst().orElse(null);
    }
    
    @Override
    public PhasmatosInventory findByTitleAndSize(final String s, final int n) {
        return this.inventories.stream().filter(PhasmatosBukkitInventoryAPI::lambda$findByTitleAndSize$1).findFirst().orElse(null);
    }
    
    public void register(final Plugin plugin) {
        final PluginManager pluginManager = plugin.getServer().getPluginManager();
        final String version = Bukkit.getServer().getVersion();
        if (version.contains("1.14") || version.contains("1.15") || version.contains("1.16")) {
            pluginManager.registerEvents((Listener)new PhasmatosInventoryListeners_14(this), plugin);
        }
        else {
            pluginManager.registerEvents((Listener)new PhasmatosInventoryListeners(this), plugin);
        }
    }
    
    private static boolean lambda$findByTitle$0(final String s, final PhasmatosInventory phasmatosInventory) {
        return Integer.valueOf(s.toUpperCase().hashCode()).equals(phasmatosInventory.getTitle().toUpperCase().hashCode());
    }
    
    @Override
    public List<PhasmatosInventory> getInventories() {
        return new ArrayList<PhasmatosInventory>(this.inventories);
    }
    
    public PhasmatosBukkitInventoryAPI() {
        this.inventories = new ArrayList<PhasmatosInventory>();
    }
}
