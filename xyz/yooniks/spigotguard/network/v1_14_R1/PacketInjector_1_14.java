package xyz.yooniks.spigotguard.network.v1_14_R1;

import xyz.yooniks.spigotguard.network.*;
import xyz.yooniks.spigotguard.user.*;
import io.netty.channel.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_14_R1.entity.*;

public class PacketInjector_1_14 extends PacketInjector
{
    private final Channel channel;
    
    @Override
    public void injectListener(final User user) {
        this.channel.pipeline().addAfter("decoder", String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())), (ChannelHandler)new PacketDecoder_1_14(this, user));
    }
    
    @Override
    public void uninjectListener() {
        if (this.channel.pipeline().get(String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName()))) != null) {
            this.channel.pipeline().remove(String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())));
        }
    }
    
    public PacketInjector_1_14(final Player player) {
        super(player);
        this.channel = ((CraftPlayer)player).getHandle().playerConnection.networkManager.channel;
    }
}
