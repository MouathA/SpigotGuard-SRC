package xyz.yooniks.spigotguard.network.v1_12_R1;

import xyz.yooniks.spigotguard.user.*;
import xyz.yooniks.spigotguard.config.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import java.nio.charset.*;
import java.util.regex.*;
import xyz.yooniks.spigotguard.helper.*;
import org.bukkit.event.inventory.*;
import org.bukkit.craftbukkit.v1_12_R1.inventory.*;
import org.bukkit.entity.*;
import java.lang.reflect.*;
import org.bukkit.inventory.*;
import xyz.yooniks.spigotguard.limitation.*;
import net.minecraft.server.v1_12_R1.*;
import io.netty.buffer.*;
import xyz.yooniks.spigotguard.*;
import xyz.yooniks.spigotguard.event.*;
import org.bukkit.*;
import org.bukkit.event.*;
import xyz.yooniks.spigotguard.network.*;
import io.netty.channel.*;
import java.util.*;
import org.bukkit.plugin.*;
import io.netty.util.concurrent.*;

@ChannelHandler.Sharable
public class PacketDecoder_1_12 extends PacketDecoder
{
    private final User user;
    private long autoRecipeTime;
    private boolean disconnected;
    private long lastBookplace;
    private static final Pattern URL_MATCHER;
    private int autoRecipeCount;
    private static final PacketLimitation LIMITATION;
    
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
                if (string.length() > imp.BOOK.MAX_PAGE_SIZE) {
                    return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("too large page content (").append(string.length()).append(")")), false, false, String.valueOf(new StringBuilder().append("increase value of \"book.max-page-size\" (bigger than: ").append(string.length()).append(") in settings.yml")));
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
                            final String lowerCase2 = s3.trim().replace(" ", "").replace("\"", "").toLowerCase();
                            final Matcher matcher = PacketDecoder_1_12.URL_MATCHER.matcher(lowerCase2);
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
    
    private void lambda$decode$2() {
        this.injector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
    }
    
    public ExploitDetails failure(final Object o) {
        final Player player = this.injector.getPlayer();
        final String simpleName = o.getClass().getSimpleName();
        if (o instanceof PacketPlayInAutoRecipe) {
            if (this.autoRecipeTime < System.currentTimeMillis()) {
                this.autoRecipeCount = 0;
                this.autoRecipeTime = System.currentTimeMillis() + 200L;
            }
            if (this.autoRecipeTime > System.currentTimeMillis()) {
                final int max_AUTO_RECIPE_RATE = Settings.IMP.PACKET_DECODER.MAX_AUTO_RECIPE_RATE;
                if (max_AUTO_RECIPE_RATE != -1 && ++this.autoRecipeCount > max_AUTO_RECIPE_RATE) {
                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Sent more than ").append(this.autoRecipeCount).append(" AutoRecipe packets. (1.12+ crasher)")), false, true, "Increase max-auto-recipe-rate in settings.yml or set it to -1.");
                }
            }
        }
        if (o.getClass().getSimpleName().equals("PacketPlayInWindowClick")) {
            if (Settings.IMP.DEBUG) {
                SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
            }
            final PacketPlayInWindowClick packetPlayInWindowClick = (PacketPlayInWindowClick)o;
            try {
                ItemStack e = ((PacketPlayInWindowClick)o).e();
                if (e == null) {
                    try {
                        final Field declaredField = PacketPlayInWindowClick.class.getDeclaredField("item");
                        declaredField.setAccessible(true);
                        e = (ItemStack)declaredField.get(o);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (e != null) {
                    final ExploitDetails checkNbtTags = this.checkNbtTags(simpleName, e);
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
                    if (b >= n && PacketDecoder_1_12.LIMITATION != null) {
                        final LimitablePacket hasReachedLimit = this.hasReachedLimit("PacketPlayInWindowClick_InvalidSlot");
                        if (hasReachedLimit != null) {
                            return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("invalid slot, slot: ").append(b).append("")), hasReachedLimit.isCancelOnly(), false, "increase packet limit of PacketPlayInWindowClick_InvalidSlot in config.yml");
                        }
                    }
                    if (e != null) {
                        final org.bukkit.inventory.ItemStack bukkitCopy = CraftItemStack.asBukkitCopy(e);
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
                final NBTTagCompound tag = itemStack.getTag();
                if (tag != null) {
                    final org.bukkit.inventory.ItemStack bukkitCopy2 = CraftItemStack.asBukkitCopy(itemStack);
                    if ((bukkitCopy2.getType() == Material.CHEST || bukkitCopy2.getType() == Material.TRAPPED_CHEST || bukkitCopy2.getType() == Material.HOPPER_MINECART || bukkitCopy2.getType() == Material.STORAGE_MINECART || bukkitCopy2.getType() == Material.MINECART) && Settings.IMP.PACKET_DECODER.BLOCK_CHESTS_WITH_NBT_ON_CREATIVE && tag.toString().length() >= Settings.IMP.PACKET_DECODER.BLOCK_CHESTS_WITH_NBT_BLOCK_SIZE_HIGHER_THAN) {
                        SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Player ").append(player.getName()).append(" clicked in chest/minecraft with nbt data bigger than ").append(Settings.IMP.PACKET_DECODER.BLOCK_CHESTS_WITH_NBT_BLOCK_SIZE_HIGHER_THAN).append(" (").append(tag.toString().length()).append(") in creative mode.")), new Object[0]);
                        player.kickPlayer(ChatColor.translateAlternateColorCodes('&', Settings.IMP.PACKET_DECODER.BLOCK_CHESTS_WITH_NBT_KICK_MESSAGE));
                        itemStack.setTag(new NBTTagCompound());
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("clicked in chest/minecraft with nbt data bigger than").append(Settings.IMP.PACKET_DECODER.BLOCK_CHESTS_WITH_NBT_BLOCK_SIZE_HIGHER_THAN).append(" (").append(tag.toString().length()).append(") in creative mode.")), false, false, "Disable \"block-chests-with-nbt-on-creative\" in settings.yml.");
                    }
                }
                final ExploitDetails checkNbtTags2 = this.checkNbtTags(simpleName, itemStack);
                if (checkNbtTags2 != null) {
                    return checkNbtTags2;
                }
                final org.bukkit.inventory.ItemStack bukkitCopy3 = CraftItemStack.asBukkitCopy(itemStack);
                if ((bukkitCopy3.getType() == Material.CHEST || bukkitCopy3.getType() == Material.HOPPER) && bukkitCopy3.hasItemMeta() && bukkitCopy3.getItemMeta().toString().getBytes().length > 262144) {
                    return new ExploitDetails(this.user, simpleName, "too big chest data", false, false);
                }
            }
        }
        else if (o.getClass().getSimpleName().equals("PacketPlayInCustomPayload")) {
            final PacketPlayInCustomPayload packetPlayInCustomPayload = (PacketPlayInCustomPayload)o;
            final String a = packetPlayInCustomPayload.a();
            final PacketDataSerializer b2 = packetPlayInCustomPayload.b();
            if (b2.capacity() > Settings.IMP.PAYLOAD_SETTINGS.MAX_CAPACITY) {
                return new ExploitDetails(this.user, simpleName, "invalid bytebuf capacity", false, true, "set \"payload-settings.max-capacity\" to higher value in settings.yml");
            }
            Label_1724: {
                if (a.equals("MC|BEdit") || a.equals("MC|BSign") || a.equals("minecraft:bedit") || a.equals("minecraft:bsign")) {
                    if (Settings.IMP.DEBUG) {
                        SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
                    }
                    try {
                        final ItemStack k = b2.k();
                        if (k != null) {
                            if (System.currentTimeMillis() - this.lastBookplace > 60000L && Settings.IMP.PAYLOAD_SETTINGS.BLOCK_BOOK_SIGNING_WHEN_NO_BOOK_PLACED) {
                                return new ExploitDetails(this.user, simpleName, "book sign, but no book used", false, false, "set \"payload-settings.block-book-signing-when-no-book-placed\" in settings.yml to false");
                            }
                            if (Settings.IMP.PAYLOAD_SETTINGS.FAIL_WHEN_NOT_HOLDING_BOOK && !player.getInventory().contains(Material.valueOf("BOOK_AND_QUILL")) && !player.getInventory().contains(Material.WRITTEN_BOOK)) {
                                return new ExploitDetails(this.user, simpleName, "book interact, but no book exists in player's inventory", false, true, "set \"payload-settings.fail-when-not-holding-book\" in settings.yml to false");
                            }
                            final ExploitDetails checkNbtTags3 = this.checkNbtTags(simpleName, k);
                            if (checkNbtTags3 != null) {
                                return checkNbtTags3;
                            }
                        }
                        break Label_1724;
                    }
                    catch (Exception ex3) {
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("exception: ").append(ex3.getMessage())), false, false);
                    }
                }
                if (a.equals("REGISTER") || Integer.valueOf(1321107516).equals(a.toUpperCase().hashCode()) || a.toLowerCase().contains("fml")) {
                    ByteBuf copy = null;
                    try {
                        copy = b2.copy();
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
                else if (a.equals("MC|ItemName") && player.getInventory() != null && player.getOpenInventory().getType() != InventoryType.ANVIL && Settings.IMP.BLOCK_ITEMNAME_WHEN_NO_ANVIL) {
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
            final double a2 = packetPlayInFlying.a(0.0);
            final double b3 = packetPlayInFlying.b(0.0);
            final double c2 = packetPlayInFlying.c(0.0);
            if ((a2 >= Double.MAX_VALUE || b3 > Double.MAX_VALUE || c2 >= Double.MAX_VALUE) && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V2) {
                return new ExploitDetails(this.user, simpleName, "Double.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v2\" to true");
            }
            if (a2 >= 2.147483647E9 || b3 > 2.147483647E9 || (c2 >= 2.147483647E9 && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V1)) {
                return new ExploitDetails(this.user, simpleName, "Integer.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v1\" to true");
            }
            final float a3 = packetPlayInFlying.a(0.0f);
            final float b4 = packetPlayInFlying.b(0.0f);
            if (a3 == Float.NEGATIVE_INFINITY || b4 == Float.NEGATIVE_INFINITY || a3 >= Float.MAX_VALUE || b4 >= Float.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "invalid float position", false, true);
            }
            if (Settings.IMP.POSITION_CHECKS.CHECK_YAW_CRASHER && (a3 < -60000.0f || b4 < -60000.0f || a3 > 60000.0f || b4 > 60000.0f || a3 == 9.223372E18 || b4 == 9.223372E18 || a3 == 9.223372E18f || b4 == 9.223372E18f)) {
                return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Invalid yaw/pitch v3 (yaw: ").append(a3).append(", pitch: ").append(b4).append(")")), false, false, "set position-checks.check-yaw-crasher to false in settings.yml report to staff on mc-protection.eu/discord");
            }
        }
        else if (o instanceof PacketPlayInHeldItemSlot) {
            final int a4 = ((PacketPlayInHeldItemSlot)o).a();
            if (a4 >= 36 || a4 < 0) {
                return new ExploitDetails(this.user, simpleName, "invalid held item slot", false, true);
            }
        }
        else if (o.getClass().getSimpleName().equals("PacketPlayInSteerVehicle")) {
            final PacketPlayInSteerVehicle packetPlayInSteerVehicle = (PacketPlayInSteerVehicle)o;
            if (packetPlayInSteerVehicle.a() >= Float.MAX_VALUE || packetPlayInSteerVehicle.b() >= Float.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "invalid vehicle movement", false, true);
            }
        }
        else if (o.getClass().getSimpleName().equals("PacketPlayInVehicleMove")) {
            final PacketPlayInVehicleMove packetPlayInVehicleMove = (PacketPlayInVehicleMove)o;
            if (packetPlayInVehicleMove.getX() >= Double.MAX_VALUE || packetPlayInVehicleMove.getY() >= Double.MAX_VALUE || packetPlayInVehicleMove.getZ() >= Double.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "invalid vehicle movement v2", false, true);
            }
            final Location location = new Location(player.getWorld(), packetPlayInVehicleMove.getX(), packetPlayInVehicleMove.getY(), packetPlayInVehicleMove.getZ(), packetPlayInVehicleMove.getYaw(), packetPlayInVehicleMove.getPitch());
            if (player.getLocation().distance(location) > 30.0 && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_VEHICLE_MOVEMENT_V3) {
                return new ExploitDetails(this.user, simpleName, "invalid vehicle movement v3", false, true, "set position-checks.allow-invalid-vehicle-movement-v3 to true");
            }
            if (player.getLocation().distance(location) > 3.0 && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_VEHICLE_MOVEMENT_V4 && this.hasReachedLimit("PacketPlayInVehicleMove_Invalid_v4") != null) {
                return new ExploitDetails(this.user, simpleName, "invalid vehicle movement v4", false, true, "set position-checks.allow-invalid-vehicle-movement-v4 to true");
            }
        }
        final LimitablePacket hasReachedLimit2 = this.hasReachedLimit(simpleName);
        if (hasReachedLimit2 != null) {
            return new ExploitDetails(this.user, simpleName, "packet limit exceed", hasReachedLimit2.isCancelOnly(), false);
        }
        return null;
    }
    
    static {
        URL_MATCHER = Pattern.compile("url");
        LIMITATION = SpigotGuardPlugin.getInstance().getPacketLimitation();
    }
    
    private LimitablePacket hasReachedLimit(final String s) {
        if (!PacketDecoder_1_12.LIMITATION.isLimitable(s)) {
            return null;
        }
        final LimitablePacket limit = PacketDecoder_1_12.LIMITATION.getLimit(s);
        if (this.user.increaseAndGetReceivedPackets(s) < limit.getLimit()) {
            return null;
        }
        return limit;
    }
    
    @Override
    public User getUser() {
        return this.user;
    }
    
    private void lambda$decode$0(final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)this.injector.getPlayer(), exploitDetails));
    }
    
    public PacketDecoder_1_12(final PacketInjector packetInjector, final User user) {
        super(packetInjector);
        this.disconnected = false;
        this.lastBookplace = -1L;
        this.user = user;
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
    
    private void lambda$decode$1(final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$0);
    }
}
