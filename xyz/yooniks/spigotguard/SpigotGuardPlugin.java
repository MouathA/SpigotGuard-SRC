package xyz.yooniks.spigotguard;

import org.bukkit.plugin.java.*;
import xyz.yooniks.spigotguard.classloader.*;
import xyz.yooniks.spigotguard.nms.*;
import xyz.yooniks.spigotguard.limitation.*;
import xyz.yooniks.spigotguard.api.inventory.*;
import xyz.yooniks.spigotguard.sql.*;
import xyz.yooniks.spigotguard.user.*;
import xyz.yooniks.spigotguard.thread.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import java.util.function.*;
import xyz.yooniks.spigotguard.config.*;
import java.nio.charset.*;
import org.bukkit.*;
import org.bukkit.event.*;
import xyz.yooniks.spigotguard.listener.*;
import xyz.yooniks.spigotguard.listener.inventory.*;
import xyz.yooniks.spigotguard.inventory.*;
import xyz.yooniks.spigotguard.command.*;
import org.bukkit.command.*;
import java.net.*;
import java.io.*;
import xyz.yooniks.spigotguard.notification.*;
import org.bukkit.plugin.*;

public final class SpigotGuardPlugin extends JavaPlugin
{
    private SpigotGuardClassLoaded spigotGuardClassLoaded;
    private NMSVersion nmsVersion;
    private PacketLimitation packetLimitation;
    private PhasmatosInventory managementInventory;
    private SqlDatabase sqlDatabase;
    private UserManager userManager;
    
    private void lambda$onDisable$3(final User user) {
        this.sqlDatabase.saveUser(user);
    }
    
    public void onDisable() {
        if (this.userManager == null) {
            return;
        }
        new Thread(new SpigotGuardUninjector(this.userManager.getUsers())).start();
        new Thread(this::lambda$onDisable$4);
    }
    
