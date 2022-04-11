package xyz.yooniks.spigotguard.user;

import java.io.*;
import xyz.yooniks.spigotguard.event.*;
import xyz.yooniks.spigotguard.network.*;
import java.util.function.*;
import java.util.*;

public class User implements Serializable
{
    private String ip;
    private String name;
    private final UUID uuid;
    private boolean injectedPacketDecoder;
    private final List<ExploitDetails> attempts;
    private final Map<String, Integer> packetsSent;
    private long lastJoin;
    private PacketInjector packetInjector;
    
    public PacketInjector getPacketInjector() {
        return this.packetInjector;
    }
    
    public void addAttempts(final List<ExploitDetails> list) {
        this.attempts.addAll(list);
    }
    
    public User(final String name, final String ip, final UUID uuid) {
        this.attempts = new ArrayList<ExploitDetails>();
        this.injectedPacketDecoder = false;
        this.name = name;
        this.ip = ip;
        this.uuid = uuid;
        this.packetsSent = new HashMap<String, Integer>();
    }
    
    public void sortAttempts() {
        this.attempts.sort(Comparator.comparing((Function<? super ExploitDetails, ? extends Comparable>)User::lambda$sortAttempts$0));
    }
    
    public Map<String, Integer> getPacketsSent() {
        return this.packetsSent;
    }
    
    public List<ExploitDetails> getAttempts() {
        return new ArrayList<ExploitDetails>(this.attempts);
    }
    
    public void setPacketInjector(final PacketInjector packetInjector) {
        this.packetInjector = packetInjector;
    }
    
    public long getLastJoin() {
        return this.lastJoin;
    }
    
    private static Date lambda$sortAttempts$0(final ExploitDetails exploitDetails) {
        return new Date(exploitDetails.getTime());
    }
    
    public void setLastJoin(final long lastJoin) {
        this.lastJoin = lastJoin;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getIp() {
        return this.ip;
    }
    
    public UUID getUuid() {
        return this.uuid;
    }
    
    public boolean isInjectedPacketDecoder() {
        return this.injectedPacketDecoder;
    }
    
    public int increaseAndGetReceivedPackets(final String s) {
        final int intValue = this.packetsSent.getOrDefault(s, 0);
        this.packetsSent.put(s, intValue + 1);
        return intValue;
    }
    
    public void setIp(final String ip) {
        this.ip = ip;
    }
    
    public void setInjectedPacketDecoder(final boolean injectedPacketDecoder) {
        this.injectedPacketDecoder = injectedPacketDecoder;
    }
    
    public void cleanup() {
        this.packetsSent.clear();
    }
}
