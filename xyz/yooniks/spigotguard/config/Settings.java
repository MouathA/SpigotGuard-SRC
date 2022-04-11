package xyz.yooniks.spigotguard.config;

import java.util.*;
import java.io.*;

public class Settings extends Config
{
    @Create
    public INVENTORIES INVENTORIES;
    @Comment({ "SpigotGuard - the best AntiCrash protection for your minecraft server", "Made by yooniks, discord: yooniks#0289, discord server: https://www.mc-protection.eu/discord", "For BungeeCord security ( Anti Bot & Anti Crash ) we recommend: https://www.mc-protection.eu/products/", "Read more about SpigotGuard or tell your friends to buy it too! https://www.mc-protection.eu/products/" })
    @Create
    public MESSAGES MESSAGES;
    @Comment({ "Increase it if <ByteBuf/Too big packet size> has false positives", "Max packet size" })
    public int VIAVERSION_MAX_CAPACITY;
    @Comment({ "Should we allow creativeSlot packets when player is without gamemode? He may be not able to use printer if it is set to FALSE." })
    public boolean ALLOW_CREATIVE_INVENTORY_CLICK_WITHOUT_GAMEMODE;
    @Create
    public HACKER_BOOK HACKER_BOOK;
    public String NULL_ADDRESS_KICK;
    @Comment({ "Available types:", "FIND <- Automatically finds a NMS version", "v1_7, v1_8, v1_12, v1_14, v1_15", "You can set it manually if you are using custom spigot and the SpigotGuard cannot get your nms server version." })
    public String NMS_VERSION;
    @Create
    public KICK KICK;
    @Create
    public BOOK BOOK;
    @Create
    public PACKET_DECODER PACKET_DECODER;
    @Ignore
    public static final Settings IMP;
    @Create
    public SQL SQL;
    public boolean BLOCK_ITEMNAME_WHEN_NO_ANVIL;
    @Comment({ "Should we use our own checks built-in into ViaVersion? (packet-size checks)" })
    public boolean VIAVERSION_INTEGRATION;
    @Comment({ "Commands that well be executed when player tried to crash server", "Available variables: {PLAYER} and {IP}", "Do not use dashes!" })
    public List<String> COMMANDS_WHEN_SURE;
    @Comment({ "Should we block null address in LoginEvents?" })
    public boolean BLOCK_NULL_ADDRESS;
    @Create
    public PLACE_CHECKS PLACE_CHECKS;
    @Comment({ "Set it to bigger value if stuff like <tried to place book but is holding xxx> has false positives" })
    public int BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN;
    @Create
    public PAYLOAD_SETTINGS PAYLOAD_SETTINGS;
    @Create
    public POSITION_CHECKS POSITION_CHECKS;
    @Comment({ "How big should be the difference beetwen NOW & LAST arm animation packets" })
    public int ARM_ANIMATION_TIMESTAMP;
    @Create
    public OTHER_NBT OTHER_NBT;
    @Create
    public SKULL_CHECKS SKULL_CHECKS;
    @Comment({ "Put your license key here. If you dont have a one, please contact us on discord: https://mc-protection.eu/discord and send proof of payment to get your license key." })
    public String LICENSE;
    public boolean DEBUG;
    
    public Settings() {
        this.LICENSE = "yourLicenseKeyHere";
        this.COMMANDS_WHEN_SURE = Arrays.asList("exampleCommand {PLAYER}");
        this.DEBUG = false;
        this.ARM_ANIMATION_TIMESTAMP = 20;
        this.BLOCK_ITEMNAME_WHEN_NO_ANVIL = true;
        this.NMS_VERSION = "FIND";
        this.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN = 1000;
        this.VIAVERSION_INTEGRATION = true;
        this.VIAVERSION_MAX_CAPACITY = 5000;
        this.ALLOW_CREATIVE_INVENTORY_CLICK_WITHOUT_GAMEMODE = false;
        this.BLOCK_NULL_ADDRESS = true;
        this.NULL_ADDRESS_KICK = "&cYour login data is invalid! Please try relogging.";
    }
    
    public void reload(final File file) {
        this.load(file);
        this.save(file);
    }
    
    static {
        IMP = new Settings();
    }
    
    public static class SKULL_CHECKS
    {
        @Comment({ "Should we block head/skull textures that do not start with http://textures.minecraft.net ?" })
        public boolean BLOCK_INVALID_HEAD_TEXTURE;
        
