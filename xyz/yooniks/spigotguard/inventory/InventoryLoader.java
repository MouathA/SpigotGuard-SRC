package xyz.yooniks.spigotguard.inventory;

import org.bukkit.entity.*;
import xyz.yooniks.spigotguard.config.*;
import xyz.yooniks.spigotguard.*;
import java.io.*;
import xyz.yooniks.spigotguard.helper.*;
import xyz.yooniks.spigotguard.event.*;
import xyz.yooniks.spigotguard.nms.*;
import java.util.stream.*;
import xyz.yooniks.spigotguard.user.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
import java.util.function.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import java.text.*;
import xyz.yooniks.spigotguard.api.inventory.*;
import org.bukkit.plugin.*;
import java.util.*;

public class InventoryLoader
{
    private static void lambda$loadInventories$0(final Player player) {
        player.closeInventory();
        Settings.IMP.reload(new File(SpigotGuardPlugin.getInstance().getDataFolder(), "settings.yml"));
        player.sendMessage(new MessageBuilder("&cSpigotGuard &8> &7Reloaded settings.yml! Remember that config.yml is not reloaded! You need to restart server to reload config.yml").coloured().toString());
    }
    
    private static void lambda$loadInventories$4(final DateFormat dateFormat, final SpigotGuardPlugin spigotGuardPlugin, final Inventory inventory, final ExploitDetails exploitDetails) {
        final User byUuid = SpigotGuardPlugin.getInstance().getUserManager().findByUuid(exploitDetails.getUserId());
        inventory.addItem(new ItemStack[] { new ItemBuilder((spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_FIVETEEN || spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_SIXTEEN || spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_SIXTEEN_R2 || spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_SIXTEEN_R3 || spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_THIRTEEN || spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_SEVENTEEN_R1) ? Material.valueOf("PLAYER_HEAD") : Material.valueOf("SKULL_ITEM"), 1, (short)3).withName(Settings.IMP.INVENTORIES.RECENT_DETECTIONS_INVENTORY.RECENT_DETECTION_ITEM.NAME.replace("{PLAYER-NAME}", byUuid.getName())).withLore((List<String>)Settings.IMP.INVENTORIES.RECENT_DETECTIONS_INVENTORY.RECENT_DETECTION_ITEM.LORE.stream().map((Function<? super Object, ?>)InventoryLoader::lambda$loadInventories$3).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList())).build() });
    }
    
    private static void lambda$loadInventories$5(final SpigotGuardPlugin spigotGuardPlugin, final DateFormat dateFormat, final ItemStack itemStack, final Player player) {
        player.closeInventory();
        final Inventory inventory = Bukkit.createInventory((InventoryHolder)null, Settings.IMP.INVENTORIES.RECENT_DETECTIONS_INVENTORY.SIZE, MessageHelper.colored(Settings.IMP.INVENTORIES.RECENT_DETECTIONS_INVENTORY.NAME));
        final ArrayList list = new ArrayList();
        spigotGuardPlugin.getUserManager().getUsers().forEach((Consumer<? super Object>)InventoryLoader::lambda$loadInventories$1);
        list.sort(InventoryLoader::lambda$loadInventories$2);
        list.forEach(InventoryLoader::lambda$loadInventories$4);
        if (list.size() == 0 && Settings.IMP.INVENTORIES.RECENT_DETECTIONS_INVENTORY.SIZE > 26) {
            inventory.setItem(13, new ItemBuilder(Material.BARRIER).withName(Settings.IMP.INVENTORIES.RECENT_DETECTIONS_INVENTORY.NO_DETECTIONS_ITEM.NAME).withLore(Settings.IMP.INVENTORIES.RECENT_DETECTIONS_INVENTORY.NO_DETECTIONS_ITEM.LORE).build());
        }
        player.openInventory(inventory);
        for (int i = 0; i < inventory.getSize(); ++i) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, itemStack);
            }
        }
    }
    
    private static void lambda$loadInventories$1(final List list, final User user) {
        list.addAll(user.getAttempts());
    }
    
    private static String lambda$loadInventories$3(final ExploitDetails exploitDetails, final DateFormat dateFormat, final User user, final String s) {
        return new MessageBuilder(s).withField("{PACKET}", exploitDetails.getPacket()).withField("{DETAILS}", exploitDetails.getDetails()).withField("{PLAYER-LAST-SEEN}", dateFormat.format(new Date(user.getLastJoin()))).withField("{TIME}", dateFormat.format(new Date(exploitDetails.getTime()))).withField("{HOW-TO-FIX-FALSE-POSITIVE}", exploitDetails.getHowToFixFalseDetect()).toString();
    }
    
    private static int lambda$loadInventories$2(final ExploitDetails exploitDetails, final ExploitDetails exploitDetails2) {
        return new Date(exploitDetails2.getTime()).compareTo(new Date(exploitDetails.getTime()));
    }
    
    public void loadInventories(final SpigotGuardPlugin spigotGuardPlugin) {
        if (Settings.IMP.INVENTORIES.PLAYER_INFO_INVENTORY.NAME.length() < 7) {
            SpigotGuardLogger.log(Level.WARNING, "player-info-inventory in settings.yml MUST have at least 7 chars! Inventory will not work!", new Object[0]);
        }
        final PhasmatosBukkitInventoryAPI phasmatosBukkitInventoryAPI = new PhasmatosBukkitInventoryAPI();
        ItemStack itemStack;
        if (spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_THIRTEEN || spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_FOURTEEN || spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_FIVETEEN || spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_SIXTEEN || spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_SIXTEEN_R2 || spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_SIXTEEN_R3 || spigotGuardPlugin.getNmsVersion() == NMSVersion.ONE_DOT_SEVENTEEN_R1) {
            itemStack = new ItemStack(Material.valueOf("GRAY_STAINED_GLASS_PANE"));
        }
        else {
            itemStack = new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short)7);
        }
        final SpigotGuardMainInventory managementInventory = new SpigotGuardMainInventory(MessageHelper.colored(Settings.IMP.INVENTORIES.MAIN_INVENTORY.NAME), Settings.IMP.INVENTORIES.MAIN_INVENTORY.SIZE);
        managementInventory.addItem(Settings.IMP.INVENTORIES.MAIN_INVENTORY.RELOAD_ITEM.SLOT, new ItemBuilder(Material.matchMaterial(Settings.IMP.INVENTORIES.MAIN_INVENTORY.RELOAD_ITEM.MATERIAL)).withName(Settings.IMP.INVENTORIES.MAIN_INVENTORY.RELOAD_ITEM.NAME).withLore(Settings.IMP.INVENTORIES.MAIN_INVENTORY.RELOAD_ITEM.LORE).build());
        managementInventory.addItemAction(Settings.IMP.INVENTORIES.MAIN_INVENTORY.RELOAD_ITEM.SLOT, InventoryLoader::lambda$loadInventories$0);
        managementInventory.addItem(Settings.IMP.INVENTORIES.MAIN_INVENTORY.RECENT_DETECTIONS_ITEM.SLOT, new ItemBuilder(Material.matchMaterial(Settings.IMP.INVENTORIES.MAIN_INVENTORY.RECENT_DETECTIONS_ITEM.MATERIAL)).withName(Settings.IMP.INVENTORIES.MAIN_INVENTORY.RECENT_DETECTIONS_ITEM.NAME).withLore(Settings.IMP.INVENTORIES.MAIN_INVENTORY.RECENT_DETECTIONS_ITEM.LORE).build());
        managementInventory.addItemAction(Settings.IMP.INVENTORIES.MAIN_INVENTORY.RECENT_DETECTIONS_ITEM.SLOT, InventoryLoader::lambda$loadInventories$5);
        final Map<Integer, ItemStack> items = managementInventory.getItems();
        for (int i = 0; i < managementInventory.getSize(); ++i) {
            if (!items.containsKey(i)) {
                items.put(i, itemStack);
            }
        }
        phasmatosBukkitInventoryAPI.addInventory(managementInventory);
        spigotGuardPlugin.setManagementInventory(managementInventory);
        phasmatosBukkitInventoryAPI.register((Plugin)spigotGuardPlugin);
    }
}
