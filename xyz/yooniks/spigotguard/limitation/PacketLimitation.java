package xyz.yooniks.spigotguard.limitation;

import org.bukkit.configuration.file.*;
import java.util.*;
import org.bukkit.configuration.*;

public class PacketLimitation
{
    private final Map<String, LimitablePacket> limitablePacketMap;
    
    public PacketLimitation() {
        this.limitablePacketMap = new HashMap<String, LimitablePacket>();
    }
    
    public LimitablePacket getLimit(final String s) {
        return this.limitablePacketMap.get(s);
    }
    
    public boolean isLimitable(final String s) {
        return this.limitablePacketMap.containsKey(s);
    }
    
    public void initialize(final FileConfiguration fileConfiguration) {
        for (final String s : fileConfiguration.getConfigurationSection("packet-limitter.limits").getKeys(false)) {
            final ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection(String.valueOf(new StringBuilder().append("packet-limitter.limits.").append(s)));
            this.limitablePacketMap.put(s, new LimitablePacket(configurationSection.getInt("limit"), configurationSection.getBoolean("cancel-only")));
        }
    }
}