        public SKULL_CHECKS() {
            this.BLOCK_INVALID_HEAD_TEXTURE = true;
        }
    }
    
    public static class BOOK
    {
        @Comment({ "Should we block BOOK_AND_QUILL and WRITTEN_BOOK on server? People will be kicked for using it" })
        public boolean BAN_BOOKS;
        @Comment({ "Max fast-check nbt item length" })
        public int FAST_CHECK_MAX_LENGTH;
        public int MAX_PAGES;
        @Comment({ "Max similar pages. Default: 4, Crash books very often use the same page text like 40 times." })
        public int MAX_SIMILAR_PAGES;
        @Comment({ "Max page size without color codes" })
        public int MAX_STRIPPED_PAGE_SIZE;
        @Comment({ "Limit of 2-byte chars. 2-byte chars are non-english chars like China chars, Emojis and similars." })
        public int MAX_2BYTE_CHARS;
        @Comment({ "Set it to higher value if stuff like <too large page> has false positives" })
        public int MAX_PAGE_SIZE;
        @Comment({ "Set it to true if your server does not use books very often", "It is fast, lightweight check that detects crash books very fast", "You could enable it if people do not even have access to book_and_quill and written_book on your server" })
        public boolean FAST_CHECK;
        
        public BOOK() {
            this.MAX_PAGES = 50;
            this.MAX_PAGE_SIZE = 900;
            this.MAX_STRIPPED_PAGE_SIZE = 256;
            this.BAN_BOOKS = false;
            this.MAX_SIMILAR_PAGES = 4;
            this.MAX_2BYTE_CHARS = 15;
            this.FAST_CHECK = false;
            this.FAST_CHECK_MAX_LENGTH = 500;
        }
    }
    
    @Comment({ "Do not use '\\ n', use %nl%" })
    public static class MESSAGES
    {
        public String NOTIFICATION_MESSAGE;
        public String PREFIX;
        public String NOTIFICATION_PERMISSION;
        
        public MESSAGES() {
            this.PREFIX = "&cSpigotGuard &8>> ";
            this.NOTIFICATION_PERMISSION = "spigotguard.notification";
            this.NOTIFICATION_MESSAGE = "%nl%{PREFIX}&7Player &c{PLAYER}&7 tried to crash the server.%nl%{PREFIX}&7Packet: &c{PACKET}%nl%{PREFIX}&7Details: &c{DETAILS}%nl%{PREFIX}&7Read more about this crash attempt: &c/spigotguard {PLAYER}&7%nl%&7";
        }
    }
    
    public static class PACKET_DECODER
    {
        @Comment({ "Most of people use chests with nbt to crash your Creative server by placing a lot of chests with chests inside on their plotme home. It overloads chunks and make server crash." })
        public boolean BLOCK_CHESTS_WITH_NBT_ON_CREATIVE;
        public int MAX_WINDOW_SIZE;
        @Comment({ "Max packet decoding size" })
        public int MAX_MAIN_SIZE;
        @Comment({ "1.12+ crasher. Fixed by Tuinity since 1.15 i think. If you are running 1.12.2 server, mostly Creative, we fix it for you.", "Set it to -1 to disable if you have false positives." })
        public int MAX_AUTO_RECIPE_RATE;
        public int MAX_PAYLOAD_SIZE;
        public int BLOCK_CHESTS_WITH_NBT_BLOCK_SIZE_HIGHER_THAN;
        public boolean ENABLED;
        public int MAX_CREATIVE_SIZE;
        @Create
        public VIAVERSION VIAVERSION;
        public String BLOCK_CHESTS_WITH_NBT_KICK_MESSAGE;
        public int MAX_PLACE_SIZE;
        
        public PACKET_DECODER() {
            this.ENABLED = true;
            this.MAX_MAIN_SIZE = 5000;
            this.MAX_WINDOW_SIZE = 1520;
            this.MAX_PLACE_SIZE = 1520;
            this.MAX_CREATIVE_SIZE = 5000;
            this.MAX_PAYLOAD_SIZE = 1700;
            this.MAX_AUTO_RECIPE_RATE = 5;
            this.BLOCK_CHESTS_WITH_NBT_ON_CREATIVE = true;
            this.BLOCK_CHESTS_WITH_NBT_BLOCK_SIZE_HIGHER_THAN = 250;
            this.BLOCK_CHESTS_WITH_NBT_KICK_MESSAGE = "&8[&6SpigotGuard&8] &cPlease do not use chests with already existing NBT data on creative.";
        }
        
