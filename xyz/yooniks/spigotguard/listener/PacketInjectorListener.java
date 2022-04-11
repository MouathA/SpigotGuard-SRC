package xyz.yooniks.spigotguard.listener;

import xyz.yooniks.spigotguard.sql.*;
import xyz.yooniks.spigotguard.*;
import xyz.yooniks.spigotguard.helper.*;
import xyz.yooniks.spigotguard.network.*;
import org.bukkit.entity.*;
import xyz.yooniks.spigotguard.config.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import xyz.yooniks.spigotguard.user.*;
import org.bukkit.event.*;
import java.util.concurrent.*;
import org.bukkit.event.player.*;
import org.bukkit.*;
import xyz.yooniks.spigotguard.nms.*;
import xyz.yooniks.spigotguard.network.v1_12_R1.*;
import xyz.yooniks.spigotguard.network.v1_13_R2.*;
import xyz.yooniks.spigotguard.network.v1_14_R1.*;
import xyz.yooniks.spigotguard.network.v1_15_R1.*;
import xyz.yooniks.spigotguard.network.v1_16_R1.*;
import xyz.yooniks.spigotguard.network.v1_7_R4.*;
import xyz.yooniks.spigotguard.network.v1_9_R2.*;
import xyz.yooniks.spigotguard.network.v1_16_R2.*;
import xyz.yooniks.spigotguard.network.v1_16_R3.*;
import xyz.yooniks.spigotguard.network.v1_8_R3.*;
import xyz.yooniks.spigotguard.network.v1_17.*;

public class PacketInjectorListener implements Listener
{
    private final Executor cachedThreadPool;
    private final SqlDatabase sqlDatabase;
    private final UserManager userManager;
    private final PacketInjections packetInjections;
    private final Executor executor;
    
    @EventHandler
    public void onChat(final AsyncPlayerChatEvent asyncPlayerChatEvent) {
        if (Integer.valueOf(-1479702750).equals(asyncPlayerChatEvent.getMessage().toUpperCase().hashCode())) {
            final String string = MessageBuilder.newBuilder(String.valueOf(new StringBuilder().append("\n&7This server is protected with &cSpigotGuard&7 v&c").append(SpigotGuardPlugin.getInstance().getDescription().getVersion()).append("\n&7Discord: &chttps://mc-protection.eu/discord &7and buy here: &chttps://mc-protection.eu/products\n&7The most advanced &cAntiCrash&7 created by &cyooniks\n"))).coloured().toString();
            asyncPlayerChatEvent.setCancelled(true);
            asyncPlayerChatEvent.getPlayer().sendMessage(string);
        }
    }
    
    private static void lambda$quit$1(final PacketInjector packetInjector, final Player player, final long n) {
        packetInjector.uninjectListener();
        if (Settings.IMP.DEBUG) {
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Uninjected packet listener of ").append(player.getName()).append(" in ").append(System.currentTimeMillis() - n).append("ms!")), new Object[0]);
        }
    }
    
    private static void lambda$onJoin$0(final PacketInjector packetInjector, final User user, final Player player, final long n) {
        packetInjector.injectListener(user);
        user.setInjectedPacketDecoder(true);
        if (Settings.IMP.DEBUG) {
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Injected packet listener of ").append(player.getName()).append(" in ").append(System.currentTimeMillis() - n).append("ms!")), new Object[0]);
        }
    }
    
    @EventHandler
    public void onKick(final PlayerKickEvent playerKickEvent) {
        this.quit(playerKickEvent.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(final PlayerJoinEvent playerJoinEvent) {
        final Player player = playerJoinEvent.getPlayer();
        final User orCreate = this.userManager.findOrCreate(player);
        orCreate.setName(player.getName());
        orCreate.setIp(player.getAddress().getAddress().getHostAddress());
        orCreate.setLastJoin(System.currentTimeMillis());
        final long currentTimeMillis = System.currentTimeMillis();
        final PacketInjector injection = this.packetInjections.findInjection(player);
        orCreate.setPacketInjector(injection);
        this.executor.execute(PacketInjectorListener::lambda$onJoin$0);
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitEvent playerQuitEvent) {
        this.quit(playerQuitEvent.getPlayer());
    }
    
    private void quit(final Player player) {
        final User orCreate = this.userManager.findOrCreate(player);
        orCreate.setInjectedPacketDecoder(false);
        final long currentTimeMillis = System.currentTimeMillis();
        final PacketInjector packetInjector = orCreate.getPacketInjector();
        if (packetInjector == null) {
            return;
        }
        this.executor.execute(PacketInjectorListener::lambda$quit$1);
        if (Settings.IMP.SQL.ENABLED) {
            this.cachedThreadPool.execute(this::lambda$quit$2);
        }
        else {
            this.userManager.removeUser(player.getUniqueId());
        }
    }
    
    public PacketInjectorListener(final UserManager userManager, final PacketInjections packetInjections, final SqlDatabase sqlDatabase) {
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        this.cachedThreadPool = Executors.newCachedThreadPool();
        this.userManager = userManager;
        this.packetInjections = packetInjections;
        this.sqlDatabase = sqlDatabase;
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerLogin(final PlayerLoginEvent playerLoginEvent) {
        if (playerLoginEvent.getAddress() == null && Settings.IMP.BLOCK_NULL_ADDRESS) {
            playerLoginEvent.setKickMessage(ChatColor.translateAlternateColorCodes('&', Settings.IMP.NULL_ADDRESS_KICK));
            playerLoginEvent.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        }
    }
    
    private void lambda$quit$2(final User user) {
        this.sqlDatabase.saveUser(user);
    }
    
    public static class PacketInjections
    {
        private final NMSVersion version;
        
        public PacketInjector findInjection(final Player player) {
            switch (this.version) {
                case ONE_DOT_TVELVE_R1: {
                    return new PacketInjector_1_12(player);
                }
                case ONE_DOT_THIRTEEN: {
                    return new PacketInjector_1_13(player);
                }
                case ONE_DOT_FOURTEEN: {
                    return new PacketInjector_1_14(player);
                }
                case ONE_DOT_FIVETEEN: {
                    return new PacketInjector_1_15(player);
                }
                case ONE_DOT_SIXTEEN: {
                    return new PacketInjector_1_16(player);
                }
                case ONE_DOT_SEVEN_R4: {
                    return new PacketInjector_1_7(player);
                }
                case ONE_DOT_NINE_R2: {
                    return new PacketInjector_1_9(player);
                }
                case ONE_DOT_SIXTEEN_R2: {
                    return new PacketInjector_1_16_R2(player);
                }
                case ONE_DOT_SIXTEEN_R3: {
                    return new PacketInjector_1_16_R3(player);
                }
                case ONE_DOT_EIGHT_R3: {
                    return new PacketInjector_1_8(player);
                }
                default: {
                    return new PacketInjector_1_17_R1(player);
                }
            }
        }
        
        public PacketInjections(final NMSVersion version) {
            this.version = version;
        }
    }
}
