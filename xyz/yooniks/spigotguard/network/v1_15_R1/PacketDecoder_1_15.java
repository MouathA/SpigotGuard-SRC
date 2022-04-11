package xyz.yooniks.spigotguard.network.v1_15_R1;

import xyz.yooniks.spigotguard.user.*;
import io.netty.channel.*;
import xyz.yooniks.spigotguard.config.*;
import xyz.yooniks.spigotguard.*;
import org.bukkit.plugin.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import java.nio.charset.*;
import java.util.regex.*;
import java.util.*;
import xyz.yooniks.spigotguard.network.*;
import xyz.yooniks.spigotguard.helper.*;
import io.netty.util.concurrent.*;
import xyz.yooniks.spigotguard.event.*;
import org.bukkit.event.*;
import xyz.yooniks.spigotguard.limitation.*;
import org.bukkit.event.inventory.*;
import org.bukkit.craftbukkit.v1_15_R1.inventory.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.lang.reflect.*;
import org.bukkit.inventory.*;
import io.netty.buffer.*;
import net.minecraft.server.v1_15_R1.*;

@ChannelHandler.Sharable
public class PacketDecoder_1_15 extends PacketDecoder
{
    private boolean disconnected;
    private static final Pattern URL_MATCHER;
    private long lastBookplace;
    private final User user;
    private static final PacketLimitation LIMITATION;
    
    @Override
    public User getUser() {
        return this.user;
    }
    
