package xyz.yooniks.spigotguard.network.v1_8_R3;

import xyz.yooniks.spigotguard.user.*;
import xyz.yooniks.spigotguard.network.*;
import io.netty.util.concurrent.*;
import xyz.yooniks.spigotguard.*;
import org.bukkit.plugin.*;
import io.netty.channel.*;
import xyz.yooniks.spigotguard.config.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import org.bukkit.event.inventory.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.*;
import io.netty.buffer.*;
import java.nio.charset.*;
import org.bukkit.entity.*;
import java.lang.reflect.*;
import xyz.yooniks.spigotguard.limitation.*;
import org.bukkit.inventory.*;
import java.util.regex.*;
import java.util.*;
import xyz.yooniks.spigotguard.helper.*;
import net.minecraft.server.v1_8_R3.*;
import xyz.yooniks.spigotguard.event.*;
import org.bukkit.*;
import org.bukkit.event.*;

@ChannelHandler.Sharable
public class PacketDecoder_1_8 extends PacketDecoder
{
    private long lastBookplace;
    private static final PacketLimitation LIMITATION;
    private static final String BLOCKED;
    private long lastAnimation;
    private long lastTabComplete;
    private static final Pattern URL_MATCHER;
    private final User user;
    private double lastZ;
    private boolean disconnected;
    private double lastX;
    private double lastY;
    
    public PacketDecoder_1_8(final PacketInjector packetInjector, final User user) {
        super(packetInjector);
        this.lastY = 0.0;
        this.lastX = 0.0;
        this.lastZ = 0.0;
        this.disconnected = false;
        this.lastBookplace = -1L;
        this.user = user;
    }
    
