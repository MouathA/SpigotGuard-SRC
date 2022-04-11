package xyz.yooniks.spigotguard.helper;

import java.util.*;
import org.yaml.snakeyaml.external.biz.base64Coder.*;
import org.bukkit.util.io.*;
import java.io.*;

public final class SerializerHelper
{
    public static String toString(final List<?> list) throws IllegalStateException {
        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream((OutputStream)byteArrayOutputStream);
            bukkitObjectOutputStream.writeObject((Object)list);
            final String encodeLines = Base64Coder.encodeLines(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.close();
            bukkitObjectOutputStream.close();
            return encodeLines;
        }
        catch (Exception ex) {
            throw new IllegalStateException("Unable to save itemstack array", ex);
        }
    }
    
    private SerializerHelper() {
    }
    
    public static List<?> fromString(final String s) throws IOException {
        try {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64Coder.decodeLines(s));
            final BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream((InputStream)byteArrayInputStream);
            final List list = (List)bukkitObjectInputStream.readObject();
            byteArrayInputStream.close();
            bukkitObjectInputStream.close();
            return (List<?>)list;
        }
        catch (ClassNotFoundException ex) {
            throw new IOException("Unable to read class type", ex);
        }
    }
}
