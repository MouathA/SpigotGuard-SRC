package xyz.yooniks.spigotguard.listener;

import xyz.yooniks.spigotguard.notification.*;
import org.bukkit.entity.*;
import xyz.yooniks.spigotguard.config.*;
import xyz.yooniks.spigotguard.helper.*;
import net.md_5.bungee.api.chat.*;
import xyz.yooniks.spigotguard.event.*;
import org.bukkit.event.player.*;
import xyz.yooniks.spigotguard.*;
import xyz.yooniks.spigotguard.nms.*;
import org.bukkit.*;
import org.bukkit.plugin.*;
import org.bukkit.event.*;
import org.bukkit.command.*;
import java.util.function.*;
import java.util.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import xyz.yooniks.spigotguard.user.*;

public class NotificationListener implements Listener
{
    private final NotificationCache notificationCache;
    
    public NotificationListener(final NotificationCache notificationCache) {
        this.notificationCache = notificationCache;
    }
    
    private void lambda$onJoin$4(final Player player, final ExploitDetails exploitDetails) {
        BookUtil.openPlayer(player, BookUtil.writtenBook().author("yooniks").title("SpigotGuard").pages(new BaseComponent[][] { { (BaseComponent)new TextComponent(MessageBuilder.newBuilder(Settings.IMP.HACKER_BOOK.CONTENT).stripped().withField("{PLAYER}", player.getName()).coloured().toString()) }, new BookUtil.PageBuilder().add((BaseComponent)new TextComponent(MessageBuilder.newBuilder(String.valueOf(new StringBuilder().append("&7Packet: &c").append(exploitDetails.getPacket()))).coloured().toString())).newLine().add((BaseComponent)new TextComponent(MessageBuilder.newBuilder("&7Blocked by: &cSpigotGuard").coloured().toString())).newLine().add(BookUtil.TextBuilder.of(MessageBuilder.newBuilder("&7Open &bSpigotGuard&7 website&r").coloured().toString()).color(ChatColor.GOLD).style(ChatColor.BOLD, ChatColor.ITALIC).onClick(BookUtil.ClickAction.openUrl("https://mc-protection.eu/")).onHover(BookUtil.HoverAction.showText("Open SpigotGuard website!")).build()).add(BookUtil.TextBuilder.of(MessageBuilder.newBuilder("&7Open &9discord&7 server&r").coloured().toString()).color(ChatColor.GOLD).style(ChatColor.BOLD, ChatColor.ITALIC).onClick(BookUtil.ClickAction.openUrl("https://mc-protection.eu/discord")).onHover(BookUtil.HoverAction.showText("Open discord server!")).build()).newLine().newLine().add((BaseComponent)new TextComponent(MessageBuilder.newBuilder(Settings.IMP.HACKER_BOOK.NEXT_PAGE).coloured().toString())).build() }).build());
        this.notificationCache.removeCache(player.getUniqueId());
    }
    
    private static void lambda$onExploitDetected$3(final ExploitDetectedEvent exploitDetectedEvent) {
        Settings.IMP.COMMANDS_WHEN_SURE.forEach(NotificationListener::lambda$onExploitDetected$2);
    }
    
    @EventHandler
    public void onJoin(final PlayerJoinEvent playerJoinEvent) {
        if (!Settings.IMP.HACKER_BOOK.ENABLED || SpigotGuardPlugin.getInstance().getNmsVersion() == NMSVersion.ONE_DOT_SEVEN_R4) {
            return;
        }
        final Player player = playerJoinEvent.getPlayer();
        final ExploitDetails cache = this.notificationCache.findCache(player.getUniqueId());
        if (cache != null) {
            Bukkit.getScheduler().runTaskLater((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$onJoin$4, 15L);
        }
    }
    
    private static void lambda$onExploitDetected$1(final String s, final Player player) {
        player.sendMessage(s);
    }
    
    private static boolean lambda$onExploitDetected$0(final Player player) {
        return player.hasPermission(Settings.IMP.MESSAGES.NOTIFICATION_PERMISSION);
    }
    
    private static void lambda$onExploitDetected$2(final ExploitDetectedEvent exploitDetectedEvent, final String s) {
        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), MessageBuilder.newBuilder(s).withField("{PLAYER}", exploitDetectedEvent.getPlayer().getName()).withField("{IP}", exploitDetectedEvent.getPlayer().getPlayer().getAddress().getAddress().getHostAddress()).toString());
    }
    
    @EventHandler
    public void onExploitDetected(final ExploitDetectedEvent exploitDetectedEvent) {
        final ExploitDetails exploitDetails = exploitDetectedEvent.getExploitDetails();
        Bukkit.getOnlinePlayers().stream().filter(NotificationListener::lambda$onExploitDetected$0).forEach(NotificationListener::lambda$onExploitDetected$1);
        final User byUuid = SpigotGuardPlugin.getInstance().getUserManager().findByUuid(exploitDetectedEvent.getPlayer().getUniqueId());
        if (byUuid != null) {
            byUuid.addAttempts(Collections.singletonList(exploitDetails));
        }
        if (Settings.IMP.HACKER_BOOK.ENABLED) {
            this.notificationCache.addCache(exploitDetectedEvent.getPlayer().getUniqueId(), exploitDetails);
        }
        if (exploitDetails.isSurelyExploit() && !Settings.IMP.COMMANDS_WHEN_SURE.isEmpty()) {
            SpigotGuardLogger.log(Level.INFO, "Executing BAN commands from settings.yml, player {0} surely tried to crash server.", exploitDetectedEvent.getPlayer().getName());
            Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), NotificationListener::lambda$onExploitDetected$3);
        }
    }
}
