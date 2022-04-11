package xyz.yooniks.spigotguard.network.v1_7_R4;

import net.minecraft.util.io.netty.handler.codec.*;
import xyz.yooniks.spigotguard.user.*;
import xyz.yooniks.spigotguard.network.*;
import xyz.yooniks.spigotguard.config.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import java.util.regex.*;
import xyz.yooniks.spigotguard.limitation.*;
import xyz.yooniks.spigotguard.helper.*;
import net.minecraft.util.io.netty.util.concurrent.*;
import xyz.yooniks.spigotguard.*;
import org.bukkit.plugin.*;
import org.bukkit.event.inventory.*;
import org.bukkit.craftbukkit.v1_7_R4.inventory.*;
import java.nio.charset.*;
import net.minecraft.server.v1_7_R4.*;
import org.bukkit.entity.*;
import java.lang.reflect.*;
import org.bukkit.inventory.*;
import net.minecraft.util.io.netty.buffer.*;
import xyz.yooniks.spigotguard.event.*;
import org.bukkit.*;
import org.bukkit.event.*;
import net.minecraft.util.io.netty.channel.*;
import java.util.*;

@ChannelHandler.Sharable
public class PacketDecoder_1_7 extends MessageToMessageDecoder<Object>
{
    private static final Pattern URL_MATCHER;
    private boolean disconnected;
    private long lastBookplace;
    private static final String BLOCKED;
    private final User user;
    private static final PacketLimitation LIMITATION;
    private final PacketInjector injector;
    
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
        if (Settings.IMP.DEBUG) {
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(s).append(" -> ").append(item.getName()).append(" -> ").append(tag.toString())), new Object[0]);
        }
        final Set c = tag.c();
        final Settings imp = Settings.IMP;
        if (c.size() > imp.OTHER_NBT.MAX_KEYS) {
            return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("too many keys (").append(c.size()).append(")")), false, true, "set \"other-nbt.max-keys\" in settings.yml to higher value");
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
                if (string.contains("wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5") || Integer.valueOf(1579238827).equals(string.toUpperCase().hashCode())) {
                    itemStack.setTag(new NBTTagCompound());
                    return new ExploitDetails(this.user, s, "crash client detected (invalid book page data)", false, true);
                }
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
            if (tag.hasKey("SkullOwner")) {
                final NBTTagCompound compound = tag.getCompound("SkullOwner");
                if (compound.hasKey("Properties")) {
                    final NBTTagCompound compound2 = compound.getCompound("Properties");
                    if (compound2.hasKey("textures")) {
                        final NBTTagList list2 = compound2.getList("textures", 10);
                        for (int l = 0; l < list2.size(); ++l) {
                            final NBTTagCompound value = list2.get(l);
                            if (value.hasKey("Value")) {
                                final String string2 = value.getString("Value");
                                String s3;
                                try {
                                    s3 = new String(Base64.getDecoder().decode(string2));
                                }
                                catch (IllegalArgumentException ex) {
                                    break;
                                }
                                final String lowerCase = s3.trim().replace(" ", "").replace("\"", "").toLowerCase();
                                final Matcher matcher = PacketDecoder_1_7.URL_MATCHER.matcher(lowerCase);
                                while (matcher.find()) {
                                    final String substring = lowerCase.substring(matcher.end() + 1);
                                    if (!substring.startsWith("http://textures.minecraft.net") && !substring.startsWith("https://textures.minecraft.net") && Settings.IMP.SKULL_CHECKS.BLOCK_INVALID_HEAD_TEXTURE) {
                                        return new ExploitDetails(this.user, s, "Invalid head texture 2", false, true, "set \"skull-checks.invalid-head-texture\" to false");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        int n3 = 0;
        for (final String s4 : c) {
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
    
    private LimitablePacket hasReachedLimit(final String s) {
        if (!PacketDecoder_1_7.LIMITATION.isLimitable(s)) {
            return null;
        }
        final LimitablePacket limit = PacketDecoder_1_7.LIMITATION.getLimit(s);
        if (this.user.increaseAndGetReceivedPackets(s) < limit.getLimit()) {
            return null;
        }
        return limit;
    }
    
    public PacketDecoder_1_7(final PacketInjector injector, final User user) {
        this.disconnected = false;
        this.lastBookplace = -1L;
        this.injector = injector;
        this.user = user;
    }
    
    private void lambda$decode$2() {
        this.injector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
    }
    
    public User getUser() {
        return this.user;
    }
    
    private void lambda$decode$1(final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$0);
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
                ItemStack g = ((PacketPlayInWindowClick)o).g();
                if (g == null) {
                    try {
                        final Field declaredField = PacketPlayInWindowClick.class.getDeclaredField("item");
                        declaredField.setAccessible(true);
                        g = (ItemStack)declaredField.get(o);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (g != null) {
                    final ExploitDetails checkNbtTags = this.checkNbtTags(simpleName, g);
                    if (checkNbtTags != null) {
                        return checkNbtTags;
                    }
                }
                final InventoryView openInventory = player.getOpenInventory();
                if (openInventory != null) {
                    final Inventory topInventory = openInventory.getTopInventory();
                    final Inventory bottomInventory = openInventory.getBottomInventory();
                    final int c = packetPlayInWindowClick.c();
                    int countSlots = openInventory.countSlots();
                    if (topInventory.getType() == InventoryType.CRAFTING && bottomInventory.getType() == InventoryType.PLAYER) {
                        countSlots += 4;
                    }
                    if (c >= countSlots && PacketDecoder_1_7.LIMITATION != null) {
                        final LimitablePacket hasReachedLimit = this.hasReachedLimit("PacketPlayInWindowClick_InvalidSlot");
                        if (hasReachedLimit != null) {
                            return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("invalid slot, slot: ").append(c).append("")), hasReachedLimit.isCancelOnly(), false, "increase packet limit of PacketPlayInWindowClick_InvalidSlot in config.yml");
                        }
                    }
                    if (g != null) {
                        final org.bukkit.inventory.ItemStack item = openInventory.getItem(c);
                        final Item item2 = g.getItem();
                        if (item2 instanceof ItemWrittenBook || item2 instanceof ItemBookAndQuill) {
                            g.getTag().remove("pages");
                            g.getTag().remove("author");
                            g.getTag().remove("title");
                            if (item == null || (item.getType() != Material.valueOf("BOOK_AND_QUILL") && item.getType() != Material.WRITTEN_BOOK)) {
                                return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("tried to use book but real item is ").append((item == null) ? "null" : item.getType().name())), false, true);
                            }
                        }
                        if ((item2 instanceof ItemFireworks || item2 instanceof ItemFireworksCharge) && (item == null || (item.getType() != Material.valueOf("FIREWORK") && item.getType() != Material.valueOf("FIREWORK_CHARGE")))) {
                            return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("tried to use firework but real item is ").append((item == null) ? "null" : item.getType().name())), false, true);
                        }
                    }
                    if (g != null) {
                        final org.bukkit.inventory.ItemStack bukkitCopy = CraftItemStack.asBukkitCopy(g);
                        if ((bukkitCopy.getType() == Material.CHEST || bukkitCopy.getType() == Material.HOPPER) && bukkitCopy.hasItemMeta() && bukkitCopy.getItemMeta().toString().getBytes().length > 262144) {
                            return new ExploitDetails(this.user, simpleName, "too big chest data", false, false);
                        }
                    }
                }
            }
            catch (Exception ex5) {}
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
        else if (o.getClass().getSimpleName().equals("PacketPlayInBlockPlace")) {
            if (Settings.IMP.DEBUG) {
                SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
            }
            ItemStack itemStack2 = ((PacketPlayInBlockPlace)o).getItemStack();
            if (itemStack2 == null) {
                try {
                    final Field declaredField3 = PacketPlayInBlockPlace.class.getDeclaredField("e");
                    declaredField3.setAccessible(true);
                    itemStack2 = (ItemStack)declaredField3.get(o);
                }
                catch (Exception ex3) {
                    ex3.printStackTrace();
                }
            }
            if (itemStack2 != null) {
                final Item item3 = itemStack2.getItem();
                if (item3 instanceof ItemWrittenBook || item3 instanceof ItemBookAndQuill) {
                    this.lastBookplace = System.currentTimeMillis();
                    final org.bukkit.inventory.ItemStack itemInHand = player.getItemInHand();
                    if (itemInHand == null || (itemInHand.getType() != Material.WRITTEN_BOOK && itemInHand.getType() != Material.valueOf("BOOK_AND_QUILL"))) {
                        return new ExploitDetails(this.user, simpleName, "placing book but not holding it", false, true);
                    }
                }
                final ExploitDetails checkNbtTags3 = this.checkNbtTags(simpleName, itemStack2);
                if (checkNbtTags3 != null) {
                    return checkNbtTags3;
                }
                final org.bukkit.inventory.ItemStack bukkitCopy3 = CraftItemStack.asBukkitCopy(itemStack2);
                if ((bukkitCopy3.getType() == Material.CHEST || bukkitCopy3.getType() == Material.HOPPER) && bukkitCopy3.hasItemMeta() && bukkitCopy3.getItemMeta().toString().getBytes().length > 262144) {
                    return new ExploitDetails(this.user, simpleName, "Too big chest data", false, false);
                }
            }
        }
        else if (o.getClass().getSimpleName().equals("PacketPlayInCustomPayload")) {
            final PacketPlayInCustomPayload packetPlayInCustomPayload = (PacketPlayInCustomPayload)o;
            final String c2 = packetPlayInCustomPayload.c();
            if (c2 != null && packetPlayInCustomPayload.e() != null) {
                try {
                    final ByteBuf wrappedBuffer = Unpooled.wrappedBuffer(packetPlayInCustomPayload.e());
                    if (wrappedBuffer.capacity() > Settings.IMP.PAYLOAD_SETTINGS.MAX_CAPACITY) {
                        return new ExploitDetails(this.user, simpleName, "invalid bytebuf capacity", false, true, "set \"payload-settings.max-capacity\" to higher value in settings.yml");
                    }
                    Label_1758: {
                        if (c2.equals("MC|BEdit") || c2.equals("MC|BSign") || c2.equals("minecraft:bedit") || c2.equals("minecraft:bsign")) {
                            if (Settings.IMP.DEBUG) {
                                SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
                            }
                            try {
                                final ItemStack c3 = new PacketDataSerializer(wrappedBuffer).c();
                                if (c3 != null) {
                                    if (System.currentTimeMillis() - this.lastBookplace > 60000L) {
                                        return new ExploitDetails(this.user, simpleName, "book sign, but no book used", false, false);
                                    }
                                    if (Settings.IMP.PAYLOAD_SETTINGS.FAIL_WHEN_NOT_HOLDING_BOOK && !player.getInventory().contains(Material.valueOf("BOOK_AND_QUILL")) && !player.getInventory().contains(Material.WRITTEN_BOOK)) {
                                        return new ExploitDetails(this.user, simpleName, "book interact, but no book exists", false, true);
                                    }
                                    final ExploitDetails checkNbtTags4 = this.checkNbtTags(simpleName, c3);
                                    if (checkNbtTags4 != null) {
                                        return checkNbtTags4;
                                    }
                                }
                                break Label_1758;
                            }
                            catch (Exception ex4) {
                                return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("exception: ").append(ex4.getMessage())), false, false);
                            }
                        }
                        if (c2.equals("REGISTER") || Integer.valueOf(1321107516).equals(c2.toUpperCase().hashCode()) || c2.toLowerCase().contains("fml")) {
                            ByteBuf copy = null;
                            try {
                                copy = wrappedBuffer.copy();
                                if (copy.toString(StandardCharsets.UTF_8).split("\u0000").length > 124) {
                                    return new ExploitDetails(this.user, simpleName, "too many channels", false, false);
                                }
                                if (copy != null) {
                                    copy.release();
                                }
                            }
                            catch (Exception ex6) {
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
                        else if (c2.equals("MC|ItemName") && player.getInventory() != null && player.getOpenInventory().getType() != InventoryType.ANVIL && Settings.IMP.BLOCK_ITEMNAME_WHEN_NO_ANVIL) {
                            return new ExploitDetails(this.user, simpleName, "trying to use MC|ItemName but no anvil exists", false, false, "set \"block-itemname-when-no-anvil\" in settings.yml to false");
                        }
                    }
                }
                catch (NullPointerException ex7) {}
            }
        }
        else if (o instanceof PacketPlayInPosition) {
            final PacketPlayInPosition packetPlayInPosition = (PacketPlayInPosition)o;
            final double c4 = packetPlayInPosition.c();
            final double d = packetPlayInPosition.d();
            final double e = packetPlayInPosition.e();
            if ((c4 >= Double.MAX_VALUE || d > Double.MAX_VALUE || e >= Double.MAX_VALUE) && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V2) {
                return new ExploitDetails(this.user, simpleName, "Double.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v2\" to true");
            }
            if (c4 >= 2.147483647E9 || d > 2.147483647E9 || (e >= 2.147483647E9 && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V1)) {
                return new ExploitDetails(this.user, simpleName, "Integer.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v1\" to true");
            }
            if (Settings.IMP.POSITION_CHECKS.CHECK_CHUNK_CRASHER) {
                final double n = Math.max(player.getLocation().getX(), c4) - Math.min(player.getLocation().getX(), c4);
                if (n >= 10.0 && n % 1.0 == 0.0) {
                    SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Received invalid location packet from: ").append(player.getName()).append(" (").append(player.getLocation().getX()).append(",").append(player.getLocation().getY()).append(",").append(player.getLocation().getZ()).append(" ||| Packet: ").append(c4).append(",").append(d).append(",").append(e).append("), difference: ").append(n)), new Object[0]);
                    if (this.hasReachedLimit("PacketPlayInPosition_Invalid_v1") != null) {
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Chunk crasher (v1, invalid x coords, diff: ").append(n).append(")")), false, false, "set position-checks.check-chunk-crasher to false in settings.yml or increase PacketPlayInPosition_Invalid_v1.limit in config.yml");
                    }
                }
                final double n2 = Math.max(player.getLocation().getZ(), e) - Math.min(player.getLocation().getZ(), e);
                if (n2 >= 10.0 && n2 % 1.0 == 0.0) {
                    SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Received invalid location packet from: ").append(player.getName()).append(" (").append(player.getLocation().getX()).append(",").append(player.getLocation().getY()).append(",").append(player.getLocation().getZ()).append(" ||| Packet: ").append(c4).append(",").append(d).append(",").append(e).append("), difference: ").append(n2)), new Object[0]);
                    if (this.hasReachedLimit("PacketPlayInPosition_Invalid_v1") != null) {
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Chunk crasher (v1, invalid z coords, diff: ").append(n2).append(")")), false, false, "set position-checks.check-chunk-crasher to false in settings.yml or increase PacketPlayInPosition_Invalid_v1.limit in config.yml");
                    }
                }
                if (Settings.IMP.POSITION_CHECKS.CHECK_FLY_CRASHER && d == player.getLocation().getY() + 0.1) {
                    final double n3 = Math.max(player.getLocation().getY(), d) - Math.min(player.getLocation().getY(), d);
                    SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Received invalid Y location packet from: ").append(player.getName()).append(" (").append(player.getLocation().getX()).append(",").append(player.getLocation().getY()).append(",").append(player.getLocation().getZ()).append(" ||| Packet: ").append(c4).append(",").append(d).append(",").append(e).append("), difference: ").append(n3)), new Object[0]);
                    if (this.hasReachedLimit("PacketPlayInPosition_Invalid_v2") != null) {
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Fly crasher (v1, invalid y coords, diff: ").append(n3).append(")")), false, false, "set position-checks.check-fly-crasher to false in settings.yml or increase PacketPlayInPosition_Invalid_v2.limit in config.yml");
                    }
                }
            }
        }
        else if (o.getClass().getSimpleName().equals("PacketPlayInFlying")) {
            final PacketPlayInFlying packetPlayInFlying = (PacketPlayInFlying)o;
            final double c5 = packetPlayInFlying.c();
            final double d2 = packetPlayInFlying.d();
            final double e2 = packetPlayInFlying.e();
            if (c5 >= Double.MAX_VALUE || d2 > Double.MAX_VALUE || e2 >= Double.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "Double.MAX_VALUE position", false, true);
            }
            final float g2 = packetPlayInFlying.g();
            final float h = packetPlayInFlying.h();
            if (g2 == Float.NEGATIVE_INFINITY || h == Float.NEGATIVE_INFINITY || g2 >= Float.MAX_VALUE || h >= Float.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "invalid float position", false, true);
            }
        }
        else if (o instanceof PacketPlayInHeldItemSlot) {
            final int c6 = ((PacketPlayInHeldItemSlot)o).c();
            if (c6 >= 36 || c6 < 0) {
                return new ExploitDetails(this.user, simpleName, "invalid held item slot", false, true);
            }
        }
        else if (o.getClass().getSimpleName().equals("PacketPlayInSteerVehicle")) {
            final PacketPlayInSteerVehicle packetPlayInSteerVehicle = (PacketPlayInSteerVehicle)o;
            if (packetPlayInSteerVehicle.c() >= Float.MAX_VALUE || packetPlayInSteerVehicle.d() >= Float.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "invalid vehicle movement", false, true);
            }
        }
        final LimitablePacket hasReachedLimit2 = this.hasReachedLimit(simpleName);
        if (hasReachedLimit2 != null) {
            return new ExploitDetails(this.user, simpleName, "packet limit exceed", hasReachedLimit2.isCancelOnly(), false, String.valueOf(new StringBuilder().append("increase packet limit of ").append(simpleName).append(" in config.yml or completely remove it")));
        }
        return null;
    }
    
    private void lambda$decode$0(final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)this.injector.getPlayer(), exploitDetails));
    }
    
    static {
        BLOCKED = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
        URL_MATCHER = Pattern.compile("url");
        LIMITATION = SpigotGuardPlugin.getInstance().getPacketLimitation();
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
        SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(SpigotGuardPlugin.getInstance().getSpigotGuardClassLoaded().getSerializable()).append(" Player ").append(this.injector.getPlayer().getName()).append(" tried to crash the server, packet: ").append(o.getClass().getSimpleName()).append(", details: ").append(failure.getDetails()).append(", how to fix if false positive: ").append(failure.getHowToFixFalseDetect()).append(")")), new Object[0]);
    }
}
