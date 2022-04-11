package xyz.yooniks.spigotguard.network;

import org.bukkit.entity.*;
import xyz.yooniks.spigotguard.config.*;
import xyz.yooniks.spigotguard.helper.*;
import xyz.yooniks.spigotguard.event.*;
import org.bukkit.*;
import org.bukkit.event.*;
import io.netty.buffer.*;
import io.netty.channel.*;
import xyz.yooniks.spigotguard.*;
import org.bukkit.plugin.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import io.netty.util.concurrent.*;

public final class SimpleCrashChecker
{
    private static void lambda$checkCrash$11(final PacketInjector packetInjector, final Player player, final ExploitDetails exploitDetails) {
        packetInjector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    private SimpleCrashChecker() {
    }
    
    private static void lambda$checkCrash$3(final Player player, final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    private static void lambda$checkCrash$6(final Player player, final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    private static void lambda$checkCrash$2(final PacketInjector packetInjector, final Player player, final ExploitDetails exploitDetails) {
        packetInjector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    public static boolean checkCrash(final Object o, final ByteBuf byteBuf, final Settings.PACKET_DECODER packet_DECODER, final int n, final PacketInjector packetInjector, final Player player, final ChannelHandlerContext channelHandlerContext) {
        if (o.getClass().getSimpleName().equals("PacketPlayInWindowClick") && n > packet_DECODER.MAX_WINDOW_SIZE && packet_DECODER.MAX_WINDOW_SIZE != -1) {
            final ExploitDetails exploitDetails = new ExploitDetails(packetInjector.getPacketDecoder().getUser(), o.getClass().getSimpleName(), String.valueOf(new StringBuilder().append("Packet size too large (").append(n).append(" > ").append(packet_DECODER.MAX_WINDOW_SIZE).append(")")), false, false, "Increase packet-decoder.max-window-size or set it to -1 to disable it");
            if (byteBuf.refCnt() > 1) {
                byteBuf.clear();
            }
            if (Settings.IMP.KICK.TYPE == 0) {
                channelHandlerContext.close().addListener(SimpleCrashChecker::lambda$checkCrash$1);
            }
            else {
                Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), SimpleCrashChecker::lambda$checkCrash$2);
            }
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(SpigotGuardPlugin.getInstance().getSpigotGuardClassLoaded().getSerializable()).append("[Adv. decoding] Player {0} tried to crash the server, packet: {1}, details: {2}")), player.getName(), o.getClass().getSimpleName(), exploitDetails.getDetails());
            return true;
        }
        if (o.getClass().getSimpleName().equals("PacketPlayInBlockPlace") && n > packet_DECODER.MAX_PLACE_SIZE && packet_DECODER.MAX_PLACE_SIZE != -1) {
            final ExploitDetails exploitDetails2 = new ExploitDetails(packetInjector.getPacketDecoder().getUser(), o.getClass().getSimpleName(), String.valueOf(new StringBuilder().append("Packet size too large (").append(n).append(" > ").append(packet_DECODER.MAX_PLACE_SIZE).append(")")), false, false, "Increase packet-decoder.max-place-size or set it to -1 to disable it");
            if (byteBuf.refCnt() > 0) {
                byteBuf.clear();
            }
            if (Settings.IMP.KICK.TYPE == 0) {
                channelHandlerContext.close().addListener(SimpleCrashChecker::lambda$checkCrash$4);
            }
            else {
                Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), SimpleCrashChecker::lambda$checkCrash$5);
            }
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(SpigotGuardPlugin.getInstance().getSpigotGuardClassLoaded().getSerializable()).append("[Adv. decoding] Player {0} tried to crash the server, packet: {1}, details: {2}")), player.getName(), o.getClass().getSimpleName(), exploitDetails2.getDetails());
            return true;
        }
        if (o.getClass().getSimpleName().equals("PacketPlayInSetCreativeSlot") && n > packet_DECODER.MAX_CREATIVE_SIZE && packet_DECODER.MAX_CREATIVE_SIZE != -1) {
            final ExploitDetails exploitDetails3 = new ExploitDetails(packetInjector.getPacketDecoder().getUser(), o.getClass().getSimpleName(), String.valueOf(new StringBuilder().append("Packet size too large (").append(n).append(" > ").append(packet_DECODER.MAX_CREATIVE_SIZE).append(")")), false, false, "Increase packet-decoder.max-creative-size or set it to -1 to disable it");
            if (byteBuf.refCnt() > 0) {
                byteBuf.clear();
            }
            if (Settings.IMP.KICK.TYPE == 0) {
                channelHandlerContext.close().addListener(SimpleCrashChecker::lambda$checkCrash$7);
            }
            else {
                Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), SimpleCrashChecker::lambda$checkCrash$8);
            }
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(SpigotGuardPlugin.getInstance().getSpigotGuardClassLoaded().getSerializable()).append("[Adv. decoding] Player {0} tried to crash the server, packet: {1}, details: {2}")), player.getName(), o.getClass().getSimpleName(), exploitDetails3.getDetails());
            return true;
        }
        if ((o.getClass().getSimpleName().equals("PacketPlayInCustomPayload") || o.getClass().getSimpleName().equals("PacketPlayInBEdit")) && n > packet_DECODER.MAX_PAYLOAD_SIZE && packet_DECODER.MAX_PAYLOAD_SIZE != -1) {
            final ExploitDetails exploitDetails4 = new ExploitDetails(packetInjector.getPacketDecoder().getUser(), o.getClass().getSimpleName(), String.valueOf(new StringBuilder().append("Packet size too large (").append(n).append(" > ").append(packet_DECODER.MAX_PAYLOAD_SIZE).append(")")), false, false, "Increase packet-decoder.max-payload-size or set it to -1 to disable it");
            if (byteBuf.refCnt() > 0) {
                byteBuf.clear();
            }
            if (Settings.IMP.KICK.TYPE == 0) {
                channelHandlerContext.close().addListener(SimpleCrashChecker::lambda$checkCrash$10);
            }
            else {
                Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), SimpleCrashChecker::lambda$checkCrash$11);
            }
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(SpigotGuardPlugin.getInstance().getSpigotGuardClassLoaded().getSerializable()).append("[Adv. decoding] Player {0} tried to crash the server, packet: {1}, details: {2}")), player.getName(), o.getClass().getSimpleName(), exploitDetails4.getDetails());
            return true;
        }
        return false;
    }
    
    private static void lambda$checkCrash$9(final Player player, final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    private static void lambda$checkCrash$4(final Player player, final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), SimpleCrashChecker::lambda$checkCrash$3);
    }
    
    private static void lambda$checkCrash$7(final Player player, final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), SimpleCrashChecker::lambda$checkCrash$6);
    }
    
    private static void lambda$checkCrash$10(final Player player, final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), SimpleCrashChecker::lambda$checkCrash$9);
    }
    
    private static void lambda$checkCrash$5(final PacketInjector packetInjector, final Player player, final ExploitDetails exploitDetails) {
        packetInjector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    private static void lambda$checkCrash$1(final Player player, final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), SimpleCrashChecker::lambda$checkCrash$0);
    }
    
    private static void lambda$checkCrash$0(final Player player, final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    private static void lambda$checkCrash$8(final PacketInjector packetInjector, final Player player, final ExploitDetails exploitDetails) {
        packetInjector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
}