        @Comment({ "Some changes when you use our ViaVersion fork" })
        public static class VIAVERSION
        {
            @Comment({ "Should we block (clear) every BOOK (written books, book_and_quill) packet, even if it does not exceed packet size limit?", "It could be very useful if you do not allow using/crafting books (written books, book and quill) on your server and want to block every exploit faster" })
            public boolean CLEAR_ALL_BOOKS;
            @Comment({ "FOR ViaVersion - Max packet size when there item inside the packet" })
            public int MAX_ITEM_SIZE;
            @Comment({ "Should we ONLY check packet size inside ViaVersion if the item is book?" })
            public boolean CHECK_ONLY_BOOKS;
            
            public VIAVERSION() {
                this.MAX_ITEM_SIZE = 2100;
                this.CHECK_ONLY_BOOKS = true;
                this.CLEAR_ALL_BOOKS = false;
            }
        }
    }
    
    public static class HACKER_BOOK
    {
        public boolean ENABLED;
        public String NEXT_PAGE;
        public String CONTENT;
        
        public HACKER_BOOK() {
            this.ENABLED = true;
            this.CONTENT = "&n&cHello, &9&l{PLAYER}&r!%nl%Looks like the last time you were here you got kicked for suspicious activity and we think you tried to crash the server. To see details please open next page.";
            this.NEXT_PAGE = "&eIf it is our mistake, please &econtact the staff.";
        }
    }
    
    public static class POSITION_CHECKS
    {
        public boolean ALLOW_INVALID_VEHICLE_MOVEMENT_V3;
        @Comment({ "Should we enable FLY CRASHER check?", "If it has false positives, just disable it." })
        public boolean CHECK_FLY_CRASHER;
        public boolean ALLOW_INVALID_VEHICLE_MOVEMENT_V4;
        @Comment({ "Should we enable CHUNK CRASHER check?", "If it has false positives, just disable it." })
        public boolean CHECK_CHUNK_CRASHER;
        @Comment({ "Should we enable CHUNK CRASHER v2 check?", "If it has false positives, just disable it." })
        public boolean CHECK_CHUNK_CRASHER_V2;
        public boolean ALLOW_INVALID_VEHICLE_MOVEMENT_V5;
        public boolean CHECK_YAW_CRASHER;
        public boolean PREVENT_MOVING_INTO_UNLOADED_CHUNKS;
        @Comment({ "Should we allow POSITION packets with Double.MAX_VALUE values? Phase cheats usually use it! It can also lag a server a bit." })
        public boolean ALLOW_INVALID_MOVEMENT_V2;
        @Comment({ "Should we allow POSITION packets with Integer.MAX_VALUE values? Phase cheats usually use it! It can also lag a server a bit." })
        public boolean ALLOW_INVALID_MOVEMENT_V1;
        public int MAX_DISTANCE_BETWEEN_PLAYER_AND_BLOCK_POSITION;
        
        public POSITION_CHECKS() {
            this.ALLOW_INVALID_MOVEMENT_V1 = true;
            this.ALLOW_INVALID_MOVEMENT_V2 = false;
            this.ALLOW_INVALID_VEHICLE_MOVEMENT_V3 = false;
            this.ALLOW_INVALID_VEHICLE_MOVEMENT_V4 = false;
            this.ALLOW_INVALID_VEHICLE_MOVEMENT_V5 = false;
            this.CHECK_CHUNK_CRASHER = true;
            this.CHECK_FLY_CRASHER = true;
            this.CHECK_CHUNK_CRASHER_V2 = true;
            this.MAX_DISTANCE_BETWEEN_PLAYER_AND_BLOCK_POSITION = 30;
            this.PREVENT_MOVING_INTO_UNLOADED_CHUNKS = false;
            this.CHECK_YAW_CRASHER = true;
        }
    }
    
    @Comment({ "Database Setup" })
    public static class SQL
    {
        @Comment({ "Available types: sqlite & mysql" })
        public String STORAGE_TYPE;
        public String PASSWORD;
        public int PORT;
        @Comment({ "After how many days to remove players from the database, which have been on server and no longer entered. Use 0 or less to stop" })
        public int PURGE_TIME;
        @Comment({ "Should we even enable database? If no, spigotguard will save no data." })
        public boolean ENABLED;
        @Comment({ "Inform console about purging" })
        public boolean PURGE_CONSOLE_INFO;
        public String USER;
        public String DATABASE;
        @Comment({ "Settings for mysql" })
        public String HOSTNAME;
        
