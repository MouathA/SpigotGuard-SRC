package xyz.yooniks.spigotguard.network.v1_8_R3;

import io.netty.handler.codec.*;
import org.bukkit.entity.*;
import xyz.yooniks.spigotguard.*;
import xyz.yooniks.spigotguard.event.*;
import org.bukkit.event.*;
import xyz.yooniks.spigotguard.config.*;
import xyz.yooniks.spigotguard.helper.*;
import io.netty.util.concurrent.*;
import org.bukkit.plugin.*;
import io.netty.buffer.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.*;
import java.util.*;
import xyz.yooniks.spigotguard.network.*;
import io.netty.channel.*;
import io.netty.util.*;
import net.minecraft.server.v1_8_R3.*;

public class PacketDecompress_1_8 extends ByteToMessageDecoder
{
    private boolean disconnected;
    private int errors;
    private final PacketInjector injector;
    
    private void lambda$decode$0(final Player player) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, new ExploitDetails(SpigotGuardPlugin.getInstance().getUserManager().findOrCreate(this.injector.getPlayer()), "undefined (bf check)", "too big packet size", false, false, "set \"packet-decoder.max-main-size\" in settings.yml to higher value or set it to -1 to disable it")));
    }
    
    private void lambda$decode$12(final Player player, final ExploitDetails exploitDetails) {
        this.injector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    private void lambda$decode$15() {
        this.injector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
    }
    
    private void lambda$decode$3(final Player player) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, new ExploitDetails(SpigotGuardPlugin.getInstance().getUserManager().findOrCreate(this.injector.getPlayer()), "undefined (bf check)", "too small packet size (refCnt < 1)", false, false)));
    }
    
    private static void lambda$decode$7(final Player player, final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    private static void lambda$decode$10(final Player player, final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    private void lambda$decode$6(final Player player, final ExploitDetails exploitDetails) {
        this.injector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    private void lambda$decode$2(final Player player) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, new ExploitDetails(SpigotGuardPlugin.getInstance().getUserManager().findOrCreate(this.injector.getPlayer()), "undefined (bf check)", "too small packet size (capacity < 0)", false, false)));
    }
    
    private void lambda$decode$13(final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)this.injector.getPlayer(), exploitDetails));
    }
    
    private void lambda$decode$9(final Player player, final ExploitDetails exploitDetails) {
        this.injector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    private void lambda$decode$14(final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$13);
    }
    
    private void lambda$decode$21() {
        this.injector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
    }
    
    private void lambda$decode$19(final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)this.injector.getPlayer(), exploitDetails));
    }
    
    protected void decode(final ChannelHandlerContext channelHandlerContext, final ByteBuf byteBuf, final List<Object> list) {
        try {
            final Channel channel = channelHandlerContext.channel();
            final AttributeKey c = NetworkManager.c;
            if (byteBuf instanceof EmptyByteBuf) {
                list.add(byteBuf.readBytes(byteBuf.readableBytes()));
                return;
            }
            if (this.disconnected) {
                if (byteBuf.refCnt() > 0) {
                    byteBuf.clear();
                }
                return;
            }
            final Player player = this.injector.getPlayer();
            if (player == null || !player.isOnline()) {
                if (byteBuf.refCnt() > 0) {
                    byteBuf.clear();
                }
                return;
            }
            final Settings.PACKET_DECODER packet_DECODER = Settings.IMP.PACKET_DECODER;
            final int capacity = byteBuf.capacity();
            final int max_MAIN_SIZE = packet_DECODER.MAX_MAIN_SIZE;
            if (max_MAIN_SIZE != -1 && capacity > max_MAIN_SIZE) {
                channelHandlerContext.close().addListener(this::lambda$decode$1);
                if (byteBuf.refCnt() > 0) {
                    byteBuf.clear();
                }
                return;
            }
            if (capacity < 0) {
                Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$2);
                return;
            }
            if (byteBuf.refCnt() < 1) {
                Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$3);
                return;
            }
            final ByteBuf copy = byteBuf.copy();
            final PacketDataSerializer packetDataSerializer = new PacketDataSerializer(copy);
            final int e = packetDataSerializer.e();
            Packet a;
            try {
                a = ((EnumProtocol)channel.attr(c).get()).a(EnumProtocolDirection.SERVERBOUND, e);
            }
            catch (Throwable t) {
                list.add(byteBuf.readBytes(byteBuf.readableBytes()));
                return;
            }
            if (a == null) {
                if (copy.refCnt() > 0) {
                    copy.clear();
                }
                list.add(byteBuf.readBytes(byteBuf.readableBytes()));
                return;
            }
            try {
                a.a(packetDataSerializer);
            }
            catch (Throwable t2) {
                list.add(byteBuf.readBytes(byteBuf.readableBytes()));
                return;
            }
            if (!this.injector.getPacketDecoder().getUser().isInjectedPacketDecoder() && (a instanceof PacketPlayInWindowClick || a instanceof PacketPlayInSetCreativeSlot || a instanceof PacketPlayInBlockPlace || a instanceof PacketPlayInCustomPayload)) {
                if (a instanceof PacketPlayInCustomPayload) {
                    final String a2 = ((PacketPlayInCustomPayload)a).a();
                    if (Integer.valueOf(-296262810).equals(a2.toUpperCase().hashCode()) || Integer.valueOf(-295840999).equals(a2.toUpperCase().hashCode())) {
                        byteBuf.skipBytes(byteBuf.readableBytes());
                        if (copy.refCnt() > 0) {
                            copy.clear();
                        }
                        SpigotGuardLogger.log(Level.INFO, "Player {0} has tried to send invalid packet (PROBABLY), but we did not inject packet_decoder yet!", new Object[0]);
                        return;
                    }
                }
                if (capacity > 100) {
                    SpigotGuardLogger.log(Level.INFO, "Player {0} has tried to send invalid packet (PROBABLY), but we did not inject packet_decoder yet!", new Object[0]);
                    byteBuf.skipBytes(byteBuf.readableBytes());
                    if (copy.refCnt() > 0) {
                        copy.clear();
                    }
                    return;
                }
            }
            ItemStack itemStack = null;
            if (a instanceof PacketPlayInCustomPayload) {
                final String a3 = ((PacketPlayInCustomPayload)a).a();
                if (a3 != null && (Integer.valueOf(-296262810).equals(a3.toUpperCase().hashCode()) || Integer.valueOf(-295840999).equals(a3.toUpperCase().hashCode()) || Integer.valueOf(-295953498).equals(a3.toUpperCase().hashCode()))) {
                    boolean b = false;
                    for (final org.bukkit.inventory.ItemStack itemStack2 : player.getInventory().getContents()) {
                        if (itemStack2.getType() == Material.BOOK_AND_QUILL || itemStack2.getType() == Material.WRITTEN_BOOK) {
                            b = true;
                        }
                    }
                    if (!b) {
                        final ExploitDetails exploitDetails = new ExploitDetails(this.injector.getPacketDecoder().getUser(), a.getClass().getSimpleName(), String.valueOf(new StringBuilder().append("Payload without book?! (capacity: ").append(capacity).append(")")), false, false, "Not fixable, it is not possible to make this bug, it must be an exploit!");
                        if (copy.refCnt() > 0) {
                            copy.clear();
                        }
                        if (Settings.IMP.KICK.TYPE == 0) {
                            channelHandlerContext.close().addListener(PacketDecompress_1_8::lambda$decode$5);
                        }
                        else {
                            Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$6);
                        }
                        SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(SpigotGuardPlugin.getInstance().getSpigotGuardClassLoaded().getSerializable()).append("[PacketDecoder] Player {0} tried to crash the server, packet: {1}, details: {2}")), player.getName(), a.getClass().getSimpleName(), exploitDetails.getDetails());
                        this.disconnected = true;
                        if (copy.refCnt() > 0) {
                            copy.clear();
                        }
                        byteBuf.skipBytes(byteBuf.readableBytes());
                        return;
                    }
                }
                if (a3 != null && ((PacketPlayInCustomPayload)a).b() != null) {
                    itemStack = ((PacketPlayInCustomPayload)a).b().i();
                }
            }
            else if (a instanceof PacketPlayInWindowClick) {
                itemStack = ((PacketPlayInWindowClick)a).e();
            }
            else if (a instanceof PacketPlayInSetCreativeSlot) {
                itemStack = ((PacketPlayInSetCreativeSlot)a).getItemStack();
                if (player.getGameMode() != GameMode.CREATIVE) {
                    final ExploitDetails exploitDetails2 = new ExploitDetails(this.injector.getPacketDecoder().getUser(), ((PacketPlayInSetCreativeSlot)a).getClass().getSimpleName(), "clicking in creative inventory without gamemode 1", false, true, "set \"allow-creative-inventory-click-without-gamemode\" to true in settings.yml");
                    if (copy.refCnt() > 0) {
                        copy.clear();
                    }
                    if (Settings.IMP.KICK.TYPE == 0) {
                        channelHandlerContext.close().addListener(PacketDecompress_1_8::lambda$decode$8);
                    }
                    else {
                        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$9);
                    }
                    SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(SpigotGuardPlugin.getInstance().getSpigotGuardClassLoaded().getSerializable()).append("[PacketDecoder] Player {0} tried to crash the server, packet: {1}, details: {2}")), player.getName(), ((PacketPlayInSetCreativeSlot)a).getClass().getSimpleName(), exploitDetails2.getDetails());
                    byteBuf.skipBytes(byteBuf.readableBytes());
                    return;
                }
            }
            else if (a instanceof PacketPlayInBlockPlace) {
                itemStack = ((PacketPlayInBlockPlace)a).getItemStack();
            }
            if (itemStack != null) {
                final CraftItemStack craftMirror = CraftItemStack.asCraftMirror(itemStack);
                final NBTTagCompound nbtTagCompound = itemStack.hasTag() ? itemStack.getTag() : null;
                if (nbtTagCompound != null && (craftMirror.getType() == Material.WRITTEN_BOOK || craftMirror.getType() == Material.BOOK_AND_QUILL)) {
                    if (nbtTagCompound.hasKey(":[{extra:[{") || Objects.requireNonNull(nbtTagCompound).toString().contains(":[{extra:[{")) {
                        nbtTagCompound.remove("pages");
                        nbtTagCompound.remove("author");
                        nbtTagCompound.remove("title");
                        final ExploitDetails exploitDetails3 = new ExploitDetails(this.injector.getPacketDecoder().getUser(), ((PacketPlayInBlockPlace)a).getClass().getSimpleName(), String.valueOf(new StringBuilder().append("Bad WRITTEN_BOOK nbt data, extra:[{ data ?! (capacity: ").append(capacity).append(")")), false, false, "Not fixable, it is not possible to make this bug, it must be an exploit!");
                        if (copy.refCnt() > 0) {
                            copy.clear();
                        }
                        if (Settings.IMP.KICK.TYPE == 0) {
                            channelHandlerContext.close().addListener(PacketDecompress_1_8::lambda$decode$11);
                        }
                        else {
                            Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$12);
                        }
                        SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(SpigotGuardPlugin.getInstance().getSpigotGuardClassLoaded().getSerializable()).append("[PacketDecoder] Player {0} tried to crash the server, packet: {1}, details: {2}")), player.getName(), ((PacketPlayInBlockPlace)a).getClass().getSimpleName(), exploitDetails3.getDetails());
                        byteBuf.skipBytes(byteBuf.readableBytes());
                        return;
                    }
                    final Settings imp = Settings.IMP;
                    if (nbtTagCompound.hasKey("pages")) {
                        if (imp.BOOK.BAN_BOOKS) {
                            nbtTagCompound.remove("pages");
                            nbtTagCompound.remove("author");
                            nbtTagCompound.remove("title");
                            itemStack.setTag(new NBTTagCompound());
                            final ExploitDetails exploitDetails4 = new ExploitDetails(this.injector.getPacketDecoder().getUser(), ((PacketPlayInBlockPlace)a).getClass().getSimpleName(), "using book while it is banned", false, true, "set \"book.ban-books\" in settings.yml to false");
                            if (Settings.IMP.KICK.TYPE == 0) {
                                channelHandlerContext.close().addListener(this::lambda$decode$14);
                            }
                            else {
                                Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$15);
                            }
                            SpigotGuardLogger.log(Level.INFO, "[1] Player {0} tried to crash the server, packet: {1}, details: {2}", this.injector.getPlayer().getName(), exploitDetails4.getClass().getSimpleName(), exploitDetails4.getDetails());
                            return;
                        }
                        final NBTTagList list2 = nbtTagCompound.getList("pages", 8);
                        if (list2.size() > imp.BOOK.MAX_PAGES) {
                            nbtTagCompound.remove("pages");
                            nbtTagCompound.remove("author");
                            nbtTagCompound.remove("title");
                            itemStack.setTag(new NBTTagCompound());
                            final ExploitDetails exploitDetails5 = new ExploitDetails(this.injector.getPacketDecoder().getUser(), ((PacketPlayInBlockPlace)a).getClass().getSimpleName(), String.valueOf(new StringBuilder().append("too many pages (").append(list2.size()).append(")")), false, true, "set \"book.max-pages\" in settings.yml to higher value");
                            if (Settings.IMP.KICK.TYPE == 0) {
                                channelHandlerContext.close().addListener(this::lambda$decode$17);
                            }
                            else {
                                Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$18);
                            }
                            SpigotGuardLogger.log(Level.INFO, "[1] Player {0} tried to crash the server, packet: {1}, details: {2}", this.injector.getPlayer().getName(), exploitDetails5.getClass().getSimpleName(), exploitDetails5.getDetails());
                            return;
                        }
                        for (int j = 0; j < list2.size(); ++j) {
                            final String string = list2.getString(j);
                            if (string.length() > imp.BOOK.MAX_PAGE_SIZE) {
                                nbtTagCompound.remove("pages");
                                nbtTagCompound.remove("author");
                                nbtTagCompound.remove("title");
                                itemStack.setTag(new NBTTagCompound());
                                final ExploitDetails exploitDetails6 = new ExploitDetails(this.injector.getPacketDecoder().getUser(), ((PacketPlayInBlockPlace)a).getClass().getSimpleName(), String.valueOf(new StringBuilder().append("too large page content (").append(string.length()).append(")")), false, false, String.valueOf(new StringBuilder().append("increase value of \"book.max-page-size\" (bigger than: ").append(string.length()).append(") in settings.yml")));
                                if (Settings.IMP.KICK.TYPE == 0) {
                                    channelHandlerContext.close().addListener(this::lambda$decode$20);
                                }
                                else {
                                    Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$21);
                                }
                                SpigotGuardLogger.log(Level.INFO, "[1] Player {0} tried to crash the server, packet: {1}, details: {2}", this.injector.getPlayer().getName(), exploitDetails6.getClass().getSimpleName(), exploitDetails6.getDetails());
                                return;
                            }
                        }
                    }
                }
            }
            if (SimpleCrashChecker.checkCrash(a, copy, packet_DECODER, capacity, this.injector, player, channelHandlerContext)) {
                this.disconnected = true;
                if (copy.refCnt() > 0) {
                    copy.clear();
                }
                byteBuf.skipBytes(byteBuf.readableBytes());
                return;
            }
        }
        catch (Throwable t3) {
            list.add(byteBuf.readBytes(byteBuf.readableBytes()));
            return;
        }
        list.add(byteBuf.readBytes(byteBuf.readableBytes()));
    }
    
    private static void lambda$decode$5(final Player player, final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), PacketDecompress_1_8::lambda$decode$4);
    }
    
    private void lambda$decode$16(final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)this.injector.getPlayer(), exploitDetails));
    }
    
    private void lambda$decode$17(final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$16);
    }
    
    private static void lambda$decode$8(final Player player, final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), PacketDecompress_1_8::lambda$decode$7);
    }
    
    private void lambda$decode$1(final Player player, final int n, final Future future) throws Exception {
        SpigotGuardLogger.log(Level.INFO, "{0} sent too large packet (size: {1}). If it is false positive please increase the limit of packet-decoder.max-main-size or set it to -1.", player.getName(), n);
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$0);
    }
    
    private void lambda$decode$20(final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$19);
    }
    
    private static void lambda$decode$11(final Player player, final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), PacketDecompress_1_8::lambda$decode$10);
    }
    
    private static void lambda$decode$4(final Player player, final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)player, exploitDetails));
    }
    
    private void lambda$decode$18() {
        this.injector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
    }
    
    public PacketDecompress_1_8(final PacketInjector injector) {
        this.disconnected = false;
        this.errors = 0;
        this.injector = injector;
    }
}
