package xyz.yooniks.spigotguard.network.v1_15_R1;

import io.netty.handler.codec.*;
import xyz.yooniks.spigotguard.network.*;
import io.netty.channel.*;
import io.netty.buffer.*;
import java.util.*;
import xyz.yooniks.spigotguard.config.*;
import xyz.yooniks.spigotguard.*;
import xyz.yooniks.spigotguard.event.*;
import org.bukkit.*;
import org.bukkit.event.*;
import io.netty.util.concurrent.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import org.bukkit.plugin.*;

public class PacketDecompress_1_15 extends ByteToMessageDecoder
{
    private final PacketInjector injector;
    
    protected void decode(final ChannelHandlerContext channelHandlerContext, final ByteBuf byteBuf, final List<Object> list) {
        if (byteBuf.capacity() > Settings.IMP.VIAVERSION_MAX_CAPACITY) {
            if (byteBuf.refCnt() > 0) {
                byteBuf.release();
            }
            channelHandlerContext.close().addListener(this::lambda$decode$1);
            return;
        }
        list.add(byteBuf.readBytes(byteBuf.readableBytes()));
    }
    
    private void lambda$decode$0() {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)this.injector.getPlayer(), new ExploitDetails(SpigotGuardPlugin.getInstance().getUserManager().findByUuid(this.injector.getPlayer().getUniqueId()), "undefined", "too big packet size", false, false, "increase viaversion-max-capacity in settings.yml or disable viaversion-integration")));
    }
    
    public PacketDecompress_1_15(final PacketInjector injector) {
        this.injector = injector;
    }
    
    private void lambda$decode$1(final ChannelHandlerContext channelHandlerContext, final ByteBuf byteBuf, final Future future) throws Exception {
        channelHandlerContext.flush();
        channelHandlerContext.disconnect();
        SpigotGuardLogger.log(Level.INFO, "{0} sent too large packet (size: {1}). If it is false positive please increase the limit of viaversion-max-capacity or just disable viaversion-integration (viaversion is not needed to let this check work correctly, so do not worry if you don't have viaversion and this information).", this.injector.getPlayer().getName(), byteBuf.capacity());
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$0);
    }
}
