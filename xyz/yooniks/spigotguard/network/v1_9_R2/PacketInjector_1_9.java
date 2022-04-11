package xyz.yooniks.spigotguard.network.v1_9_R2;

import io.netty.channel.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import xyz.yooniks.spigotguard.config.*;
import xyz.yooniks.spigotguard.network.*;
import xyz.yooniks.spigotguard.user.*;
import org.bukkit.*;
import xyz.yooniks.spigotguard.*;
import org.bukkit.plugin.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.*;

public class PacketInjector_1_9 extends PacketInjector
{
    private final Channel channel;
    
    private void lambda$injectListener$1() {
        try {
            this.channel.pipeline().addBefore("decompress", "sg_decompress", (ChannelHandler)new PacketDecompress_1_9(this));
        }
        catch (Throwable t) {
            SpigotGuardLogger.log(Level.WARNING, "Could not inject packetDecompress for {0}, even after 1.5 sec, reason: {1}", this.player.getName(), t.getMessage());
        }
    }
    
    @Override
    public void uninjectListener() {
        try {
            if (this.channel.pipeline().get(String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName()))) != null) {
                this.channel.pipeline().remove(String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())));
            }
            if (Settings.IMP.VIAVERSION_INTEGRATION && this.channel.pipeline().get("sg_decompress") != null) {
                this.channel.pipeline().remove("sg_decompress");
            }
        }
        catch (Exception ex) {
            SpigotGuardLogger.log(Level.WARNING, "Could not uninject packetListener for {0}, reason: {1}", this.player.getName(), ex.getMessage());
        }
    }
    
    private void lambda$injectListener$0(final PacketDecoder packetDecoder) {
        try {
            this.channel.pipeline().addAfter("decoder", String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())), (ChannelHandler)packetDecoder);
        }
        catch (Throwable t) {
            SpigotGuardLogger.log(Level.WARNING, "Could not inject packetListener for {0}, even after 1.5 sec, reason: {1}", this.player.getName(), t.getMessage());
        }
    }
    
    @Override
    public void injectListener(final User user) {
        final PacketDecoder_1_9 packetDecoder_1_9 = new PacketDecoder_1_9(this, user);
        try {
            this.channel.pipeline().addAfter("decoder", String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())), (ChannelHandler)packetDecoder_1_9);
        }
        catch (Throwable t) {
            SpigotGuardLogger.log(Level.WARNING, "Could not inject packetListener for {0}, trying again in 1.5 sec ({1})", this.player.getName(), t.getMessage());
            Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$injectListener$0, 30L);
        }
        if (Settings.IMP.VIAVERSION_INTEGRATION && this.channel.pipeline().get("decompress") != null) {
            this.channel.pipeline().addBefore("decompress", "sg_decompress", (ChannelHandler)new PacketDecompress_1_9(this));
        }
        else if (Settings.IMP.VIAVERSION_INTEGRATION) {
            Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$injectListener$1, 30L);
        }
    }
    
    public PacketInjector_1_9(final Player player) {
        super(player);
        this.channel = ((CraftPlayer)player).getHandle().playerConnection.networkManager.channel;
    }
}
