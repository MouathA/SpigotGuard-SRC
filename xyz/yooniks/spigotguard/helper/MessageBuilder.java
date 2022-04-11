package xyz.yooniks.spigotguard.helper;

import org.apache.commons.lang.*;
import xyz.yooniks.spigotguard.config.*;
import org.bukkit.*;

public class MessageBuilder
{
    private String text;
    
    public static MessageBuilder newBuilder(final String s) {
        return new MessageBuilder(s);
    }
    
    @Override
    public String toString() {
        return this.text;
    }
    
    public MessageBuilder withField(final String s, final String s2) {
        this.text = StringUtils.replace(this.text, s, s2);
        return this;
    }
    
    public MessageBuilder stripped() {
        return this.withField("%nl%", "\n");
    }
    
    public MessageBuilder prefix() {
        return this.withField("{PREFIX}", Settings.IMP.MESSAGES.PREFIX);
    }
    
    public MessageBuilder(final String text) {
        this.text = text;
    }
    
    public MessageBuilder coloured() {
        this.text = ChatColor.translateAlternateColorCodes('&', this.text);
        return this.withField(">>", "»");
    }
}
