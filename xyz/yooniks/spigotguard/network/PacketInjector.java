package xyz.yooniks.spigotguard.network;

import org.bukkit.entity.*;
import xyz.yooniks.spigotguard.user.*;

public abstract class PacketInjector
{
    protected final Player player;
    protected PacketDecoder packetDecoder;
    
    public abstract void injectListener(final User p0);
    
    public PacketInjector(final Player player) {
        this.player = player;
    }
    
    public abstract void uninjectListener();
    
    public PacketDecoder getPacketDecoder() {
        return this.packetDecoder;
    }
    
    public Player getPlayer() {
        return this.player;
    }
}
