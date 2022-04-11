package xyz.yooniks.spigotguard.helper;

import org.bukkit.inventory.*;
import java.lang.reflect.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.*;
import java.util.*;
import net.md_5.bungee.chat.*;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import java.util.regex.*;

public final class NmsBookHelper
{
    private static final Constructor<?> nbtTagCompoundConstructor;
    private static final Method chatSerializerA;
    private static final Object[] hands;
    private static final Method craftItemStackAsNMSCopy;
    private static final String version;
    private static final Field craftMetaBookField;
    private static final Method craftPlayerGetHandle;
    private static final Method nmsItemStackSave;
    private static final Method craftMetaBookInternalAddPageMethod;
    private static final Method entityPlayerOpenBook;
    private static final boolean doubleHands;
    private static final Class<?> craftMetaBookClass;
    
    public static Object nmsCopy(final ItemStack itemStack) throws IllegalAccessException, InvocationTargetException {
        return NmsBookHelper.craftItemStackAsNMSCopy.invoke(null, itemStack);
    }
    
    public static void openBook(final Player player, final ItemStack itemStack, final boolean b) {
        try {
            if (NmsBookHelper.doubleHands) {
                NmsBookHelper.entityPlayerOpenBook.invoke(toNms(player), nmsCopy(itemStack), NmsBookHelper.hands[b]);
            }
            else {
                NmsBookHelper.entityPlayerOpenBook.invoke(toNms(player), nmsCopy(itemStack));
            }
        }
        catch (Exception ex) {
            throw new UnsupportedVersionException(ex);
        }
    }
    
    public static void setPages(final BookMeta bookMeta, final BaseComponent[][] array) {
        try {
            final List list = (List)NmsBookHelper.craftMetaBookField.get(bookMeta);
            if (list != null) {
                list.clear();
            }
            for (final BaseComponent[] array2 : array) {
                if (NmsBookHelper.craftMetaBookInternalAddPageMethod != null) {
                    NmsBookHelper.craftMetaBookInternalAddPageMethod.invoke(bookMeta, (array2 != null) ? ComponentSerializer.toString(array2) : "");
                }
                else {
                    list.add(NmsBookHelper.chatSerializerA.invoke(null, ComponentSerializer.toString((array2 != null) ? array2 : jsonToComponents(""))));
                }
            }
        }
        catch (Exception ex) {
            throw new UnsupportedVersionException(ex);
        }
    }
    
    private static String itemToJson(final ItemStack itemStack) {
        try {
            return NmsBookHelper.nmsItemStackSave.invoke(nmsCopy(itemStack), NmsBookHelper.nbtTagCompoundConstructor.newInstance(new Object[0])).toString();
        }
        catch (Exception ex) {
            throw new UnsupportedVersionException(ex);
        }
    }
    
    public static Class<?> getNmsClass(final String s) {
        return getNmsClass(s, false);
    }
    
    public static Class<?> getNmsClass(final String s, final boolean b) {
        try {
            return Class.forName(String.valueOf(new StringBuilder().append("net.minecraft.server.").append(NmsBookHelper.version).append(".").append(s)));
        }
        catch (ClassNotFoundException ex) {
            if (b) {
                throw new RuntimeException(String.valueOf(new StringBuilder().append("Cannot find NMS class ").append(s)), ex);
            }
            return null;
        }
    }
    
    private static Class<?> getCraftClass(final String s) {
        try {
            return Class.forName(String.valueOf(new StringBuilder().append("org.bukkit.craftbukkit.").append(NmsBookHelper.version).append(".").append(s)));
        }
        catch (ClassNotFoundException ex) {
            throw new RuntimeException(String.valueOf(new StringBuilder().append("Cannot find CraftBukkit class at path: ").append(s)), ex);
        }
    }
    
    static String access$000() {
        return NmsBookHelper.version;
    }
    
    public static BaseComponent[] itemToComponents(final ItemStack itemStack) {
        return jsonToComponents(itemToJson(itemStack));
    }
    
