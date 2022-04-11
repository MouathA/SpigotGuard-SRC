package xyz.yooniks.spigotguard.network.v1_16_R1;

import xyz.yooniks.spigotguard.user.*;
import io.netty.channel.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import org.bukkit.*;
import xyz.yooniks.spigotguard.*;
import org.bukkit.plugin.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_16_R1.entity.*;
import xyz.yooniks.spigotguard.network.*;

public class PacketInjector_1_16 extends PacketInjector
{
    private final Channel channel;
    
    @Override
    public void injectListener(final User user) {
        final PacketDecoder_1_16 packetDecoder_1_16 = new PacketDecoder_1_16(this, user);
        try {
            this.channel.pipeline().addAfter("decoder", String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())), (ChannelHandler)packetDecoder_1_16);
        }
        catch (Throwable t) {
            SpigotGuardLogger.log(Level.WARNING, "Could not inject packetListener for {0}, trying again in 1.5 sec ({1})", this.player.getName(), t.getMessage());
            Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$injectListener$0, 30L);
        }
    }
    
    public PacketInjector_1_16(final Player player) {
        super(player);
        this.channel = ((CraftPlayer)player).getHandle().playerConnection.networkManager.channel;
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
    public void uninjectListener() {
        if (this.channel.pipeline().get(String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName()))) != null) {
            this.channel.pipeline().remove(String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())));
        }
    }
}
