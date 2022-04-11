package xyz.yooniks.spigotguard.command;

import xyz.yooniks.spigotguard.event.*;
import xyz.yooniks.spigotguard.user.*;
import xyz.yooniks.spigotguard.helper.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import xyz.yooniks.spigotguard.*;
import xyz.yooniks.spigotguard.nms.*;
import java.text.*;
import xyz.yooniks.spigotguard.config.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
import java.util.*;
import xyz.yooniks.spigotguard.api.inventory.*;
import java.util.function.*;
import java.util.stream.*;

public class SpigotGuardCommand implements CommandExecutor
{
    private final String message;
    private final PhasmatosInventory inventory;
    
    private static String lambda$onCommand$1(final ExploitDetails exploitDetails, final DateFormat dateFormat, final User user, final String s) {
        return new MessageBuilder(s).withField("{PACKET}", exploitDetails.getPacket()).withField("{DETAILS}", exploitDetails.getDetails()).withField("{LAST-SEEN}", dateFormat.format(new Date(user.getLastJoin()))).withField("{TIME}", dateFormat.format(new Date(exploitDetails.getTime()))).withField("{IP}", user.getIp()).withField("{HOW-TO-FIX-FALSE-POSITIVE}", exploitDetails.getHowToFixFalseDetect()).toString();
    }
    
    private static int lambda$onCommand$0(final ExploitDetails exploitDetails, final ExploitDetails exploitDetails2) {
        return new Date(exploitDetails2.getTime()).compareTo(new Date(exploitDetails.getTime()));
    }
    
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] array) {
        if (!commandSender.hasPermission("spigotguard.command")) {
            commandSender.sendMessage(this.message);
            return true;
        }
        if (commandSender instanceof Player) {
            if (array.length > 0) {
                final User byName = SpigotGuardPlugin.getInstance().getUserManager().findByName(array[0]);
                if (byName == null) {
                    commandSender.sendMessage(new MessageBuilder(String.valueOf(new StringBuilder().append("&6[&eSpigotGuard&6] &cUser &e").append(array[0]).append("&c has never been here"))).coloured().toString());
                    return true;
                }
                if (byName.getAttempts().size() == 0) {
                    commandSender.sendMessage(new MessageBuilder(String.valueOf(new StringBuilder().append("&6[&eSpigotGuard&6] &cUser &e").append(array[0]).append("&c has never tried to crash the server"))).coloured().toString());
                    return true;
                }
                final ItemStack itemStack = new ItemStack((SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_THIRTEEN || SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_FOURTEEN || SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_FIVETEEN || SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_SIXTEEN || SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_SIXTEEN_R2 || SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_SIXTEEN_R3 || SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_SEVENTEEN_R1) ? Material.valueOf("GRAY_STAINED_GLASS_PANE") : Material.valueOf("STAINED_GLASS_PANE"), 1, (short)5);
                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                final Inventory inventory = Bukkit.createInventory((InventoryHolder)null, Settings.IMP.INVENTORIES.PLAYER_INFO_INVENTORY.SIZE, MessageHelper.colored(Settings.IMP.INVENTORIES.PLAYER_INFO_INVENTORY.NAME.replace("{PLAYER}", byName.getName())));
                final List<ExploitDetails> attempts = byName.getAttempts();
                attempts.sort(SpigotGuardCommand::lambda$onCommand$0);
                attempts.forEach(SpigotGuardCommand::lambda$onCommand$2);
                for (int i = 0; i < inventory.getSize(); ++i) {
                    if (inventory.getItem(i) == null) {
                        inventory.setItem(i, itemStack);
                    }
                }
                ((Player)commandSender).openInventory(inventory);
                return true;
            }
            else {
                this.inventory.open((Player)commandSender);
            }
        }
        return true;
    }
    
    public SpigotGuardCommand(final PhasmatosInventory inventory) {
        this.message = MessageBuilder.newBuilder(String.valueOf(new StringBuilder().append("\n&7This server is protected with &cSpigotGuard&7 &8(&c").append(SpigotGuardPlugin.getInstance().getDescription().getVersion()).append("&8)\n&7More about SpigotGuard: &6https://mc-protection.eu\n&7Buy here: &6https://mc-protection.eu&7The most advanced &6AntiCrash&7 created by &6yooniks\n\n&7Our discord: &6https://mc-protection.eu/discord\n"))).coloured().toString();
        this.inventory = inventory;
    }
    
    private static void lambda$onCommand$2(final DateFormat dateFormat, final User user, final Inventory inventory, final ExploitDetails exploitDetails) {
        inventory.addItem(new ItemStack[] { new ItemBuilder((SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_FIVETEEN || SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_SIXTEEN || SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_SIXTEEN_R2 || SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_SIXTEEN_R3 || SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_SEVENTEEN_R1) ? Material.valueOf("PLAYER_HEAD") : Material.valueOf("SKULL_ITEM"), 1, (short)3).withName(Settings.IMP.INVENTORIES.PLAYER_INFO_INVENTORY.CRASH_ATTEMPT_ITEM.NAME.replace("{TIME}", dateFormat.format(new Date(exploitDetails.getTime())))).withLore((List<String>)Settings.IMP.INVENTORIES.PLAYER_INFO_INVENTORY.CRASH_ATTEMPT_ITEM.LORE.stream().map((Function<? super Object, ?>)SpigotGuardCommand::lambda$onCommand$1).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList())).build() });
    }
}