    public static BaseComponent[] jsonToComponents(final String s) {
        return new BaseComponent[] { (BaseComponent)new TextComponent(s) };
    }
    
    static {
        version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        final Matcher matcher = Pattern.compile("v([0-9]+)_([0-9]+)").matcher(NmsBookHelper.version);
        if (matcher.find()) {
            final int int1 = Integer.parseInt(matcher.group(1));
            final int int2 = Integer.parseInt(matcher.group(2));
            doubleHands = (int1 <= 1 && int2 >= 9);
            try {
                craftMetaBookClass = getCraftClass("inventory.CraftMetaBook");
                (craftMetaBookField = NmsBookHelper.craftMetaBookClass.getDeclaredField("pages")).setAccessible(true);
                Method declaredMethod = null;
                try {
                    declaredMethod = NmsBookHelper.craftMetaBookClass.getDeclaredMethod("internalAddPage", String.class);
                    declaredMethod.setAccessible(true);
                }
                catch (NoSuchMethodException ex2) {}
                craftMetaBookInternalAddPageMethod = declaredMethod;
                Class<?> clazz = getNmsClass("IChatBaseComponent$ChatSerializer", false);
                if (clazz == null) {
                    clazz = getNmsClass("ChatSerializer");
                }
                chatSerializerA = clazz.getDeclaredMethod("a", String.class);
                craftPlayerGetHandle = getCraftClass("entity.CraftPlayer").getMethod("getHandle", (Class<?>[])new Class[0]);
                final Class<?> nmsClass = getNmsClass("EntityPlayer");
                final Class<?> nmsClass2 = getNmsClass("ItemStack");
                if (NmsBookHelper.doubleHands) {
                    final Class<?> nmsClass3 = getNmsClass("EnumHand");
                    Method entityPlayerOpenBook2;
                    try {
                        entityPlayerOpenBook2 = nmsClass.getMethod("a", nmsClass2, nmsClass3);
                    }
                    catch (NoSuchMethodException ex3) {
                        entityPlayerOpenBook2 = nmsClass.getMethod("openBook", nmsClass2, nmsClass3);
                    }
                    entityPlayerOpenBook = entityPlayerOpenBook2;
                    hands = (Object[])nmsClass3.getEnumConstants();
                }
                else {
                    entityPlayerOpenBook = nmsClass.getMethod("openBook", nmsClass2);
                    hands = null;
                }
                craftItemStackAsNMSCopy = getCraftClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class);
                final Class<?> nmsClass4 = getNmsClass("ItemStack");
                final Class<?> nmsClass5 = getNmsClass("NBTTagCompound");
                nmsItemStackSave = nmsClass4.getMethod("save", nmsClass5);
                nbtTagCompoundConstructor = nmsClass5.getConstructor((Class<?>[])new Class[0]);
            }
            catch (Exception ex) {
                throw new IllegalStateException(String.valueOf(new StringBuilder().append("Cannot initiate reflections for ").append(NmsBookHelper.version)), ex);
            }
            return;
        }
        throw new IllegalStateException(String.valueOf(new StringBuilder().append("Cannot parse version \"").append(NmsBookHelper.version).append("\", make sure it follows \"v<major>_<minor>...\"")));
    }
    
    public static Object toNms(final Player player) throws InvocationTargetException, IllegalAccessException {
        return NmsBookHelper.craftPlayerGetHandle.invoke(player, new Object[0]);
    }
    
    public static class UnsupportedVersionException extends RuntimeException
    {
        private final String version;
        
        public String getVersion() {
            return this.version;
        }
        
        public UnsupportedVersionException(final Exception ex) {
            super(String.valueOf(new StringBuilder().append("Error while executing reflections, submit to developers the following log (version: ").append(NmsBookHelper.access$000()).append(")")), ex);
            this.version = NmsBookHelper.access$000();
        }
    }
}
