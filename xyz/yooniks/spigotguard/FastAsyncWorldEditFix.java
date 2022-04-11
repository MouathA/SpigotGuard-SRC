package xyz.yooniks.spigotguard;

import org.bukkit.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import java.util.*;
import java.io.*;
import org.bukkit.configuration.file.*;

public class FastAsyncWorldEditFix
{
    public boolean isLoaded() {
        return Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
    }
    
    public void fixConfig() {
        final File file = new File("plugins/FastAsyncWorldEdit", "commands.yml");
        if (!file.exists()) {
            SpigotGuardLogger.log(Level.WARNING, "commands.yml cannot be found. FAWE fix will not be implement.", new Object[0]);
            return;
        }
        final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(file);
        if (((FileConfiguration)loadConfiguration).getStringList("BrushOptionsCommands.targetoffset.aliases").size() > 0) {
            SpigotGuardPlugin.getInstance().getLogger().warning("Found FastAsyncWorldEdit crash command bug and fixed it, a restart may be required to reload the changes in FAWE!");
            SpigotGuardLogger.log(Level.WARNING, "We can't fix FAWE crashes to 100% and we don't recommend to use FAWE on a production server anyway, use it at your own risk!", new Object[0]);
            ((FileConfiguration)loadConfiguration).set("BrushOptionsCommands.targetoffset.aliases", (Object)new ArrayList());
            ((FileConfiguration)loadConfiguration).set("UtilityCommands./calc.aliases", (Object)new ArrayList());
        }
        try {
            ((FileConfiguration)loadConfiguration).save(file);
        }
        catch (IOException ex) {
            SpigotGuardPlugin.getInstance().getLogger().warning(String.valueOf(new StringBuilder().append("Could not override FAWE config! ").append(ex.getMessage())));
        }
    }
}