        public SQL() {
            this.PURGE_TIME = 10;
            this.PURGE_CONSOLE_INFO = true;
            this.HOSTNAME = "localhost";
            this.PORT = 3306;
            this.USER = "user";
            this.PASSWORD = "password";
            this.DATABASE = "database";
            this.STORAGE_TYPE = "sqlite";
            this.ENABLED = true;
        }
    }
    
    @Comment({ "How should we kick player when he tries to crash the server?" })
    public static class KICK
    {
        public String MESSAGE;
        @Comment({ "Available types:", "0 - Instant channel close, kick without any message", "1 - Kick with message (a bit slower because we have to send packet to player that he is kicked)" })
        public int TYPE;
        
        public KICK() {
            this.TYPE = 0;
            this.MESSAGE = "&cSpigotGuard &8> &7You have been kicked for trying to &ccrash&7 the server.";
        }
    }
    
    public static class PLACE_CHECKS
    {
        @Comment({ "Should player be able to send PacketPlayInBlockPlace with FIREWORK and similars when he is not holding it? It is surely exploit." })
        public boolean BLOCK_PLACING_FIREWORK_WHEN_NOT_HOLDING_IT;
        @Comment({ "Should player be able to send PacketPlayInBlockPlace with WRITTEN_BOOK and similars when he is not holding it? It is surely exploit." })
        public boolean BLOCK_PLACING_BOOK_WHEN_NOT_HOLDING_IT;
        @Comment({ "Should player be able to send PacketPlayInBlockPlace with BANNER and similars when he is not holding it? It is surely exploit." })
        public boolean BLOCK_PLACING_BANNER_WHEN_NOT_HOLDING_IT;
        
        public PLACE_CHECKS() {
            this.BLOCK_PLACING_BOOK_WHEN_NOT_HOLDING_IT = true;
            this.BLOCK_PLACING_FIREWORK_WHEN_NOT_HOLDING_IT = true;
            this.BLOCK_PLACING_BANNER_WHEN_NOT_HOLDING_IT = true;
        }
    }
    
    public static class PAYLOAD_SETTINGS
    {
        @Comment({ "Should we block sending payload with MC|BSign when no book was used?" })
        public boolean BLOCK_BOOK_SIGNING_WHEN_NO_BOOK_PLACED;
        @Comment({ "Max CustomPayload packet size" })
        public int MAX_CAPACITY;
        @Comment({ "Should we block sending payload (book) packets while player does not hold a book in a hand?" })
        public boolean FAIL_WHEN_NOT_HOLDING_BOOK;
        
        public PAYLOAD_SETTINGS() {
            this.MAX_CAPACITY = 2500;
            this.FAIL_WHEN_NOT_HOLDING_BOOK = true;
            this.BLOCK_BOOK_SIGNING_WHEN_NO_BOOK_PLACED = true;
        }
    }
    
    public static class OTHER_NBT
    {
        public int MAX_ARRAY_SIZE;
        public int MAX_LIST_SIZE;
        public boolean LIMIT_CHEST_NBT;
        @Comment({ "Set it to higher value if stuff like <too large list length> has false positives" })
        public int MAX_LIST_CONTENT;
        public int MAX_LISTS;
        public int MAX_KEYS;
        public int FIREWORK_LIMIT;
        public int FIREWORKS_CHARGE_LIMIT;
        public int MAX_SIMPLE_NBT_LIMIT;
        public boolean SIMPLE_NBT_LIMIT;
        public int BANNER_LIMIT;
        
        public OTHER_NBT() {
            this.MAX_LISTS = 10;
            this.MAX_LIST_SIZE = 50;
            this.MAX_LIST_CONTENT = 900;
            this.MAX_KEYS = 20;
            this.MAX_ARRAY_SIZE = 50;
            this.FIREWORKS_CHARGE_LIMIT = 800;
            this.FIREWORK_LIMIT = 300;
            this.BANNER_LIMIT = 200;
            this.SIMPLE_NBT_LIMIT = true;
            this.MAX_SIMPLE_NBT_LIMIT = 10000;
            this.LIMIT_CHEST_NBT = true;
        }
    }
    