    private void lambda$decode$1(final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$0);
    }
    
    protected void decode(final ChannelHandlerContext channelHandlerContext, final Object o, final List<Object> list) {
        if (this.disconnected) {
            return;
        }
        final ExploitDetails failure2 = this.failure2(o);
        if (failure2 == null) {
            list.add(o);
            return;
        }
        if (failure2.isCancelOnly()) {
            return;
        }
        this.disconnected = true;
        if (Settings.IMP.KICK.TYPE == 0) {
            channelHandlerContext.close().addListener(this::lambda$decode$1);
        }
        else {
            Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$decode$2);
        }
        SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(SpigotGuardPlugin.getInstance().getSpigotGuardClassLoaded().getSerializable()).append(" Player ").append(this.injector.getPlayer().getName()).append(" tried to crash the server, packet: ").append(o.getClass().getSimpleName()).append(", details: ").append(failure2.getDetails()).append(", how to fix if false positive: ").append(failure2.getHowToFixFalseDetect()).append(")")), new Object[0]);
    }
    
    private ExploitDetails failure2(final Object o) {
        final Player player = this.injector.getPlayer();
        if (o != null && Settings.IMP.DEBUG) {
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(player.getName()).append(" received ").append(o.getClass().getSimpleName()).append(" -> ").append(o.toString())), new Object[0]);
        }
        final String simpleName = o.getClass().getSimpleName();
        if (o instanceof PacketPlayInWindowClick) {
            if (Settings.IMP.DEBUG) {
                SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Received ").append(simpleName).append(" from ").append(player.getName())), new Object[0]);
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
                final int b = packetPlayInWindowClick.b();
                if (b > 127 || b < -999) {
                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Invalid slot ").append(b)), false, true, "can't be fixed, it's surely exploit");
                }
                if (b < 0 && b > -999 && e != null) {
                    final Item item = e.getItem();
                    if (item instanceof ItemBookAndQuill || item instanceof ItemWrittenBook || item instanceof ItemFireworks || item instanceof ItemFireworksCharge || item instanceof ItemSkull) {
                        final LimitablePacket hasReachedLimit = this.hasReachedLimit("PacketPlayInWindowClick_InvalidSlot");
                        if (hasReachedLimit != null) {
                            return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("invalid slot + dangerous item, slot: ").append(b).append("")), hasReachedLimit.isCancelOnly(), false, "increase packet limit of PacketPlayInWindowClick_InvalidSlot in config.yml");
                        }
                    }
                }
                if (e != null) {
                    final ExploitDetails checkNbtTags = this.checkNbtTags(simpleName, e);
                    if (checkNbtTags != null) {
                        return checkNbtTags;
                    }
                }
                try {
                    final InventoryView openInventory = player.getOpenInventory();
                    if (openInventory != null) {
                        final Inventory topInventory = openInventory.getTopInventory();
                        final Inventory bottomInventory = openInventory.getBottomInventory();
                        int countSlots = openInventory.countSlots();
                        if (topInventory.getType() == InventoryType.CRAFTING && bottomInventory.getType() == InventoryType.PLAYER) {
                            countSlots += 4;
                        }
                        if (b >= countSlots && PacketDecoder_1_8.LIMITATION != null) {
                            final LimitablePacket hasReachedLimit2 = this.hasReachedLimit("PacketPlayInWindowClick_InvalidSlot");
                            if (hasReachedLimit2 != null) {
                                return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("invalid slot (").append(b).append(" >= ").append(countSlots).append(")")), hasReachedLimit2.isCancelOnly(), false, "increase packet limit of PacketPlayInWindowClick_InvalidSlot in config.yml");
                            }
                        }
                        if (e != null && b > 0 && openInventory.getType() == InventoryType.PLAYER) {
                            try {
                                final org.bukkit.inventory.ItemStack item2 = openInventory.getItem(b);
                                final Item item3 = e.getItem();
                                if (item3 instanceof ItemWrittenBook || item3 instanceof ItemBookAndQuill) {
                                    e.getTag().remove("pages");
                                    e.getTag().remove("author");
                                    e.getTag().remove("title");
                                    if (item2 != null && item2.getType() != Material.valueOf("BOOK_AND_QUILL") && item2.getType() != Material.WRITTEN_BOOK && e.getTag() != null && e.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("tried to use book but real item is ").append(item2.getType().name())), false, true, "increase \"block-fake-packets-only-when-items-nbt-bigger-than\" value in settings.yml");
                                    }
                                }
                                if ((item3 instanceof ItemFireworks || item3 instanceof ItemFireworksCharge) && item2 != null && item2.getType() != Material.valueOf("FIREWORK") && item2.getType() != Material.valueOf("FIREWORK_CHARGE") && e.getTag() != null && e.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("tried to use firework but real item is ").append(item2.getType().name())), false, true, "increase \"block-fake-packets-only-when-items-nbt-bigger-than\" value in settings.yml");
                                }
                                if (item3 instanceof ItemBanner && item2 != null && !item2.getType().name().contains("BANNER") && e.getTag() != null && e.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("tried to use banner but real item is ").append(item2.getType().name())), false, true, "increase \"block-fake-packets-only-when-items-nbt-bigger-than\" value in settings.yml");
                                }
                            }
                            catch (Exception ex7) {}
                        }
                    }
                }
                catch (Exception ex8) {}
                if (e != null) {
                    final org.bukkit.inventory.ItemStack bukkitCopy = CraftItemStack.asBukkitCopy(e);
                    if ((bukkitCopy.getType() == Material.CHEST || bukkitCopy.getType() == Material.HOPPER) && bukkitCopy.hasItemMeta() && bukkitCopy.getItemMeta().toString().getBytes().length > 262144) {
                        return new ExploitDetails(this.user, simpleName, "too big chest data", false, false);
                    }
                }
            }
            catch (Exception ex2) {
                return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("exception ").append(ex2.getMessage())), false, false);
            }
        }
        else if (o instanceof PacketPlayInSetCreativeSlot) {
            if (Settings.IMP.DEBUG) {
                SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
            }
            if (player.getGameMode() != GameMode.CREATIVE && !Settings.IMP.ALLOW_CREATIVE_INVENTORY_CLICK_WITHOUT_GAMEMODE) {
                ItemStack itemStack = null;
                try {
                    final Field declaredField2 = PacketPlayInSetCreativeSlot.class.getDeclaredField("b");
                    declaredField2.setAccessible(true);
                    itemStack = (ItemStack)declaredField2.get(o);
                }
                catch (Exception ex3) {
                    ex3.printStackTrace();
                }
                if (itemStack != null) {
                    if (itemStack.getName().toLowerCase().contains("book") && itemStack.getTag() != null) {
                        itemStack.getTag().remove("pages");
                        itemStack.getTag().remove("author");
                        itemStack.getTag().remove("title");
                    }
                    itemStack.setTag(new NBTTagCompound());
                }
                return new ExploitDetails(this.user, simpleName, "clicking in creative inventory without gamemode 1", false, true, "set \"allow-creative-inventory-click-without-gamemode\" to true in settings.yml");
            }
            ItemStack itemStack2 = null;
            try {
                final Field declaredField3 = PacketPlayInSetCreativeSlot.class.getDeclaredField("b");
                declaredField3.setAccessible(true);
                itemStack2 = (ItemStack)declaredField3.get(o);
            }
            catch (Exception ex4) {
                ex4.printStackTrace();
            }
            if (itemStack2 != null) {
                final ExploitDetails checkNbtTags2 = this.checkNbtTags(simpleName, itemStack2);
                if (checkNbtTags2 != null) {
                    return checkNbtTags2;
                }
                final org.bukkit.inventory.ItemStack bukkitCopy2 = CraftItemStack.asBukkitCopy(itemStack2);
                if ((bukkitCopy2.getType() == Material.CHEST || bukkitCopy2.getType() == Material.HOPPER) && bukkitCopy2.hasItemMeta() && bukkitCopy2.getItemMeta().toString().getBytes().length > 262144) {
                    return new ExploitDetails(this.user, simpleName, "too big chest data", false, false);
                }
            }
        }
        else if (o instanceof PacketPlayInBlockPlace) {
            if (Settings.IMP.DEBUG) {
                SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
            }
            ItemStack itemStack3 = ((PacketPlayInBlockPlace)o).getItemStack();
            if (itemStack3 == null) {
                try {
                    final Field declaredField4 = PacketPlayInBlockPlace.class.getDeclaredField("d");
                    declaredField4.setAccessible(true);
                    itemStack3 = (ItemStack)declaredField4.get(o);
                }
                catch (Exception ex5) {
                    ex5.printStackTrace();
                }
            }
            if (itemStack3 != null) {
                final Item item4 = itemStack3.getItem();
                if (item4 instanceof ItemWrittenBook || item4 instanceof ItemBookAndQuill) {
                    final LimitablePacket hasReachedLimit3 = this.hasReachedLimit("BOOK_Place");
                    if (hasReachedLimit3 != null) {
                        SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Player {0} has reached ").append(hasReachedLimit3.getLimit()).append("/s book places (ppm), it is exploit probably. If it is false positive, please increase BOOK_Place limit in config.yml")), player.getName());
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("too many book places (").append(hasReachedLimit3.getLimit()).append("/s)")), hasReachedLimit3.isCancelOnly(), false, "increase packet limit of BOOK_Place in config.yml");
                    }
                    this.lastBookplace = System.currentTimeMillis();
                    final org.bukkit.inventory.ItemStack itemInHand = player.getItemInHand();
                    if (itemInHand == null || (itemInHand.getType() != Material.WRITTEN_BOOK && itemInHand.getType() != Material.valueOf("BOOK_AND_QUILL") && Settings.IMP.PLACE_CHECKS.BLOCK_PLACING_BOOK_WHEN_NOT_HOLDING_IT)) {
                        if (itemStack3.getTag() != null) {
                            itemStack3.getTag().remove("pages");
                            itemStack3.getTag().remove("author");
                            itemStack3.getTag().remove("title");
                        }
                        return new ExploitDetails(this.user, simpleName, "placing book but not holding it", false, true, "set \"place-checks.block-placing-book-when-not-holding-it\" in settings.yml to false");
                    }
                }
                if (item4 instanceof ItemFireworks || item4 instanceof ItemFireworksCharge) {
                    final org.bukkit.inventory.ItemStack itemInHand2 = player.getItemInHand();
                    if ((itemInHand2 == null || (itemInHand2.getType() != Material.valueOf("FIREWORK_CHARGE") && itemInHand2.getType() != Material.valueOf("FIREWORK") && Settings.IMP.PLACE_CHECKS.BLOCK_PLACING_FIREWORK_WHEN_NOT_HOLDING_IT)) && itemStack3.getTag() != null && itemStack3.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                        return new ExploitDetails(this.user, simpleName, "placing firework but not holding it", false, true, "set \"place-checks.block-placing-firework-when-not-holding-it\" in settings.yml to false");
                    }
                }
                if (item4 instanceof ItemBanner) {
                    final org.bukkit.inventory.ItemStack itemInHand3 = player.getItemInHand();
                    if ((itemInHand3 == null || (!itemInHand3.getType().name().contains("BANNER") && Settings.IMP.PLACE_CHECKS.BLOCK_PLACING_BANNER_WHEN_NOT_HOLDING_IT)) && itemStack3.getTag() != null && itemStack3.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                        return new ExploitDetails(this.user, simpleName, "placing banner but not holding it", false, true, "set \"place-checks.block-placing-banner-when-not-holding-it\" in settings.yml to false");
                    }
                }
                final ExploitDetails checkNbtTags3 = this.checkNbtTags(simpleName, itemStack3);
                if (checkNbtTags3 != null) {
                    return checkNbtTags3;
                }
                final org.bukkit.inventory.ItemStack bukkitCopy3 = CraftItemStack.asBukkitCopy(itemStack3);
                if ((bukkitCopy3.getType() == Material.CHEST || bukkitCopy3.getType() == Material.HOPPER) && bukkitCopy3.hasItemMeta() && bukkitCopy3.getItemMeta().toString().getBytes().length > 262144) {
                    return new ExploitDetails(this.user, simpleName, "Too big chest data", false, false);
                }
            }
        }
        else if (o instanceof PacketPlayInCustomPayload) {
            final PacketPlayInCustomPayload packetPlayInCustomPayload = (PacketPlayInCustomPayload)o;
            final String a = packetPlayInCustomPayload.a();
            final PacketDataSerializer b2 = packetPlayInCustomPayload.b();
            if (((ByteBuf)b2).capacity() > Settings.IMP.PAYLOAD_SETTINGS.MAX_CAPACITY) {
                return new ExploitDetails(this.user, simpleName, "invalid bytebuf capacity", false, true, "set \"payload-settings.max-capacity\" to higher value in settings.yml");
            }
            if (Integer.valueOf(-296262810).equals(a.toUpperCase().hashCode()) || Integer.valueOf(-295840999).equals(a.toUpperCase().hashCode()) || Integer.valueOf(883258655).equals(a.toUpperCase().hashCode()) || Integer.valueOf(883680466).equals(a.toUpperCase().hashCode())) {
                if (Settings.IMP.DEBUG) {
                    SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
                }
                final LimitablePacket hasReachedLimit4 = this.hasReachedLimit("BOOK_Edit");
                if (hasReachedLimit4 != null) {
                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("too many book edits/signs! (").append(hasReachedLimit4.getLimit()).append("/s)")), hasReachedLimit4.isCancelOnly(), false, "increase packet limit of BOOK_Edit in config.yml");
                }
                try {
                    final ItemStack i = new PacketDataSerializer(Unpooled.wrappedBuffer((ByteBuf)b2)).i();
                    if (i != null) {
                        if (System.currentTimeMillis() - this.lastBookplace > 60000L && Settings.IMP.PAYLOAD_SETTINGS.BLOCK_BOOK_SIGNING_WHEN_NO_BOOK_PLACED) {
                            return new ExploitDetails(this.user, simpleName, "book sign, but no book used", false, false, "set \"payload-settings.block-book-signing-when-no-book-placed\" in settings.yml to false");
                        }
                        if (Settings.IMP.PAYLOAD_SETTINGS.FAIL_WHEN_NOT_HOLDING_BOOK && !player.getInventory().contains(Material.valueOf("BOOK_AND_QUILL")) && !player.getInventory().contains(Material.WRITTEN_BOOK)) {
                            return new ExploitDetails(this.user, simpleName, "book interact, but no book exists in player's inventory", false, true, "set \"payload-settings.fail-when-not-holding-book\" in settings.yml to false");
                        }
                        final ExploitDetails checkNbtTags4 = this.checkNbtTags(simpleName, i);
                        if (checkNbtTags4 != null) {
                            return checkNbtTags4;
                        }
                    }
                }
                catch (Exception ex6) {
                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("exception: ").append(ex6.getMessage())), false, false);
                }
            }
            else if (a.equals("REGISTER") || Integer.valueOf(1321107516).equals(a.toUpperCase().hashCode()) || a.toLowerCase().contains("fml")) {
                ByteBuf copy = null;
                try {
                    copy = ((ByteBuf)b2).copy();
                    if (copy.toString(StandardCharsets.UTF_8).split("\u0000").length > 124) {
                        return new ExploitDetails(this.user, simpleName, "too many channels", false, false);
                    }
                    if (copy != null) {
                        copy.release();
                    }
                }
                catch (Exception ex9) {
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
        else if (o instanceof PacketPlayInFlying.PacketPlayInPosition) {
            final PacketPlayInFlying.PacketPlayInPosition packetPlayInPosition = (PacketPlayInFlying.PacketPlayInPosition)o;
            final double a2 = packetPlayInPosition.a();
            final double b3 = packetPlayInPosition.b();
            final double c = packetPlayInPosition.c();
            if ((a2 >= Double.MAX_VALUE || b3 > Double.MAX_VALUE || c >= Double.MAX_VALUE) && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V2) {
                return new ExploitDetails(this.user, simpleName, "Double.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v2\" to true");
            }
            if (a2 >= 2.147483647E9 || b3 > 2.147483647E9 || (c >= 2.147483647E9 && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V1)) {
                return new ExploitDetails(this.user, simpleName, "Integer.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v1\" to true");
            }
            if (Settings.IMP.POSITION_CHECKS.CHECK_CHUNK_CRASHER) {
                final double n = Math.max(player.getLocation().getX(), a2) - Math.min(player.getLocation().getX(), a2);
                if (n >= 10.0 && n % 1.0 == 0.0) {
                    SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Received x invalid location packet from: ").append(player.getName()).append(" (").append(player.getLocation().getX()).append(",").append(player.getLocation().getY()).append(",").append(player.getLocation().getZ()).append(" ||| Packet: ").append(a2).append(",").append(b3).append(",").append(c).append("), difference: ").append(n)), new Object[0]);
                    if (n > 1000000.0) {
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Chunk crasher (v2, invalid x coords, diff: ").append(n).append(")")), false, false, "set position-checks.check-chunk-crasher to false in settings.yml");
                    }
                    if (n == 99413.0) {
                        return new ExploitDetails(this.user, simpleName, "Chunk crasher v2 (99413.0 difference between old and new Z coords)", false, true, "Set position-checks-check-chunk-crasher-v2 to false in settings.yml");
                    }
                    if (this.hasReachedLimit("PacketPlayInPosition_Invalid_v1") != null) {
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Chunk crasher (v1, invalid x coords, diff: ").append(n).append(")")), false, false, "set position-checks.check-chunk-crasher to false in settings.yml or increase PacketPlayInPosition_Invalid_v1.limit in config.yml");
                    }
                }
                final double n2 = Math.max(player.getLocation().getZ(), c) - Math.min(player.getLocation().getZ(), c);
                if (n2 >= 10.0 && n2 % 1.0 == 0.0) {
                    SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Received z invalid location packet from: ").append(player.getName()).append(" (").append(player.getLocation().getX()).append(",").append(player.getLocation().getY()).append(",").append(player.getLocation().getZ()).append(" ||| Packet: ").append(a2).append(",").append(b3).append(",").append(c).append("), difference: ").append(n2)), new Object[0]);
                    if (n2 > 1000000.0) {
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Chunk crasher (v2, invalid z coords, diff: ").append(n2).append(")")), false, false, "set position-checks.check-chunk-crasher to false in settings.yml");
                    }
                    if (n2 == 99413.0) {
                        return new ExploitDetails(this.user, simpleName, "Chunk crasher v2 (99413.0 difference between old and new Z coords)", false, true, "Set position-checks-check-chunk-crasher-v2 to false in settings.yml");
                    }
                    if (this.hasReachedLimit("PacketPlayInPosition_Invalid_v1") != null) {
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Chunk crasher (v1, invalid z coords, diff: ").append(n2).append(")")), false, false, "set position-checks.check-chunk-crasher to false in settings.yml or increase PacketPlayInPosition_Invalid_v1.limit in config.yml");
                    }
                }
            }
            if (Settings.IMP.POSITION_CHECKS.CHECK_FLY_CRASHER && b3 == player.getLocation().getY() + 0.1) {
                final double n3 = Math.max(player.getLocation().getY(), b3) - Math.min(player.getLocation().getY(), b3);
                SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Received invalid Y location packet from: ").append(player.getName()).append(" (").append(player.getLocation().getX()).append(",").append(player.getLocation().getY()).append(",").append(player.getLocation().getZ()).append(" ||| Packet: ").append(a2).append(",").append(b3).append(",").append(c).append("), difference: ").append(n3)), new Object[0]);
                if (this.hasReachedLimit("PacketPlayInPosition_Invalid_v2") != null) {
                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Fly crasher (v1, invalid y coords, diff: ").append(n3).append(")")), false, false, "set position-checks.check-fly-crasher to false in settings.yml or increase PacketPlayInPosition_Invalid_v2.limit in config.yml");
                }
            }
        }
        else if (o instanceof PacketPlayInFlying) {
            final PacketPlayInFlying packetPlayInFlying = (PacketPlayInFlying)o;
            final double a3 = packetPlayInFlying.a();
            final double b4 = packetPlayInFlying.b();
            final double c2 = packetPlayInFlying.c();
            if ((a3 >= Double.MAX_VALUE || b4 > Double.MAX_VALUE || c2 >= Double.MAX_VALUE) && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V2) {
                return new ExploitDetails(this.user, simpleName, "Double.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v2\" to true");
            }
            if (a3 >= 2.147483647E9 || b4 > 2.147483647E9 || (c2 >= 2.147483647E9 && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V1)) {
                return new ExploitDetails(this.user, simpleName, "Integer.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v1\" to true");
            }
            final float d = packetPlayInFlying.d();
            final float e2 = packetPlayInFlying.e();
            if (d == Float.NEGATIVE_INFINITY || e2 == Float.NEGATIVE_INFINITY || d >= Float.MAX_VALUE || e2 >= Float.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "invalid float position", false, true);
            }
            if (Settings.IMP.POSITION_CHECKS.CHECK_YAW_CRASHER && (d < -60000.0f || e2 < -60000.0f || d > 60000.0f || e2 > 60000.0f || d == 9.223372E18 || e2 == 9.223372E18 || d == 9.223372E18f || e2 == 9.223372E18f)) {
                return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Invalid yaw/pitch v3 (yaw: ").append(d).append(", pitch: ").append(e2).append(")")), false, false, "set position-checks.check-yaw-crasher to false in settings.yml report to staff on mc-protection.eu/discord");
            }
            if (Math.abs(b4) > 1.0E9) {
                return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Invalid Y coords (").append(b4).append(")")), false, false, "report to staff on mc-protection.eu/discord");
            }
            if (this.lastY == 0.0) {
                this.lastY = b4;
            }
            if (this.lastZ == 0.0) {
                this.lastZ = c2;
            }
            if (this.lastX == 0.0) {
                this.lastX = a3;
            }
            else if (c2 - this.lastZ == 9.0) {
                this.lastZ = 0.0;
                final LimitablePacket hasReachedLimit5 = this.hasReachedLimit("PacketPlayInPositionLook_InvalidMovement");
                if (hasReachedLimit5 != null) {
                    return new ExploitDetails(this.user, simpleName, "invalid Z movement", hasReachedLimit5.isCancelOnly(), false, "increase packet limit of PacketPlayInPositionLook_InvalidMovement in config.yml");
                }
            }
            else if (b4 - this.lastY == 9.0) {
                this.lastY = 0.0;
                final LimitablePacket hasReachedLimit6 = this.hasReachedLimit("PacketPlayInPositionLook_InvalidMovement");
                if (hasReachedLimit6 != null) {
                    return new ExploitDetails(this.user, simpleName, "invalid Y movement", hasReachedLimit6.isCancelOnly(), false, "increase packet limit of PacketPlayInPositionLook_InvalidMovement in config.yml");
                }
            }
            else if (a3 - this.lastX == 9.0) {
                this.lastX = 0.0;
                final LimitablePacket hasReachedLimit7 = this.hasReachedLimit("PacketPlayInPositionLook_InvalidMovement");
                if (hasReachedLimit7 != null) {
                    return new ExploitDetails(this.user, simpleName, "invalid X movement", hasReachedLimit7.isCancelOnly(), false, "increase packet limit of PacketPlayInPositionLook_InvalidMovement in config.yml");
                }
            }
            this.lastX = a3;
            this.lastY = b4;
            this.lastZ = c2;
        }
        else if (o instanceof PacketPlayInHeldItemSlot) {
            final int a4 = ((PacketPlayInHeldItemSlot)o).a();
            if (a4 >= 36 || a4 < 0) {
                return new ExploitDetails(this.user, simpleName, "invalid held item slot", false, true);
            }
        }
        else if (o instanceof PacketPlayInSteerVehicle) {
            final PacketPlayInSteerVehicle packetPlayInSteerVehicle = (PacketPlayInSteerVehicle)o;
            if (packetPlayInSteerVehicle.b() >= Float.MAX_VALUE || packetPlayInSteerVehicle.a() >= Float.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "invalid vehicle movement", false, true);
            }
        }
        final LimitablePacket hasReachedLimit8 = this.hasReachedLimit(simpleName);
        if (hasReachedLimit8 != null) {
            return new ExploitDetails(this.user, simpleName, "packet limit exceed", hasReachedLimit8.isCancelOnly(), false, String.valueOf(new StringBuilder().append("increase packet limit of ").append(simpleName).append(" in config.yml or completely remove it")));
        }
        return null;
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
        if (item instanceof ItemWrittenBook || item instanceof ItemBookAndQuill) {
            if (tag.hasKey(":[{extra:[{") || Objects.requireNonNull(tag).toString().contains(":[{extra:[{")) {
                tag.remove("pages");
                tag.remove("author");
                tag.remove("title");
                SpigotGuardLogger.log(Level.INFO, "Player {0} has tried to crash server with \"extra:[{\" book crasher.", this.injector.getPlayer().getName());
                return new ExploitDetails(this.user, s, "bad book data (extra nbt keys)", false, false, "not fixable, if false positive > report to our staff.");
            }
            final LimitablePacket hasReachedLimit = this.hasReachedLimit("BOOK_Use");
            if (hasReachedLimit != null) {
                SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Player {0} has reached ").append(hasReachedLimit.getLimit()).append("/s book interactions, it is exploit probably. If it is false positive, please increase BOOK_Use limit in config.yml")), this.injector.getPlayer().getName());
                return new ExploitDetails(this.user, s, String.valueOf(new StringBuilder().append("too many book interations! (").append(hasReachedLimit.getLimit()).append("/s)")), hasReachedLimit.isCancelOnly(), false, "increase packet limit of BOOK_Use in config.yml");
            }
            if (Settings.IMP.BOOK.FAST_CHECK && String.valueOf(tag.get("pages")).length() > Settings.IMP.BOOK.FAST_CHECK_MAX_LENGTH) {
                tag.remove("pages");
                tag.remove("author");
                tag.remove("title");
                itemStack.setTag(new NBTTagCompound());
                return new ExploitDetails(this.user, s, "too large pages tag (fast-check)", false, false, "set \"book.fast-check\" in settings.yml to false or increase limit of \"book.fast-check-max-length\" ");
            }
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
                tag.remove("pages");
                tag.remove("author");
                tag.remove("title");
                itemStack.setTag(new NBTTagCompound());
                return new ExploitDetails(this.user, s, "using book while it is banned", false, true, "set \"book.ban-books\" in settings.yml to false");
            }
            final NBTTagList list = tag.getList("pages", 8);
            if (list.size() > imp.BOOK.MAX_PAGES) {
                tag.remove("pages");
                tag.remove("author");
                tag.remove("title");
                itemStack.setTag(new NBTTagCompound());
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
                    tag.remove("pages");
                    tag.remove("author");
                    tag.remove("title");
                    itemStack.setTag(new NBTTagCompound());
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
                    tag.remove("pages");
                    tag.remove("author");
                    tag.remove("title");
                    itemStack.setTag(new NBTTagCompound());
                    return new ExploitDetails(this.user, s, "too many similar pages", false, true, "set \"book.max-similar-pages\" in settings.yml to higher value");
                }
                final String stripColor = ChatColor.stripColor(string.replaceAll("\\+", ""));
                if (stripColor == null || stripColor.equals("null")) {
                    return new ExploitDetails(this.user, s, "null stripped page", false, true);
                }
                if (stripColor.length() > imp.BOOK.MAX_STRIPPED_PAGE_SIZE) {
                    tag.remove("pages");
                    tag.remove("author");
                    tag.remove("title");
                    itemStack.setTag(new NBTTagCompound());
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
                            itemStack.setTag(new NBTTagCompound());
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
                if (compound2.isEmpty()) {
                    SpigotGuardLogger.log(Level.INFO, "Empty properties skull", new Object[0]);
                }
                if (compound2.hasKey("textures")) {
                    final NBTTagList list2 = compound2.getList("textures", 10);
                    if (list2.isEmpty()) {
                        SpigotGuardLogger.log(Level.INFO, "Empty texture skull", new Object[0]);
                    }
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
                            final Matcher matcher = PacketDecoder_1_8.URL_MATCHER.matcher(lowerCase2);
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
    
    static {
        BLOCKED = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
        URL_MATCHER = Pattern.compile("url");
        LIMITATION = SpigotGuardPlugin.getInstance().getPacketLimitation();
    }
    
    @Override
    public User getUser() {
        return this.user;
    }
    
    private void lambda$decode$2() {
        this.injector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
    }
    
    public ExploitDetails failure(final Object o) {
        final Player player = this.injector.getPlayer();
        if (o != null && Settings.IMP.DEBUG) {
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("{0} received ").append(o.getClass().getSimpleName()).append(" -> ").append(o.toString())), new Object[0]);
        }
        final String simpleName = o.getClass().getSimpleName();
        if (o instanceof PacketPlayInArmAnimation) {
            if (System.currentTimeMillis() - this.lastAnimation < Settings.IMP.ARM_ANIMATION_TIMESTAMP) {
                final LimitablePacket hasReachedLimit = this.hasReachedLimit(simpleName);
                if (hasReachedLimit != null) {
                    return new ExploitDetails(this.user, simpleName, "packet limit exceed", hasReachedLimit.isCancelOnly(), false, String.valueOf(new StringBuilder().append("increase packet limit of ").append(simpleName).append(" in config.yml to higher value or completely remove it")));
                }
                return new ExploitDetails(this.user, simpleName, "too fast arm", true, false, "set arm-animation-timestamp in settings.yml to lower value");
            }
            else {
                this.lastAnimation = System.currentTimeMillis();
            }
        }
        else if (o instanceof PacketPlayInWindowClick) {
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
                final int b = packetPlayInWindowClick.b();
                if (b > 127 || b < -999) {
                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("Invalid slot ").append(b)), false, true, "can't be fixed, it's surely exploit");
                }
                if (b < 0 && b > -999 && e != null) {
                    final Item item = e.getItem();
                    if (item instanceof ItemBookAndQuill || item instanceof ItemWrittenBook || item instanceof ItemFireworks || item instanceof ItemFireworksCharge || item instanceof ItemSkull) {
                        final LimitablePacket hasReachedLimit2 = this.hasReachedLimit("PacketPlayInWindowClick_InvalidSlot");
                        if (hasReachedLimit2 != null) {
                            return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("invalid slot + dangerous item, slot: ").append(b).append("")), hasReachedLimit2.isCancelOnly(), false, "increase packet limit of PacketPlayInWindowClick_InvalidSlot in config.yml");
                        }
                    }
                }
                if (e != null) {
                    final ExploitDetails checkNbtTags = this.checkNbtTags(simpleName, e);
                    if (checkNbtTags != null) {
                        return checkNbtTags;
                    }
                }
                try {
                    final InventoryView openInventory = player.getOpenInventory();
                    if (openInventory != null) {
                        final Inventory topInventory = openInventory.getTopInventory();
                        final Inventory bottomInventory = openInventory.getBottomInventory();
                        int countSlots = openInventory.countSlots();
                        if (topInventory.getType() == InventoryType.CRAFTING && bottomInventory.getType() == InventoryType.PLAYER) {
                            countSlots += 4;
                        }
                        if (b >= countSlots && PacketDecoder_1_8.LIMITATION != null) {
                            final LimitablePacket hasReachedLimit3 = this.hasReachedLimit("PacketPlayInWindowClick_InvalidSlot");
                            if (hasReachedLimit3 != null) {
                                return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("invalid slot (").append(b).append(" >= ").append(countSlots).append(")")), hasReachedLimit3.isCancelOnly(), false, "increase packet limit of PacketPlayInWindowClick_InvalidSlot in config.yml");
                            }
                        }
                        if (e != null && b > 0 && openInventory.getType() == InventoryType.PLAYER) {
                            try {
                                final org.bukkit.inventory.ItemStack item2 = openInventory.getItem(b);
                                final Item item3 = e.getItem();
                                if (item3 instanceof ItemWrittenBook || item3 instanceof ItemBookAndQuill) {
                                    e.getTag().remove("pages");
                                    e.getTag().remove("author");
                                    e.getTag().remove("title");
                                    if (item2 != null && item2.getType() != Material.valueOf("BOOK_AND_QUILL") && item2.getType() != Material.WRITTEN_BOOK && e.getTag() != null && e.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("tried to use book but real item is ").append(item2.getType().name())), false, true, "increase \"block-fake-packets-only-when-items-nbt-bigger-than\" value in settings.yml");
                                    }
                                }
                                if ((item3 instanceof ItemFireworks || item3 instanceof ItemFireworksCharge) && item2 != null && item2.getType() != Material.valueOf("FIREWORK") && item2.getType() != Material.valueOf("FIREWORK_CHARGE") && e.getTag() != null && e.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("tried to use firework but real item is ").append(item2.getType().name())), false, true, "increase \"block-fake-packets-only-when-items-nbt-bigger-than\" value in settings.yml");
                                }
                                if (item3 instanceof ItemBanner && item2 != null && !item2.getType().name().contains("BANNER") && e.getTag() != null && e.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("tried to use banner but real item is ").append(item2.getType().name())), false, true, "increase \"block-fake-packets-only-when-items-nbt-bigger-than\" value in settings.yml");
                                }
                            }
                            catch (Exception ex7) {}
                        }
                    }
                }
                catch (Exception ex8) {}
                if (e != null) {
                    final org.bukkit.inventory.ItemStack bukkitCopy = CraftItemStack.asBukkitCopy(e);
                    if ((bukkitCopy.getType() == Material.CHEST || bukkitCopy.getType() == Material.HOPPER) && bukkitCopy.hasItemMeta() && bukkitCopy.getItemMeta().toString().getBytes().length > 262144) {
                        return new ExploitDetails(this.user, simpleName, "too big chest data", false, false);
                    }
                }
            }
            catch (Exception ex2) {
                return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("exception ").append(ex2.getMessage())), false, false);
            }
        }
        else if (o instanceof PacketPlayInSetCreativeSlot) {
            if (Settings.IMP.DEBUG) {
                SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
            }
            if (player.getGameMode() != GameMode.CREATIVE && !Settings.IMP.ALLOW_CREATIVE_INVENTORY_CLICK_WITHOUT_GAMEMODE) {
                ItemStack itemStack = null;
                try {
                    final Field declaredField2 = PacketPlayInSetCreativeSlot.class.getDeclaredField("b");
                    declaredField2.setAccessible(true);
                    itemStack = (ItemStack)declaredField2.get(o);
                }
                catch (Exception ex3) {
                    ex3.printStackTrace();
                }
                if (itemStack != null) {
                    if (itemStack.getName().toLowerCase().contains("book") && itemStack.getTag() != null) {
                        itemStack.getTag().remove("pages");
                        itemStack.getTag().remove("author");
                        itemStack.getTag().remove("title");
                    }
                    itemStack.setTag(new NBTTagCompound());
                }
                return new ExploitDetails(this.user, simpleName, "clicking in creative inventory without gamemode 1", false, true, "set \"allow-creative-inventory-click-without-gamemode\" to true in settings.yml");
            }
            ItemStack itemStack2 = null;
            try {
                final Field declaredField3 = PacketPlayInSetCreativeSlot.class.getDeclaredField("b");
                declaredField3.setAccessible(true);
                itemStack2 = (ItemStack)declaredField3.get(o);
            }
            catch (Exception ex4) {
                ex4.printStackTrace();
            }
            if (itemStack2 != null) {
                final ExploitDetails checkNbtTags2 = this.checkNbtTags(simpleName, itemStack2);
                if (checkNbtTags2 != null) {
                    return checkNbtTags2;
                }
                final org.bukkit.inventory.ItemStack bukkitCopy2 = CraftItemStack.asBukkitCopy(itemStack2);
                if ((bukkitCopy2.getType() == Material.CHEST || bukkitCopy2.getType() == Material.HOPPER) && bukkitCopy2.hasItemMeta() && bukkitCopy2.getItemMeta().toString().getBytes().length > 262144) {
                    return new ExploitDetails(this.user, simpleName, "too big chest data", false, false);
                }
            }
        }
        else if (o instanceof PacketPlayInBlockPlace) {
            if (Settings.IMP.DEBUG) {
                SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
            }
            ItemStack itemStack3 = ((PacketPlayInBlockPlace)o).getItemStack();
            if (itemStack3 == null) {
                try {
                    final Field declaredField4 = PacketPlayInBlockPlace.class.getDeclaredField("d");
                    declaredField4.setAccessible(true);
                    itemStack3 = (ItemStack)declaredField4.get(o);
                }
                catch (Exception ex5) {
                    ex5.printStackTrace();
                }
            }
            if (itemStack3 != null) {
                final Item item4 = itemStack3.getItem();
                if (item4 instanceof ItemWrittenBook || item4 instanceof ItemBookAndQuill) {
                    final LimitablePacket hasReachedLimit4 = this.hasReachedLimit("BOOK_Place");
                    if (hasReachedLimit4 != null) {
                        SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("Player {0} has reached ").append(hasReachedLimit4.getLimit()).append("/s book places (ppm), it is exploit probably. If it is false positive, please increase BOOK_Place limit in config.yml")), player.getName());
                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("too many book places (").append(hasReachedLimit4.getLimit()).append("/s)")), hasReachedLimit4.isCancelOnly(), false, "increase packet limit of BOOK_Place in config.yml");
                    }
                    this.lastBookplace = System.currentTimeMillis();
                    final org.bukkit.inventory.ItemStack itemInHand = player.getItemInHand();
                    if (itemInHand == null || (itemInHand.getType() != Material.WRITTEN_BOOK && itemInHand.getType() != Material.valueOf("BOOK_AND_QUILL") && Settings.IMP.PLACE_CHECKS.BLOCK_PLACING_BOOK_WHEN_NOT_HOLDING_IT)) {
                        if (itemStack3.getTag() != null) {
                            itemStack3.getTag().remove("pages");
                            itemStack3.getTag().remove("author");
                            itemStack3.getTag().remove("title");
                        }
                        return new ExploitDetails(this.user, simpleName, "placing book but not holding it", false, true, "set \"place-checks.block-placing-book-when-not-holding-it\" in settings.yml to false");
                    }
                }
                if (item4 instanceof ItemFireworks || item4 instanceof ItemFireworksCharge) {
                    final org.bukkit.inventory.ItemStack itemInHand2 = player.getItemInHand();
                    if ((itemInHand2 == null || (itemInHand2.getType() != Material.valueOf("FIREWORK_CHARGE") && itemInHand2.getType() != Material.valueOf("FIREWORK") && Settings.IMP.PLACE_CHECKS.BLOCK_PLACING_FIREWORK_WHEN_NOT_HOLDING_IT)) && itemStack3.getTag() != null && itemStack3.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                        return new ExploitDetails(this.user, simpleName, "placing firework but not holding it", false, true, "set \"place-checks.block-placing-firework-when-not-holding-it\" in settings.yml to false");
                    }
                }
                if (item4 instanceof ItemBanner) {
                    final org.bukkit.inventory.ItemStack itemInHand3 = player.getItemInHand();
                    if ((itemInHand3 == null || (!itemInHand3.getType().name().contains("BANNER") && Settings.IMP.PLACE_CHECKS.BLOCK_PLACING_BANNER_WHEN_NOT_HOLDING_IT)) && itemStack3.getTag() != null && itemStack3.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                        return new ExploitDetails(this.user, simpleName, "placing banner but not holding it", false, true, "set \"place-checks.block-placing-banner-when-not-holding-it\" in settings.yml to false");
                    }
                }
                final ExploitDetails checkNbtTags3 = this.checkNbtTags(simpleName, itemStack3);
                if (checkNbtTags3 != null) {
                    return checkNbtTags3;
                }
                final org.bukkit.inventory.ItemStack bukkitCopy3 = CraftItemStack.asBukkitCopy(itemStack3);
                if ((bukkitCopy3.getType() == Material.CHEST || bukkitCopy3.getType() == Material.HOPPER) && bukkitCopy3.hasItemMeta() && bukkitCopy3.getItemMeta().toString().getBytes().length > 262144) {
                    return new ExploitDetails(this.user, simpleName, "Too big chest data", false, false);
                }
            }
        }
        else if (o instanceof PacketPlayInTabComplete) {
            final PacketPlayInTabComplete packetPlayInTabComplete = (PacketPlayInTabComplete)o;
            if (this.lastTabComplete > System.currentTimeMillis()) {
                return new ExploitDetails(this.user, simpleName, "Too fast tab complete", true, false);
            }
            this.lastTabComplete = System.currentTimeMillis() + 500L;
        }
        else if (o instanceof PacketPlayInChat) {
            final String a = ((PacketPlayInChat)o).a();
            int n = 0;
            for (int i = 0; i < a.length(); ++i) {
                if (String.valueOf(a.charAt(i)).getBytes().length > 1 && ++n > 15) {
                    SpigotGuardLogger.log(Level.INFO, String.format("MessageValidator -> %s's message has been blocked! Message content: %s", player.getName(), a), new Object[0]);
                    player.sendMessage(String.valueOf(new StringBuilder().append(ChatColor.RED).append("Invalid chars in your message!")));
                    return new ExploitDetails(this.user, simpleName, "Invalid chars in message", true, false);
                }
            }
        }
        else if (o instanceof PacketPlayInCustomPayload) {
            final PacketPlayInCustomPayload packetPlayInCustomPayload = (PacketPlayInCustomPayload)o;
            final String a2 = packetPlayInCustomPayload.a();
            final PacketDataSerializer b2 = packetPlayInCustomPayload.b();
            if (((ByteBuf)b2).capacity() > Settings.IMP.PAYLOAD_SETTINGS.MAX_CAPACITY) {
                return new ExploitDetails(this.user, simpleName, "invalid bytebuf capacity", false, true, "set \"payload-settings.max-capacity\" to higher value in settings.yml");
            }
            if (Integer.valueOf(-296262810).equals(a2.toUpperCase().hashCode()) || Integer.valueOf(-295840999).equals(a2.toUpperCase().hashCode()) || Integer.valueOf(883258655).equals(a2.toUpperCase().hashCode()) || Integer.valueOf(883680466).equals(a2.toUpperCase().hashCode())) {
                if (Settings.IMP.DEBUG) {
                    SpigotGuardLogger.log(Level.INFO, "Received {0} from {1}", simpleName, player.getName());
                }
                final LimitablePacket hasReachedLimit5 = this.hasReachedLimit("BOOK_Edit");
                if (hasReachedLimit5 != null) {
                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("too many book edits/signs! (").append(hasReachedLimit5.getLimit()).append("/s)")), hasReachedLimit5.isCancelOnly(), false, "increase packet limit of BOOK_Edit in config.yml");
                }
                try {
                    final ItemStack j = new PacketDataSerializer(Unpooled.wrappedBuffer((ByteBuf)b2)).i();
                    if (j != null) {
                        if (System.currentTimeMillis() - this.lastBookplace > 60000L && Settings.IMP.PAYLOAD_SETTINGS.BLOCK_BOOK_SIGNING_WHEN_NO_BOOK_PLACED) {
                            return new ExploitDetails(this.user, simpleName, "book sign, but no book used", false, false, "set \"payload-settings.block-book-signing-when-no-book-placed\" in settings.yml to false");
                        }
                        if (Settings.IMP.PAYLOAD_SETTINGS.FAIL_WHEN_NOT_HOLDING_BOOK && !player.getInventory().contains(Material.valueOf("BOOK_AND_QUILL")) && !player.getInventory().contains(Material.WRITTEN_BOOK)) {
                            return new ExploitDetails(this.user, simpleName, "book interact, but no book exists in player's inventory", false, true, "set \"payload-settings.fail-when-not-holding-book\" in settings.yml to false");
                        }
                        final ExploitDetails checkNbtTags4 = this.checkNbtTags(simpleName, j);
                        if (checkNbtTags4 != null) {
                            return checkNbtTags4;
                        }
                    }
                }
                catch (Exception ex6) {
                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("exception: ").append(ex6.getMessage())), false, false);
                }
            }
            else if (a2.equals("REGISTER") || Integer.valueOf(1321107516).equals(a2.toUpperCase().hashCode()) || a2.toLowerCase().contains("fml")) {
                ByteBuf copy = null;
                try {
                    copy = ((ByteBuf)b2).copy();
                    if (copy.toString(StandardCharsets.UTF_8).split("\u0000").length > 124) {
                        return new ExploitDetails(this.user, simpleName, "too many channels", false, false);
                    }
                    if (copy != null) {
                        copy.release();
                    }
                }
                catch (Exception ex9) {
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
            else if (a2.equals("MC|ItemName") && player.getInventory() != null && player.getOpenInventory().getType() != InventoryType.ANVIL && Settings.IMP.BLOCK_ITEMNAME_WHEN_NO_ANVIL) {
                return new ExploitDetails(this.user, simpleName, "trying to use MC|ItemName but no anvil exists", false, false, "set \"block-itemname-when-no-anvil\" in settings.yml to false");
            }
        }
        else if (o instanceof PacketPlayInFlying) {
            final PacketPlayInFlying packetPlayInFlying = (PacketPlayInFlying)o;
            final double a3 = packetPlayInFlying.a();
            final double b3 = packetPlayInFlying.b();
            final double c = packetPlayInFlying.c();
            if ((a3 >= Double.MAX_VALUE || b3 > Double.MAX_VALUE || c >= Double.MAX_VALUE) && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V2) {
                return new ExploitDetails(this.user, simpleName, "Double.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v2\" to true");
            }
            if (a3 >= 2.147483647E9 || b3 > 2.147483647E9 || (c >= 2.147483647E9 && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V1)) {
                return new ExploitDetails(this.user, simpleName, "Integer.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v1\" to true");
            }
            final float d = packetPlayInFlying.d();
            final float e2 = packetPlayInFlying.e();
            if (d == Float.NEGATIVE_INFINITY || e2 == Float.NEGATIVE_INFINITY || d >= Float.MAX_VALUE || e2 >= Float.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "invalid float position", false, true);
            }
            if (o instanceof PacketPlayInFlying.PacketPlayInPosition) {
                System.out.println(String.valueOf(new StringBuilder().append("Player location1: ").append(player.getLocation().getX()).append(",").append(player.getLocation().getY()).append(",").append(player.getLocation().getZ()).append(" ||| Packet: ").append(a3).append(",").append(b3).append(",").append(c)));
            }
            if (o instanceof PacketPlayInFlying.PacketPlayInPositionLook) {
                System.out.println(String.valueOf(new StringBuilder().append("Player location2: ").append(player.getLocation().getX()).append(",").append(player.getLocation().getY()).append(",").append(player.getLocation().getZ()).append(" ||| Packet: ").append(a3).append(",").append(b3).append(",").append(c)));
            }
            if (this.lastY == 0.0) {
                this.lastY = b3;
            }
            if (this.lastZ == 0.0) {
                this.lastZ = c;
            }
            if (this.lastX == 0.0) {
                this.lastX = a3;
            }
            else if (c - this.lastZ == 9.0) {
                this.lastZ = 0.0;
                final LimitablePacket hasReachedLimit6 = this.hasReachedLimit("PacketPlayInPositionLook_InvalidMovement");
                if (hasReachedLimit6 != null) {
                    return new ExploitDetails(this.user, simpleName, "invalid Z movement", hasReachedLimit6.isCancelOnly(), false, "increase packet limit of PacketPlayInPositionLook_InvalidMovement in config.yml");
                }
            }
            else if (b3 - this.lastY == 9.0) {
                this.lastY = 0.0;
                final LimitablePacket hasReachedLimit7 = this.hasReachedLimit("PacketPlayInPositionLook_InvalidMovement");
                if (hasReachedLimit7 != null) {
                    return new ExploitDetails(this.user, simpleName, "invalid Y movement", hasReachedLimit7.isCancelOnly(), false, "increase packet limit of PacketPlayInPositionLook_InvalidMovement in config.yml");
                }
            }
            else if (a3 - this.lastX == 9.0) {
                this.lastX = 0.0;
                final LimitablePacket hasReachedLimit8 = this.hasReachedLimit("PacketPlayInPositionLook_InvalidMovement");
                if (hasReachedLimit8 != null) {
                    return new ExploitDetails(this.user, simpleName, "invalid X movement", hasReachedLimit8.isCancelOnly(), false, "increase packet limit of PacketPlayInPositionLook_InvalidMovement in config.yml");
                }
            }
            this.lastX = a3;
            this.lastY = b3;
            this.lastZ = c;
        }
        else if (o instanceof PacketPlayInHeldItemSlot) {
            final int a4 = ((PacketPlayInHeldItemSlot)o).a();
            if (a4 >= 36 || a4 < 0) {
                return new ExploitDetails(this.user, simpleName, "invalid held item slot", false, true);
            }
        }
        else if (o instanceof PacketPlayInSteerVehicle) {
            final PacketPlayInSteerVehicle packetPlayInSteerVehicle = (PacketPlayInSteerVehicle)o;
            if (packetPlayInSteerVehicle.b() >= Float.MAX_VALUE || packetPlayInSteerVehicle.a() >= Float.MAX_VALUE) {
                return new ExploitDetails(this.user, simpleName, "invalid vehicle movement", false, true);
            }
        }
        final LimitablePacket hasReachedLimit9 = this.hasReachedLimit(simpleName);
        if (hasReachedLimit9 != null) {
            return new ExploitDetails(this.user, simpleName, "packet limit exceed", hasReachedLimit9.isCancelOnly(), false, String.valueOf(new StringBuilder().append("increase packet limit of ").append(simpleName).append(" in config.yml or completely remove it")));
        }
        return null;
    }
    
    private LimitablePacket hasReachedLimit(final String s) {
        if (!PacketDecoder_1_8.LIMITATION.isLimitable(s)) {
            return null;
        }
        final LimitablePacket limit = PacketDecoder_1_8.LIMITATION.getLimit(s);
        if (this.user.increaseAndGetReceivedPackets(s) < limit.getLimit()) {
            return null;
        }
        return limit;
    }
    
    private void lambda$decode$0(final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)this.injector.getPlayer(), exploitDetails));
    }
}