    private void lambda$onEnable$2() {
        SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("> You are running: ").append(this.getDescription().getVersion()).append(" SpigotGuard, Our discord: https://mc-protection.eu/discord (private: yooniks#0289), Webpage: https://mc-protection.eu")), new Object[0]);
    }
    
    public void setManagementInventory(final PhasmatosInventory managementInventory) {
        this.managementInventory = managementInventory;
    }
    
    private void lambda$onEnable$1() {
        this.userManager.getUsers().forEach(User::cleanup);
    }
    
    private void lambda$onEnable$0() {
        this.sqlDatabase.setupConnect(this.userManager);
    }
    
    public PacketLimitation getPacketLimitation() {
        return this.packetLimitation;
    }
    
    public NMSVersion getNmsVersion() {
        return this.nmsVersion;
    }
    
    private void lambda$onDisable$4() {
        SpigotGuardLogger.log(Level.INFO, "[Database] Saving sql users...", new Object[0]);
        this.userManager.getUsers().forEach(this::lambda$onDisable$3);
        this.sqlDatabase.close();
        SpigotGuardLogger.log(Level.INFO, "[Database] Saved sql users!", new Object[0]);
    }
    
    public void setSpigotGuardClassLoaded(final SpigotGuardClassLoaded spigotGuardClassLoaded) {
        this.spigotGuardClassLoaded = spigotGuardClassLoaded;
    }
    
    public void onEnable() {
        this.saveDefaultConfig();
        Settings.IMP.reload(new File(this.getDataFolder(), "settings.yml"));
        try {
            final URLConnection openConnection = new URL("https://mc-protection.eu/api/verifyLicense/").openConnection();
            openConnection.setRequestProperty("User-Agent", "Java client");
            final HttpURLConnection httpURLConnection = (HttpURLConnection)openConnection;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("User-Agent", "Java client");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setDoOutput(true);
            final byte[] bytes = String.valueOf(new StringBuilder().append("license=").append(Settings.IMP.LICENSE).append("&resource=2")).getBytes(StandardCharsets.UTF_8);
            httpURLConnection.connect();
            final OutputStream outputStream = httpURLConnection.getOutputStream();
            try {
                outputStream.write(bytes);
                if (outputStream != null) {
                    outputStream.close();
                }
            }
            catch (Throwable t) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    }
                    catch (Throwable t2) {
                        t.addSuppressed(t2);
                    }
                }
                throw t;
            }
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openConnection.getInputStream()));
            StringBuilder sb;
            try {
                sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                }
                bufferedReader.close();
            }
            catch (Throwable t3) {
                try {
                    bufferedReader.close();
                }
                catch (Throwable t4) {
                    t3.addSuppressed(t4);
                }
                throw t3;
            }
            if (!String.valueOf(sb).contains("true")) {
                System.out.println(String.valueOf(sb));
                this.getLogger().warning("License is invalid. Please put your license key into SpigotGuard/settings.yml file. To get your license, download a .zip file from https://mc-protection.eu/products and in the .zip file you will find your license key.");
                this.getPluginLoader().disablePlugin((Plugin)this);
                return;
            }
            this.getLogger().info("License valid. Thanks for using our product!");
        }
        catch (Exception ex) {}
        this.spigotGuardClassLoaded = new SpigotGuardClassLoaded("[1]");
        if (!this.getDescription().getAuthors().contains("yooniks") || !this.getDescription().getName().equals("SpigotGuard")) {
            this.getLogger().warning("Please do not change the SpigotGuard plugin.yml =c");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
            return;
        }
        String nms_VERSION = Settings.IMP.NMS_VERSION;
        if (Integer.valueOf(2158009).equals(nms_VERSION.toUpperCase().hashCode())) {
            try {
                nms_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            }
            catch (ArrayIndexOutOfBoundsException ex2) {
                nms_VERSION = "1_8";
                SpigotGuardLogger.log(Level.WARNING, "Could not get the NMS Version of your spigot server! We are using 1_8_R3 as default, if it's not your nms version then change it manually in settings.yml", new Object[0]);
            }
        }
        else {
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Using manually set nms version! ").append(nms_VERSION)), new Object[0]);
        }
        this.nmsVersion = NMSVersion.find(nms_VERSION);
        SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Using ").append(this.nmsVersion.getName()).append(" NMS version as PacketInjector & PacketDecoder!")), new Object[0]);
        if (this.nmsVersion == NMSVersion.UNSUPPORTED) {
            SpigotGuardLogger.log(Level.WARNING, "Your version is UNSUPPORTED! Are you sure you are running valid mc server version? The plugin might not work if your version isn't supported", new Object[0]);
        }
        NotificationCache notificationCache;
        if (this.nmsVersion == NMSVersion.ONE_DOT_SEVEN_R4) {
            notificationCache = new NotificationCache_1_7();
        }
        else {
            notificationCache = new NotificationCacheDefault();
        }
        (this.packetLimitation = new PacketLimitation()).initialize(this.getConfig());
        this.userManager = new UserManager();
        this.sqlDatabase = new SqlDatabase();
        if (Settings.IMP.SQL.ENABLED) {
            this.getServer().getScheduler().runTaskAsynchronously((Plugin)this, this::lambda$onEnable$0);
        }
        final PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents((Listener)new PacketInjectorListener(this.userManager, new PacketInjectorListener.PacketInjections(this.nmsVersion), this.sqlDatabase), (Plugin)this);
        pluginManager.registerEvents((Listener)new NotificationListener(notificationCache), (Plugin)this);
        pluginManager.registerEvents((Listener)new SignChangeListener(), (Plugin)this);
        pluginManager.registerEvents((Listener)new FaweTabCompletionListener(), (Plugin)this);
        if (Settings.IMP.POSITION_CHECKS.PREVENT_MOVING_INTO_UNLOADED_CHUNKS) {
            pluginManager.registerEvents((Listener)new MovementListener(), (Plugin)this);
        }
        if (this.nmsVersion == NMSVersion.ONE_DOT_FOURTEEN || this.nmsVersion == NMSVersion.ONE_DOT_FIVETEEN || this.nmsVersion == NMSVersion.ONE_DOT_SIXTEEN || this.nmsVersion == NMSVersion.ONE_DOT_SIXTEEN_R2 || this.nmsVersion == NMSVersion.ONE_DOT_SIXTEEN_R3 || this.nmsVersion == NMSVersion.ONE_DOT_SEVENTEEN_R1) {
            pluginManager.registerEvents((Listener)new InventoryListener_1_14(), (Plugin)this);
        }
        else {
            pluginManager.registerEvents((Listener)new DefaultInventoryListener(), (Plugin)this);
        }
        new InventoryLoader().loadInventories(this);
        this.getCommand("spigotguard").setExecutor((CommandExecutor)new SpigotGuardCommand(this.managementInventory));
        this.getServer().getScheduler().runTaskTimerAsynchronously((Plugin)this, this::lambda$onEnable$1, 10L, 10L);
        this.getServer().getScheduler().runTaskLaterAsynchronously((Plugin)this, this::lambda$onEnable$2, 100L);
        this.getLogger().info("Thanks for using our resource. We recommend you to buy our other products on: https://mc-protection.eu");
    }
    
    public static SpigotGuardPlugin getInstance() {
        return (SpigotGuardPlugin)getPlugin((Class)SpigotGuardPlugin.class);
    }
    
    public SpigotGuardClassLoaded getSpigotGuardClassLoaded() {
        return this.spigotGuardClassLoaded;
    }
    
    public UserManager getUserManager() {
        return this.userManager;
    }
    
    public PhasmatosInventory getManagementInventory() {
        return this.managementInventory;
    }
}