    public static class INVENTORIES
    {
        @Create
        public RECENT_DETECTIONS_INVENTORY RECENT_DETECTIONS_INVENTORY;
        @Create
        public PLAYER_INFO_INVENTORY PLAYER_INFO_INVENTORY;
        @Create
        public MAIN_INVENTORY MAIN_INVENTORY;
        
        public static class RECENT_DETECTIONS_INVENTORY
        {
            @Create
            public RECENT_DETECTION_ITEM RECENT_DETECTION_ITEM;
            public String NAME;
            public int SIZE;
            @Create
            public NO_DETECTIONS_ITEM NO_DETECTIONS_ITEM;
            
            public RECENT_DETECTIONS_INVENTORY() {
                this.NAME = "&cRecent detections";
                this.SIZE = 27;
            }
            
            public static class NO_DETECTIONS_ITEM
            {
                public String NAME;
                public List<String> LORE;
                
                public NO_DETECTIONS_ITEM() {
                    this.NAME = "&cNo detections found";
                    this.LORE = Arrays.asList(" &7We did not find any people that tried to crash the server :(", " &7Looks like your players are afraid to try crashers!");
                }
            }
            
            public static class RECENT_DETECTION_ITEM
            {
                public String NAME;
                public List<String> LORE;
                
                public RECENT_DETECTION_ITEM() {
                    this.NAME = "&c{PLAYER-NAME}";
                    this.LORE = Arrays.asList(" &7Packet name: &c{PACKET}", " &7Details: &c{DETAILS}", " &7Time of crash attempt: &c{TIME}", " &7Player last seen at: &c{PLAYER-LAST-SEEN}", " ", "   &9&lHow to fix it, if it is false positive/detection?", " &7{HOW-TO-FIX-FALSE-POSITIVE}");
                }
            }
        }
        
        public static class MAIN_INVENTORY
        {
            public int SIZE;
            @Create
            public RECENT_DETECTIONS_ITEM RECENT_DETECTIONS_ITEM;
            @Create
            public RELOAD_ITEM RELOAD_ITEM;
            public String NAME;
            
            public MAIN_INVENTORY() {
                this.NAME = "&cSpigotGuard management";
                this.SIZE = 27;
            }
            
            public static class RECENT_DETECTIONS_ITEM
            {
                public List<String> LORE;
                public String NAME;
                public int SLOT;
                public String MATERIAL;
                
                public RECENT_DETECTIONS_ITEM() {
                    this.MATERIAL = "DIAMOND_CHESTPLATE";
                    this.NAME = "&cRecent detections";
                    this.LORE = Arrays.asList("&8&l&m>&r &7Click to open inventory with recent detections", "&8&l&m>&r &7You will see who tried to crash the server in last time!");
                    this.SLOT = 12;
                }
            }
            
            public static class RELOAD_ITEM
            {
                public List<String> LORE;
                public int SLOT;
                public String MATERIAL;
                public String NAME;
                
                public RELOAD_ITEM() {
                    this.MATERIAL = "STICK";
                    this.NAME = "&cReload configs";
                    this.LORE = Arrays.asList("&8&l&m>&r &7Click to reload configuration files");
                    this.SLOT = 14;
                }
            }
        }
        
        public static class PLAYER_INFO_INVENTORY
        {
            public int SIZE;
            @Create
            public CRASH_ATTEMPT_ITEM CRASH_ATTEMPT_ITEM;
            public String NAME;
            
            public PLAYER_INFO_INVENTORY() {
                this.NAME = "&cCrash attempts of {PLAYER}";
                this.SIZE = 27;
            }
            
            public static class CRASH_ATTEMPT_ITEM
            {
                public String NAME;
                public List<String> LORE;
                
                public CRASH_ATTEMPT_ITEM() {
                    this.NAME = "&c{TIME}";
                    this.LORE = Arrays.asList(" &7Packet name: &c{PACKET}", " &7Details: &c{DETAILS}", " &7Time of crash attempt: &c{TIME}", " &7Player was last seen at: &c{LAST-SEEN}", " &7IP: &c{IP}", " ", "   &9&lHow to fix it, if it is false positive/detection?", " &7{HOW-TO-FIX-FALSE-POSITIVE}");
                }
            }
        }
    }
}
