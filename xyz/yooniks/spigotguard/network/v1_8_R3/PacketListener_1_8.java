package xyz.yooniks.spigotguard.network.v1_8_R3;

import xyz.yooniks.spigotguard.user.*;
import xyz.yooniks.spigotguard.network.*;
import xyz.yooniks.spigotguard.config.*;
import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import org.bukkit.event.inventory.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.*;
import io.netty.buffer.*;
import java.nio.charset.*;
import org.bukkit.entity.*;
import java.lang.reflect.*;
import org.bukkit.inventory.*;
import io.netty.util.concurrent.*;
import xyz.yooniks.spigotguard.*;
import org.bukkit.plugin.*;
import io.netty.channel.*;
import xyz.yooniks.spigotguard.helper.*;
import net.minecraft.server.v1_8_R3.*;
import java.util.regex.*;
import java.util.*;
import xyz.yooniks.spigotguard.event.*;
import org.bukkit.*;
import org.bukkit.event.*;

@ChannelHandler.Sharable
public class PacketListener_1_8 extends ChannelDuplexHandler
{
    private final User user;
    private long lastBookplace;
    private boolean disconnected;
    private static final Pattern URL_MATCHER;
    private static final String BLOCKED;
    private final PacketInjector injector;
    
    private ExploitDetails failure(final Object o) {
        final Player player = this.injector.getPlayer();
        if (o != null && Settings.IMP.DEBUG) {
            SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append("{0} received ").append(o.getClass().getSimpleName()).append(" -> ").append(o.toString())), new Object[0]);
        }
        final String simpleName = o.getClass().getSimpleName();
        if (o instanceof PacketPlayInWindowClick) {
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
                        if (e != null && b > 0 && openInventory.getType() == InventoryType.PLAYER) {
                            try {
                                final org.bukkit.inventory.ItemStack item = openInventory.getItem(b);
                                final Item item2 = e.getItem();
                                if (item2 instanceof ItemWrittenBook || item2 instanceof ItemBookAndQuill) {
                                    e.getTag().remove("pages");
                                    e.getTag().remove("author");
                                    e.getTag().remove("title");
                                    if (item != null && item.getType() != Material.valueOf("BOOK_AND_QUILL") && item.getType() != Material.WRITTEN_BOOK && e.getTag() != null && e.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                                        return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("tried to use book but real item is ").append(item.getType().name())), false, true, "increase \"block-fake-packets-only-when-items-nbt-bigger-than\" value in settings.yml");
                                    }
                                }
                                if ((item2 instanceof ItemFireworks || item2 instanceof ItemFireworksCharge) && item != null && item.getType() != Material.valueOf("FIREWORK") && item.getType() != Material.valueOf("FIREWORK_CHARGE") && e.getTag() != null && e.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("tried to use firework but real item is ").append(item.getType().name())), false, true, "increase \"block-fake-packets-only-when-items-nbt-bigger-than\" value in settings.yml");
                                }
                                if (item2 instanceof ItemBanner && item != null && !item.getType().name().contains("BANNER") && e.getTag() != null && e.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("tried to use banner but real item is ").append(item.getType().name())), false, true, "increase \"block-fake-packets-only-when-items-nbt-bigger-than\" value in settings.yml");
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
                final Item item3 = itemStack3.getItem();
                if (item3 instanceof ItemWrittenBook || item3 instanceof ItemBookAndQuill) {
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
                if (item3 instanceof ItemFireworks || item3 instanceof ItemFireworksCharge) {
                    final org.bukkit.inventory.ItemStack itemInHand2 = player.getItemInHand();
                    if ((itemInHand2 == null || (itemInHand2.getType() != Material.valueOf("FIREWORK_CHARGE") && itemInHand2.getType() != Material.valueOf("FIREWORK") && Settings.IMP.PLACE_CHECKS.BLOCK_PLACING_FIREWORK_WHEN_NOT_HOLDING_IT)) && itemStack3.getTag() != null && itemStack3.getTag().toString().length() > Settings.IMP.BLOCK_FAKE_PACKETS_ONLY_WHEN_ITEMS_NBT_BIGGER_THAN) {
                        return new ExploitDetails(this.user, simpleName, "placing firework but not holding it", false, true, "set \"place-checks.block-placing-firework-when-not-holding-it\" in settings.yml to false");
                    }
                }
                if (item3 instanceof ItemBanner) {
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
                    return null;
                }
                catch (Exception ex6) {
                    return new ExploitDetails(this.user, simpleName, String.valueOf(new StringBuilder().append("exception: ").append(ex6.getMessage())), false, false);
                }
            }
            if (a.equals("REGISTER") || Integer.valueOf(1321107516).equals(a.toUpperCase().hashCode()) || a.toLowerCase().contains("fml")) {
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
            if ((a2 >= Double.MAX_VALUE || b3 > Double.MAX_VALUE || c >= Double.MAX_VALUE) && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V1) {
                return new ExploitDetails(this.user, simpleName, "Double.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v2\" to true");
            }
            if (a2 >= 2.147483647E9 || b3 > 2.147483647E9 || (c >= 2.147483647E9 && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V1)) {
                return new ExploitDetails(this.user, simpleName, "Integer.MAX_VALUE position", false, true, "set \"position-checks.allow-invalid-movement-v1\" to true");
            }
        }
        else if (o instanceof PacketPlayInFlying) {
            final PacketPlayInFlying packetPlayInFlying = (PacketPlayInFlying)o;
            final double a3 = packetPlayInFlying.a();
            final double b4 = packetPlayInFlying.b();
            final double c2 = packetPlayInFlying.c();
            if ((a3 >= Double.MAX_VALUE || b4 > Double.MAX_VALUE || c2 >= Double.MAX_VALUE) && !Settings.IMP.POSITION_CHECKS.ALLOW_INVALID_MOVEMENT_V1) {
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
        return null;
    }
    
    private void lambda$channelRead$1(final ExploitDetails exploitDetails, final Future future) throws Exception {
        Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$channelRead$0);
    }
    
    private void lambda$channelRead$2() {
        this.injector.getPlayer().kickPlayer(MessageBuilder.newBuilder(Settings.IMP.KICK.MESSAGE).stripped().coloured().toString());
    }
    
    public PacketListener_1_8(final PacketInjector injector, final User user) {
        this.disconnected = false;
        this.lastBookplace = -1L;
        this.injector = injector;
        this.user = user;
    }
    
    static {
        BLOCKED = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
        URL_MATCHER = Pattern.compile("url");
    }
    
    public void channelRead(final ChannelHandlerContext channelHandlerContext, final Object o) throws Exception {
        if (this.disconnected) {
            return;
        }
        final ExploitDetails failure = this.failure(o);
        if (failure == null) {
            super.channelRead(channelHandlerContext, o);
            return;
        }
        if (failure.isCancelOnly()) {
            return;
        }
        this.disconnected = true;
        if (Settings.IMP.KICK.TYPE == 0) {
            channelHandlerContext.close().addListener(this::lambda$channelRead$1);
        }
        else {
            Bukkit.getScheduler().runTask((Plugin)SpigotGuardPlugin.getInstance(), this::lambda$channelRead$2);
        }
        SpigotGuardLogger.log(Level.INFO, String.valueOf(new StringBuilder().append(SpigotGuardPlugin.getInstance().getSpigotGuardClassLoaded().getSerializable()).append(" Player {0} tried to crash the server, packet: {1}, details: {2}")), this.injector.getPlayer().getName(), o.getClass().getSimpleName(), failure.getDetails());
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
        if ((item instanceof ItemWrittenBook || item instanceof ItemBookAndQuill) && Settings.IMP.BOOK.FAST_CHECK && String.valueOf(tag.get("pages")).length() > Settings.IMP.BOOK.FAST_CHECK_MAX_LENGTH) {
            tag.remove("pages");
            tag.remove("author");
            tag.remove("title");
            itemStack.setTag(new NBTTagCompound());
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
                            final Matcher matcher = PacketListener_1_8.URL_MATCHER.matcher(lowerCase2);
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
    
    private void lambda$channelRead$0(final ExploitDetails exploitDetails) {
        Bukkit.getPluginManager().callEvent((Event)new ExploitDetectedEvent((OfflinePlayer)this.injector.getPlayer(), exploitDetails));
    }
}