    protected void decode(final ChannelHandlerContext channelHandlerContext, final Object o, final List<Object> list) {
        if (this.disconnected) {
            return;
        }
        final ExploitDetails failure = this.failure(o);
        if (failure == null) {
            list.add(o);
            return;
        }
        if (failure.isCancelOnly()) {
            return;
        }
        this.disconnected = true;
        if (Settings.IMP.KICK.TYPE == 0) {
            channelHandlerContext.close().addListener(this::lambda$decode$1);
        }
        else {
            Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$2);
        }
        SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(SpigotGuardPlugin.getInstance().getSpigotGuardClassLoaded()).append(" Player {0} tried to crash the server, packet: {1}, details: {2}")), this.injector.getPlayer().getName(), o.getClass().getSimpleName(), failure.getDetails());
    }
    
    private ExploitDetails checkNbtTags(final String s, final ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        final NBTTagCompound tag = itemStack.getTag();
        final Item item = itemStack.getItem();
        if (tag == null) {
            if (Settings.IMP.DEBUG) {
                SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(s).append(" -> ").append(item.getName()).append(" -> no nbt detected")), new Object[0]);
            }
            return null;
        }
        if (Settings.IMP.BOOK.FAST_CHECK && (item instanceof ItemWrittenBook || item instanceof ItemBookAndQuill) && String.valueOf(tag.get("pages")).length() > Settings.IMP.BOOK.FAST_CHECK_MAX_LENGTH) {
            tag.remove("pages");
            tag.remove("author");
            tag.remove("title");
            return new ExploitDetails(this.user, s, "too large pages tag (fast-check)", false, false, "set \"book.fast-check\" in settings.yml to false or increase limit of \"book.fast-check-max-length\" ");
        }
        if (item instanceof ItemFireworks && tag.toString().length() > Settings.IMP.OTHER_NBT.FIREWORK_LIMIT) {
            return new ExploitDetails(this.user, s, "too big firework data", false, true, "set \"other-nbt.firework-limit\" in settings.yml to higher value");
        }
        if (item instanceof ItemFireworksCharge && tag.toString().length() > Settings.IMP.OTHER_NBT.FIREWORKS_CHARGE_LIMIT) {
            return new ExploitDetails(this.user, s, "too big firework_charge data", false, false, "set \"other-nbt.fireworks-charge-limit\" in settings.yml to higher value");
        }
        if (item instanceof ItemBanner && tag.toString().length() > Settings.IMP.OTHER_NBT.BANNER_LIMIT) {
            return new ExploitDetails(this.user, s, "too big banner data", false, false, "set \"other-nbt.banner-limit\" in settings.yml to higher value");
        }
        if (Settings.IMP.DEBUG) {
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(s).append(" -> ").append(item.getName()).append(" -> ").append(tag.toString())), new Object[0]);
        }
        final Set keys = tag.getKeys();
        final Settings imp = Settings.IMP;
        if (keys.size() > imp.OTHER_NBT.MAX_KEYS) {
            return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("too many keys (").append(keys.size()).append(")")), false, true, "set \"other-nbt.max-keys\" in settings.yml to higher value");
        }
        if (tag.hasKey("pages")) {
            if (imp.BOOK.BAN_BOOKS) {
                return new ExploitDetails(this.user, s, "using book while it is banned", false, true, "set \"book.ban-books\" in settings.yml to false");
            }
            final NBTTagList list = tag.getList("pages", 8);
            if (list.size() > imp.BOOK.MAX_PAGES) {
                return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("too many pages (").append(list.size()).append(")")), false, true, "set \"book.max-pages\" in settings.yml to higher value");
            }
            String s2 = "";
            int n = 0;
            for (int i = 0; i < list.size(); ++i) {
                final String string = list.getString(i);
                if (string.length() > imp.BOOK.MAX_PAGE_SIZE) {
                    return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("too large page content (").append(string.length()).append(")")), false, false, String.valueOf(new StringBuilder().append("increase value of \"book.max-page-size\" (bigger than: ").append(string.length()).append(") in settings.yml")));
                }
                if (string.split("extra").length > 8) {
                    return new ExploitDetails(this.user, s, "too many extra words", false, true);
                }
                if (s2.equals(string)) {
                    ++n;
                }
                s2 = string;
                if (n > imp.BOOK.MAX_SIMILAR_PAGES) {
                    return new ExploitDetails(this.user, s, "too many similar pages", false, true, "set \"book.max-similar-pages\" in settings.yml to higher value");
                }
                final String stripColor = ChatColor.stripColor(string.replaceAll("\\+", ""));
                if (stripColor == null || stripColor.equals("null")) {
                    return new ExploitDetails(this.user, s, "null stripped page", false, true);
                }
                if (stripColor.length() > imp.BOOK.MAX_STRIPPED_PAGE_SIZE) {
                    return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("too large stripped page content (").append(string.length()).append(")")), false, true, String.valueOf(new StringBuilder().append("set \"book.max-stripped-page-size\" in settings.yml to value higher than ").append(string.length())));
                }
                if (imp.BOOK.MAX_2BYTE_CHARS > 0) {
                    int n2 = 0;
                    for (int j = 0; j < string.length(); ++j) {
                        if (String.valueOf(string.charAt(j)).getBytes().length > 1 && ++n2 > imp.BOOK.MAX_2BYTE_CHARS) {
                            return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("Too many 2byte chars in page content. Allowed: ").append(imp.BOOK.MAX_2BYTE_CHARS).append(", current: ").append(n2)), false, true, String.valueOf(new StringBuilder().append("increase \"book.max-2byte-chars\" (must be bigger than ").append(++n2).append(") in settings.yml")));
                        }
                    }
                }
                if (string.replace(" ", "").startsWith("{\"translate\"")) {
                    final String[] mojang_CRASH_TRANSLATIONS = MojangCrashTranslations.MOJANG_CRASH_TRANSLATIONS;
                    for (int length = mojang_CRASH_TRANSLATIONS.length, k = 0; k < length; ++k) {
                        final String format = String.format("{\"translate\":\"%s\"}", mojang_CRASH_TRANSLATIONS[k]);
                        if (Integer.valueOf(format.toUpperCase().hashCode()).equals(string.toUpperCase().hashCode())) {
                            return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("Crash book! TranslationJson: ").append(format)), false, true);
                        }
                    }
                }
            }
        }
        if (Settings.IMP.OTHER_NBT.SIMPLE_NBT_LIMIT) {
            final String lowerCase = item.getName().toLowerCase();
            if (!lowerCase.contains("chest") && !lowerCase.contains("hopper") && !lowerCase.contains("shulker")) {
                final int length2 = String.valueOf(tag).getBytes(StandardCharsets.UTF_8).length;
                if (length2 > Settings.IMP.OTHER_NBT.MAX_SIMPLE_NBT_LIMIT) {
                    SpigotGuardLogger.log(Level.INFO, "{0} too big NBT data sent! ({1}) If it is false positive please change max-simple-nbt-limit in settings.yml to bigger value than player reached.", this.injector.getPlayer().getName(), length2);
                    return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("Too big NBT data! (").append(length2).append(")")), false, true, "increase \"other-nbt.max-simple-nbt-limit\" in settings.yml or set \"other-nbt.simple-nbt-limit\" to false");
                }
            }
        }
        if (tag.hasKey("SkullOwner")) {
            final NBTTagCompound compound = tag.getCompound("SkullOwner");
            if (compound.hasKey("Properties")) {
                final NBTTagCompound compound2 = compound.getCompound("Properties");
                if (compound2.hasKey("textures")) {
                    final NBTTagList list2 = compound2.getList("textures", 10);
                    for (int l = 0; l < list2.size(); ++l) {
                        final NBTTagCompound compound3 = list2.getCompound(l);
                        if (compound3.hasKey("Value")) {
                            final String string2 = compound3.getString("Value");
                            String s3;
                            try {
                                s3 = new String(Base64.getDecoder().decode(string2));
                            }
                            catch (IllegalArgumentException ex) {
                                break;
                            }
                            final String lowerCase2 = s3.trim().replace(" ", "").replace("\"", "").toLowerCase();
                            final Matcher matcher = PacketDecoder_1_15.URL_MATCHER.matcher(lowerCase2);
                            while (matcher.find()) {
                                final String substring = lowerCase2.substring(matcher.end() + 1);
                                if (!substring.startsWith("http://textures.minecraft.net") && !substring.startsWith("https://textures.minecraft.net") && Settings.IMP.SKULL_CHECKS.BLOCK_INVALID_HEAD_TEXTURE) {
                                    return new ExploitDetails(this.user, s, "Invalid head texture 2", false, true, "set \"skull-checks.invalid-head-texture\" to false");
                                }
                            }
                        }
                    }
                }
            }
        }
        int n3 = 0;
        for (final String s4 : keys) {
            if (tag.hasKeyOfType(s4, 9)) {
                if (++n3 > Settings.IMP.OTHER_NBT.MAX_LISTS) {
                    return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("too many NBTLists (").append(n3).append(")")), false, true, String.valueOf(new StringBuilder().append("set \"other-nbt.max-lists\" to value bigger than ").append(n3).append(" in settings.yml")));
                }
                final NBTTagList list3 = tag.getList(s4, 8);
                final int size = list3.size();
                if (size > imp.OTHER_NBT.MAX_LIST_SIZE) {
                    tag.remove(s4);
                    return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("too big NBTList (").append(size).append(")")), false, true, String.valueOf(new StringBuilder().append("set \"other-nbt.max-list-size\" to value higher than ").append(size).append(" in settings.yml")));
                }
                for (int n4 = 0; n4 < list3.size(); ++n4) {
                    final String string3 = list3.getString(n4);
                    if (string3 == null || string3.equals("null")) {
                        return new ExploitDetails(this.user, s, "null list content", false, true);
                    }
                    if (string3.length() > imp.OTHER_NBT.MAX_LIST_CONTENT) {
                        return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("too big list content (").append(string3.length()).append(")")), false, false, String.valueOf(new StringBuilder().append("set \"other-nbt.max-list-content\" to value higher than ").append(string3.length()).append(" in settings.yml")));
                    }
                }
            }
            if (tag.hasKeyOfType(s4, 11)) {
                final int[] intArray = tag.getIntArray(s4);
                if (intArray.length > imp.OTHER_NBT.MAX_ARRAY_SIZE) {
                    return new ExploitDetails(this.user, s, "too large int array", false, true, String.valueOf(new StringBuilder().append("set \"other-nbt.max-array-size\" to value higher than ").append(intArray.length).append(" in settings.yml")));
                }
                continue;
            }
        }
        return null;
    }
    
    static {
        URL_MATCHER = Pattern.compile("url");
        LIMITATION = SpigotGuardPlugin.getInstance().getPacketLimitation();
    }
    
    public PacketDecoder_1_15(final PacketInjector packetInjector, final User user) {
        super(packetInjector);
        this.disconnected = false;
        this.lastBookplace = -1L;
        this.user = user;
    }
    
    private void lambda$decode$2() {
        this.injector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
    }
    
    private void lambda$decode$1(final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$0);
    }
    
    private void lambda$decode$0(final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)this.injector.getPlayer(), exploitDetails));
    }
    
    private LimitablePacket hasReachedLimit(final String s) {
        if (!PacketDecoder_1_15.LIMITATION.isLimitable(s)) {
            return null;
        }
        final LimitablePacket limit = PacketDecoder_1_15.LIMITATION.getLimit(s);
        if (this.user.increaseAndGetReceivedPackets(s) < limit.getLimit()) {
            return null;
        }
        return limit;
    }
    
    public ExploitDetails failure(final Object o) {
        final Player player = this.injector.getPlayer();
        final String simpleName = o.getClass().getSimpleName();
        if (o.getClass().getSimpleName().equals("PacketPlayInWindowClick")) {
            if (Settings.IMP.DEBUG) {
                SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
            }
            final PacketPlayInWindowClick packetPlayInWindowClick = (PacketPlayInWindowClick)o;
            try {
                ItemStack f = ((PacketPlayInWindowClick)o).f();
                if (f == null) {
                    try {
                        final Field declaredField = PacketPlayInWindowClick.class.getDeclaredField("item");
                        declaredField.setAccessible(true);
                        f = (ItemStack)declaredField.get(o);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (f != null) {
                    final ExploitDetails checkNbtTags = this.checkNbtTags(simpleName, f);
                    if (checkNbtTags != null) {
                        return checkNbtTags;
                    }
                }
                final int b = packetPlayInWindowClick.b();
                if (b > 127 || b < -999) {
                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Invalid slot ").append(b)), false, true, "can't be fixed, it's surely exploit");
                }
                final InventoryView openInventory = player.getOpenInventory();
                if (openInventory != null) {
                    final Inventory topInventory = openInventory.getTopInventory();
                    final Inventory bottomInventory = openInventory.getBottomInventory();
                    int n = 127;
                    if (topInventory.getType() == InventoryType.CRAFTING && bottomInventory.getType() == InventoryType.PLAYER) {
                        n += 4;
                    }
                    if (b >= n && PacketDecoder_1_15.LIMITATION != null) {
                        final LimitablePacket hasReachedLimit = this.hasReachedLimit("PacketPlayInWindowClick_InvalidSlot");
                        if (hasReachedLimit != null) {
                            return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("invalid slot, slot: ").append(b).append("")), hasReachedLimit.isCancelOnly(), false, "increase packet limit of PacketPlayInWindowClick_InvalidSlot in config.yml");
                        }
                    }
                    if (f != null) {
                        final org.bukkit.inventory.ItemStack bukkitCopy = CraftItemStack.asBukkitCopy(f);
                        if ((bukkitCopy.getType() == Material.CHEST || bukkitCopy.getType() == Material.HOPPER) && bukkitCopy.hasItemMeta() && bukkitCopy.getItemMeta().toString().getBytes().length > 262144) {
                            return new ExploitDetails(this.user, simpleName, "too big chest data", false, false);
                        }
                    }
                }
            }
            catch (Exception ex4) {}
        }
        else if (o.getClass().getSimpleName().equals("PacketPlayInBlockPlace")) {
            final org.bukkit.inventory.ItemStack itemInHand = player.getInventory().getItemInHand();
            if (itemInHand != null && itemInHand.getType().name().contains("BOOK")) {
                this.lastBookplace = System.currentTimeMillis();
            }
        }
        else if (o.getClass().getSimpleName().equals("PacketPlayInSetCreativeSlot")) {
            if (Settings.IMP.DEBUG) {
                SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
            }
            if (player.getGameMode() != GameMode.CREATIVE && !Settings.IMP.ALLOW_CREATIVE_INVENTORY_CLICK_WITHOUT_GAMEMODE) {
                return new ExploitDetails(this.user, simpleName, "clicking in creative inventory without gamemode 1", false, true, "set \"allow-creative-inventory-click-without-gamemode\" to true in settings.yml");
            }
            ItemStack itemStack = null;
            try {
                final Field declaredField2 = PacketPlayInSetCreativeSlot.class.getDeclaredField("b");
                declaredField2.setAccessible(true);
                itemStack = (ItemStack)declaredField2.get(o);
            }
            catch (Exception ex2) {
                ex2.printStackTrace();
            }
            if (itemStack != null) {
                final ExploitDetails checkNbtTags2 = this.checkNbtTags(simpleName, itemStack);
                if (checkNbtTags2 != null) {
                    return checkNbtTags2;
                }
                final org.bukkit.inventory.ItemStack bukkitCopy2 = CraftItemStack.asBukkitCopy(itemStack);
                if ((bukkitCopy2.getType() == Material.CHEST || bukkitCopy2.getType() == Material.HOPPER) && bukkitCopy2.hasItemMeta() && bukkitCopy2.getItemMeta().toString().getBytes().length > 262144) {
                    return new ExploitDetails(this.user, simpleName, "too big chest data", false, false);
                }
            }
        }
        else if (o.getClass().getSimpleName().equals("PacketPlayInCustomPayload")) {
            final PacketPlayInCustomPayload packetPlayInCustomPayload = (PacketPlayInCustomPayload)o;
            final String string = packetPlayInCustomPayload.tag.toString();
            final PacketDataSerializer data = packetPlayInCustomPayload.data;
            if (data.capacity() > Settings.IMP.PAYLOAD_SETTINGS.MAX_CAPACITY) {
                return new ExploitDetails(this.user, simpleName, "invalid bytebuf capacity", false, true, "set \"payload-settings.max-capacity\" to higher value in settings.yml");
            }
            Label_1320: {
                if (string.equals("MC|BEdit") || string.equals("MC|BSign") || string.equals("minecraft:bedit") || string.equals("minecraft:bsign")) {
                    if (Settings.IMP.DEBUG) {
                        SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
                    }
                    try {
                        final ItemStack m = data.m();
                        if (m != null) {
                            if (System.currentTimeMillis() - this.lastBookplace > 60000L && Settings.IMP.PAYLOAD_SETTINGS.BLOCK_BOOK_SIGNING_WHEN_NO_BOOK_PLACED) {
                                return new ExploitDetails(this.user, simpleName, "book sign, but no book used", false, false, "set \"payload-settings.block-book-signing-when-no-book-placed\" in settings.yml to false");
                            }
                            if (Settings.IMP.PAYLOAD_SETTINGS.FAIL_WHEN_NOT_HOLDING_BOOK && !player.getInventory().contains(Material.valueOf("BOOK_AND_QUILL")) && !player.getInventory().contains(Material.WRITTEN_BOOK)) {
                                return new ExploitDetails(this.user, simpleName, "book interact, but no book exists in player's inventory", false, true, "set \"payload-settings.fail-when-not-holding-book\" in settings.yml to false");
                            }
                            final ExploitDetails checkNbtTags3 = this.checkNbtTags(simpleName, m);
                            if (checkNbtTags3 != null) {
                                return checkNbtTags3;
                            }
                        }
                        break Label_1320;
                    }
                    catch (Exception ex3) {
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("exception: ").append(ex3.getMessage())), false, false);
                    }
                }
                if (string.equals("REGISTER") || Integer.valueOf(1321107516).equals(string.toUpperCase().hashCode()) || string.toLowerCase().contains("fml")) {
                    ByteBuf copy = null;
                    try {
                        copy = data.copy();
                        if (copy.toString(StandardCharsets.UTF_8).split("\u0000").length > 124) {
                            return new ExploitDetails(this.user, simpleName, "too many channels", false, false);
                        }
                        if (copy != null) {
                            copy.release();
                        }
                    }
                    catch (Exception ex5) {
                        if (copy != null) {
                            copy.release();
                        }
                    }
                    finally {
                        if (copy != null) {
                            copy.release();
                        }
                    }
                }
                else if (string.equals("MC|ItemName") && player.getInventory() != null && player.getOpenInventory().getType() != InventoryType.ANVIL && Settings.IMP.BLOCK_ITEMNAME_WHEN_NO_ANVIL) {
                    return new ExploitDetails(this.user, simpleName, "trying to use MC|ItemName but no anvil exists", false, false, "set \"block-itemname-when-no-anvil\" in settings.yml to false");
                }
            }
        }
        else if (o instanceof PacketPlayInFlying.PacketPlayInPosition) {
            final PacketPlayInFlying.PacketPlayInPosition packetPlayInPosition = (PacketPlayInFlying.PacketPlayInPosition)o;
            final double n2 = packetPlayInPosition.a(0.0f);
            final double n3 = packetPlayInPosition.b(0.0f);
            final double c = packetPlayInPosition.c(0.0);
            if ((n2 >= Double.MAX_VALUE || n3 > Double.MAX_VALUE || c >= Double.MAX_VALUE) && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V2) {
                return new ExploitDetails(this.user, simpleName, "Double.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v2\" to true");
            }
            if (n2 >= 2.147483647E9 || n3 > 2.147483647E9 || (c >= 2.147483647E9 && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V1)) {
                return new ExploitDetails(this.user, simpleName, "Integer.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v1\" to true");
            }
            if (Settings.IMP.POSITION_CHECKS.CHECK_CHUNK_CRASHER) {
                final double n4 = Math.max(player.getLocation().getX(), n2) - Math.min(player.getLocation().getX(), n2);
                if (n4 >= 10.0 && n4 % 1.0 == 0.0) {
                    SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Received invalid location packet from: ").append(player.getName()).append(" (").append(player.getLocation().getX()).append(",").append(player.getLocation().getY()).append(",").append(player.getLocation().getZ()).append(" ||| Packet: ").append(n2).append(",").append(n3).append(",").append(c).append("), difference: ").append(n4)), new Object[0]);
                    if (this.hasReachedLimit("PacketPlayInPosition_Invalid_v1") != null) {
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Chunk crasher (v1, invalid x coords, diff: ").append(n4).append(")")), false, false, "set position-checks.check-chunk-crasher to false in settings.yml or increase PacketPlayInPosition_Invalid_v1.limit in config.yml");
                    }
                }
                final double n5 = Math.max(player.getLocation().getZ(), c) - Math.min(player.getLocation().getZ(), c);
                if (n5 >= 10.0 && n5 % 1.0 == 0.0) {
                    SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Received invalid location packet from: ").append(player.getName()).append(" (").append(player.getLocation().getX()).append(",").append(player.getLocation().getY()).append(",").append(player.getLocation().getZ()).append(" ||| Packet: ").append(n2).append(",").append(n3).append(",").append(c).append("), difference: ").append(n5)), new Object[0]);
                    if (this.hasReachedLimit("PacketPlayInPosition_Invalid_v1") != null) {
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Chunk crasher (v1, invalid z coords, diff: ").append(n5).append(")")), false, false, "set position-checks.check-chunk-crasher to false in settings.yml or increase PacketPlayInPosition_Invalid_v1.limit in config.yml");
                    }
                }
                if (Settings.IMP.POSITION_CHECKS.CHECK_FLY_CRASHER && n3 == player.getLocation().getY() + 0.1) {
                    final double n6 = Math.max(player.getLocation().getY(), n3) - Math.min(player.getLocation().getY(), n3);
                    SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Received invalid Y location packet from: ").append(player.getName()).append(" (").append(player.getLocation().getX()).append(",").append(player.getLocation().getY()).append(",").append(player.getLocation().getZ()).append(" ||| Packet: ").append(n2).append(",").append(n3).append(",").append(c).append("), difference: ").append(n6)), new Object[0]);
                    if (this.hasReachedLimit("PacketPlayInPosition_Invalid_v2") != null) {
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Fly crasher (v1, invalid y coords, diff: ").append(n6).append(")")), false, false, "set position-checks.check-fly-crasher to false in settings.yml or increase PacketPlayInPosition_Invalid_v2.limit in config.yml");
                    }
                }
            }
        }
        else if (o instanceof PacketPlayInFlying) {
            final PacketPlayInFlying packetPlayInFlying = (PacketPlayInFlying)o;
            final double a = packetPlayInFlying.a(0.0);
            final double b2 = packetPlayInFlying.b(0.0);
            final double c2 = packetPlayInFlying.c(0.0);
            if ((a >= Double.MAX_VALUE || b2 > Double.MAX_VALUE || c2 >= Double.MAX_VALUE) && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V2) {
                return new ExploitDetails(this.user, simpleName, "Double.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v2\" to true");
            }
            if (a >= 2.147483647E9 || b2 > 2.147483647E9 || (c2 >= 2.147483647E9 && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V1)) {
                return new ExploitDetails(this.user, simpleName, "Integer.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v1\" to true");
            }
            final float a2 = packetPlayInFlying.a(0.0f);
            final float b3 = packetPlayInFlying.b(0.0f);
            if (a2 == Float.NEGATIVE_INFINITY || b3 == Float.NEGATIVE_INFINITY || a2 >= Float.MAX_VALUE || b3 >= Float.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "invalid float position", false, true);
            }
            if (Settings.IMP.POSITION_CHECKS.CHECK_YAW_CRASHER && (a2 < -30000.0f || b3 < -30000.0f || a2 > 30000.0f || b3 > 30000.0f || a2 == 9.223372E18 || b3 == 9.223372E18 || a2 == 9.223372E18f || b3 == 9.223372E18f)) {
                return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Invalid yaw/pitch v3 (yaw: ").append(a2).append(", pitch: ").append(b3).append(")")), false, false, "set position-checks.check-yaw-crasher to false in settings.yml report to staff on mc-protection.eu/discord");
            }
        }
        else if (o instanceof PacketPlayInHeldItemSlot) {
            final int b4 = ((PacketPlayInHeldItemSlot)o).b();
            if (b4 >= 36 || b4 < 0) {
                return new ExploitDetails(this.user, simpleName, "invalid held item slot", false, true);
            }
        }
        else if (o.getClass().getSimpleName().equals("PacketPlayInSteerVehicle")) {
            final PacketPlayInSteerVehicle packetPlayInSteerVehicle = (PacketPlayInSteerVehicle)o;
            if (packetPlayInSteerVehicle.b() >= Float.MAX_VALUE || packetPlayInSteerVehicle.c() >= Float.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "invalid vehicle movement", false, true);
            }
        }
        else if (o.getClass().getSimpleName().equals("PacketPlayInVehicleMove")) {
            final PacketPlayInVehicleMove packetPlayInVehicleMove = (PacketPlayInVehicleMove)o;
            if (packetPlayInVehicleMove.getX() >= Double.MAX_VALUE || packetPlayInVehicleMove.getY() >= Double.MAX_VALUE || packetPlayInVehicleMove.getZ() >= Double.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "invalid vehicle movement v2", false, true);
            }
            if (player.getLocation().distance(new Location(player.getWorld(), packetPlayInVehicleMove.getX(), packetPlayInVehicleMove.getY(), packetPlayInVehicleMove.getZ())) > 30.0 && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_VEHICLE_MOVEMENT_V3) {
                return new ExploitDetails(this.user, simpleName, "invalid vehicle movement v3", false, true, "set position-checks.allow-invalid-vehicle-movement-v3 to true");
            }
            if (player.getLocation().distance(new Location(player.getWorld(), packetPlayInVehicleMove.getX(), packetPlayInVehicleMove.getY(), packetPlayInVehicleMove.getZ())) > 3.0 && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_VEHICLE_MOVEMENT_V4 && this.hasReachedLimit("PacketPlayInVehicleMove_Invalid_v4") != null) {
                return new ExploitDetails(this.user, simpleName, "invalid vehicle movement v4", false, true, "set position-checks.allow-invalid-vehicle-movement-v4 to true");
            }
        }
        else if (o.getClass().getSimpleName().equals("PacketPlayInUseItem")) {
            final BlockPosition blockPosition = ((PacketPlayInUseItem)o).c().getBlockPosition();
            final int max_DISTANCE_BETWEEN_PLAYER_AND_BLOCK_POSITION = Settings.IMP.POSITION_CHECKS.MAX_DISTANCE_BETWEEN_PLAYER_AND_BLOCK_POSITION;
            if (max_DISTANCE_BETWEEN_PLAYER_AND_BLOCK_POSITION > 0 && new Location(player.getWorld(), (double)blockPosition.getX(), (double)blockPosition.getY(), (double)blockPosition.getZ()).distance(player.getLocation()) > max_DISTANCE_BETWEEN_PLAYER_AND_BLOCK_POSITION) {
                return new ExploitDetails(this.user, simpleName, "invalid UseItem blockPosition", false, true, "set position-checks.max-distance-between-player-and-block-position to -1");
            }
        }
        final LimitablePacket hasReachedLimit2 = this.hasReachedLimit(simpleName);
        if (hasReachedLimit2 != null) {
            return new ExploitDetails(this.user, simpleName, "packet limit exceed", hasReachedLimit2.isCancelOnly(), false);
        }
        return null;
    }
}
