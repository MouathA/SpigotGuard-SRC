package xyz.yooniks.spigotguard.network.v1_7_R4;

import xyz.yooniks.spigotguard.network.*;
import org.bukkit.entity.*;
import org.bukkit.craftbukkit.v1_7_R4.entity.*;
import xyz.yooniks.spigotguard.logger.*;
import java.lang.reflect.*;
import xyz.yooniks.spigotguard.user.*;
import net.minecraft.util.io.netty.channel.*;

public class PacketInjector_1_7 extends PacketInjector
{
    private Channel channel;
    
    @Override
    public void uninjectListener() {
        if (this.channel.pipeline().get(String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName()))) != null) {
            this.channel.pipeline().remove(String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())));
        }
        if (this.channel.pipeline().get(this.player.getName()) != null) {
            this.channel.pipeline().remove(this.player.getName());
        }
    }
    
    public PacketInjector_1_7(final Player player) {
        super(player);
        try {
            final Field declaredField = ((CraftPlayer)this.player).getHandle().playerConnection.networkManager.getClass().getDeclaredField("m");
            declaredField.setAccessible(true);
            this.channel = (Channel)declaredField.get(((CraftPlayer)this.player).getHandle().playerConnection.networkManager);
            declaredField.setAccessible(false);
        }
        catch (Exception ex) {
            SpigotGuardLogger.exception("Could not initialize packet injector", ex);
        }
    }
    
    @Override
    public void injectListener(final User user) {
        this.channel.pipeline().addAfter("decoder", String.valueOf(new StringBuilder().append("SpigotGuard_").append(this.player.getName())), (ChannelHandler)new PacketDecoder_1_7(this, user));
    }
}
