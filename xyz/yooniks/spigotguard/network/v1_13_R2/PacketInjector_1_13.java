package xyz.yooniks.spigotguard.network.v1_13_R2;

import xyz.yooniks.spigotguard.network.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_13_R2.entity.*;
import xyz.yooniks.spigotguard.user.*;
import io.netty.channel.*;

public class PacketInjector_1_13 extends PacketInjector
{
    private final Channel channel;
    
    public PacketInjector_1_13(final Player player) {
        super(player);
        this.channel = ((CraftPlayer)player).getHandle().playerConnection.networkManager.channel;
    }
    
    @Override
    public void injectListener(final User user) {
        this.channel.pipeline().addAfter("decoder", String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())), (ChannelHandler)new PacketDecoder_1_13(this, user));
    }
    
    @Override
    public void uninjectListener() {
        if (this.channel.pipeline().get(String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName()))) != null) {
            this.channel.pipeline().remove(String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())));
        }
    }
}
