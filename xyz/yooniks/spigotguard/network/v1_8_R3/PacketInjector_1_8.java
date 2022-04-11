package xyz.yooniks.spigotguard.network.v1_8_R3;

import io.netty.channel.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import xyz.yooniks.spigotguard.user.*;
import org.bukkit.*;
import xyz.yooniks.spigotguard.*;
import org.bukkit.plugin.*;
import xyz.yooniks.spigotguard.network.*;
import xyz.yooniks.spigotguard.config.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.*;

public class PacketInjector_1_8 extends PacketInjector
{
    private final Channel channel;
    
    private void lambda$injectListener$0(final PacketDecoder packetDecoder) {
        try {
            this.channel.pipeline().addAfter("decoder", String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())), (ChannelHandler)packetDecoder);
        }
        catch (Throwable t) {
            SpigotGuardLogger.log(Level.WARNING, String.valueOf(new StringBuilder().append("Could not inject packetListener for ").append(this.player.getName()).append(", even after 1.5 sec, reason: ").append(t.getMessage())), new Object[0]);
        }
    }
    
    @Override
    public void injectListener(final User user) {
        final PacketDecoder_1_8 packetDecoder = new PacketDecoder_1_8(this, user);
        this.packetDecoder = packetDecoder;
        try {
            this.channel.pipeline().addAfter("decoder", String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())), (ChannelHandler)packetDecoder);
        }
        catch (Throwable t) {
            SpigotGuardLogger.log(Level.WARNING, String.valueOf(new StringBuilder().append("Could not inject packetListener for ").append(this.player.getName()).append(", trying again in 1.5 sec (").append(t.getMessage()).append(")")), new Object[0]);
            Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$injectListener$0, 30L);
        }
        final Plugin plugin = Bukkit.getPluginManager().getPlugin("ViaVersion");
        if (plugin != null && ViaVersionCheck.shouldChangeCompressor(this.player)) {
            if (ViaVersionCheck.shouldChangeCompressor(this.player) && Settings.IMP.PACKET_DECODER.ENABLED) {
                if (this.channel.pipeline().get("decompress") != null) {
                    this.channel.pipeline().addAfter("decompress", "sg_decompress", (ChannelHandler)new PacketDecompress_1_8(this));
                }
                else {
                    try {
                        this.channel.pipeline().addAfter("splitter", "sg_decompress", (ChannelHandler)new PacketDecompress_1_8(this));
                    }
                    catch (Exception ex) {}
                }
            }
        }
        else if (plugin == null && Settings.IMP.PACKET_DECODER.ENABLED) {
            if (this.channel.pipeline().get("decompress") != null) {
                this.channel.pipeline().addAfter("decompress", "sg_decompress", (ChannelHandler)new PacketDecompress_1_8(this));
            }
            else {
                try {
                    this.channel.pipeline().addAfter("splitter", "sg_decompress", (ChannelHandler)new PacketDecompress_1_8(this));
                }
                catch (Exception ex2) {}
            }
        }
        try {
            this.channel.pipeline().addBefore("packet_handler", "sg_listener", (ChannelHandler)new PacketListener_1_8(this, user));
        }
        catch (Throwable t2) {
            SpigotGuardLogger.log(Level.WARNING, String.valueOf(new StringBuilder().append("Could not inject packetHandler for ").append(this.player.getName()).append(", trying again in 1.5 sec (").append(t2.getMessage()).append(")")), new Object[0]);
            Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$injectListener$1, 30L);
        }
    }
    
    private void lambda$injectListener$1(final User user) {
        try {
            this.channel.pipeline().addBefore("packet_handler", "sg_listener", (ChannelHandler)new PacketListener_1_8(this, user));
        }
        catch (Throwable t) {
            SpigotGuardLogger.log(Level.WARNING, String.valueOf(new StringBuilder().append("Could not inject packetHandler for ").append(this.player.getName()).append(", even after 1.5 sec, reason: ").append(t.getMessage())), new Object[0]);
        }
    }
    
    @Override
    public void uninjectListener() {
        try {
            if (this.channel.pipeline().get(String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName()))) != null) {
                this.channel.pipeline().remove(String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())));
            }
            if (this.channel.pipeline().get("sg_decompress") != null) {
                this.channel.pipeline().remove("sg_decompress");
            }
            if (this.channel.pipeline().get("sg_listener") != null) {
                this.channel.pipeline().remove("sg_listener");
            }
        }
        catch (Exception ex) {
            SpigotGuardLogger.log(Level.WARNING, String.valueOf(new StringBuilder().append("Could not uninject packetListener for ").append(this.player.getName()).append(", reason: ").append(ex.getMessage())), new Object[0]);
        }
    }
    
    public PacketInjector_1_8(final Player player) {
        super(player);
        this.channel = ((CraftPlayer)player).getHandle().playerConnection.networkManager.channel;
    }
}
