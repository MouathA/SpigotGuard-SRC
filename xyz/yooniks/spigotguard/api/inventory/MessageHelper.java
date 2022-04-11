package xyz.yooniks.spigotguard.api.inventory;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.bukkit.*;

public final class MessageHelper
{
    public static List<String> colored(final List<String> list) {
        return list.stream().map((Function<? super Object, ?>)MessageHelper::colored).collect((Collector<? super Object, ?, List<String>>)Collectors.toList());
    }
    
    private MessageHelper() {
    }
    
    public static String colored(final String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
