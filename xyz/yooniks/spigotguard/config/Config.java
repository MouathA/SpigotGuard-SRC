package xyz.yooniks.spigotguard.config;

import java.util.logging.*;
import xyz.yooniks.spigotguard.logger.*;
import org.bukkit.configuration.*;
import java.nio.charset.*;
import org.bukkit.configuration.file.*;
import java.io.*;
import java.lang.reflect.*;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.lang.annotation.*;

public class Config
{
    private Field getField(final String[] array, final Object o) {
        try {
            final Field field = o.getClass().getField(this.toFieldName(array[array.length - 1]));
            this.setAccessible(field);
            return field;
        }
        catch (Exception ex) {
            SpigotGuardLogger.log(Level.WARNING, "Invalid config field: {0} for {1}", String.join(".", (CharSequence[])array), this.toNodeName(o.getClass().getSimpleName()));
            return null;
        }
    }
    
    public Config() {
        this.save(new ArrayList<String>(), this.getClass(), this, 0);
    }
    
    public void set(final ConfigurationSection configurationSection, final String s) {
        for (final String s2 : configurationSection.getKeys(false)) {
            final Object value = configurationSection.get(s2);
            final String value2 = String.valueOf(new StringBuilder().append(s).append(s.isEmpty() ? "" : ".").append(s2));
            if (value instanceof ConfigurationSection) {
                this.set((ConfigurationSection)value, value2);
            }
            else {
                this.set(value2, value);
            }
        }
    }
    
    private void set(final String s, Object value) {
        final String[] split = s.split("\\.");
        final Object instance = this.getInstance(split, this.getClass());
        if (instance != null) {
            final Field field = this.getField(split, instance);
            if (field != null) {
                try {
                    if (field.getAnnotation(Final.class) != null) {
                        return;
                    }
                    if (field.getType() == String.class && !(value instanceof String)) {
                        value = String.valueOf(new StringBuilder().append(value).append(""));
                    }
                    field.set(instance, value);
                    return;
                }
                catch (IllegalAccessException ex) {}
                catch (IllegalArgumentException ex2) {}
            }
        }
        SpigotGuardLogger.log(Level.WARNING, "Failed to set config option: {0}: {1} | {2} ", s, value, instance);
    }
    
    public boolean load(final File file) {
        if (!file.exists()) {
            return false;
        }
        YamlConfiguration loadConfiguration;
        try {
            final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            try {
                loadConfiguration = YamlConfiguration.loadConfiguration((Reader)inputStreamReader);
                inputStreamReader.close();
            }
            catch (Throwable t) {
                try {
                    inputStreamReader.close();
                }
                catch (Throwable t2) {
                    t.addSuppressed(t2);
                }
                throw t;
            }
        }
        catch (IOException ex) {
            SpigotGuardLogger.exception("Unable to load config.", ex);
            return false;
        }
        this.set((ConfigurationSection)loadConfiguration, "");
        return true;
    }
    
    private String repeat(final String s, final int n) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            sb.append(s);
        }
        return String.valueOf(sb);
    }
    
    private String toNodeName(final String s) {
        return s.toLowerCase().replace("_", "-");
    }
    
    private void save(final List<String> list, final Class clazz, final Object o, final int n) {
        try {
            final String repeat = this.repeat(" ", n);
            for (final Field accessible : clazz.getFields()) {
                if (accessible.getAnnotation(Ignore.class) == null) {
                    final Class<?> type = accessible.getType();
                    if (accessible.getAnnotation(Ignore.class) == null) {
                        final Comment comment = accessible.getAnnotation(Comment.class);
                        if (comment != null) {
                            final String[] value = comment.value();
                            for (int length2 = value.length, j = 0; j < length2; ++j) {
                                list.add(String.valueOf(new StringBuilder().append(repeat).append("# ").append(value[j])));
                            }
                        }
                        if (accessible.getAnnotation(Create.class) != null) {
                            Object o2 = accessible.get(o);
                            this.setAccessible(accessible);
                            if (n == 0) {
                                list.add("");
                            }
                            final Comment comment2 = type.getAnnotation(Comment.class);
                            if (comment2 != null) {
                                final String[] value2 = comment2.value();
                                for (int length3 = value2.length, k = 0; k < length3; ++k) {
                                    list.add(String.valueOf(new StringBuilder().append(repeat).append("# ").append(value2[k])));
                                }
                            }
                            list.add(String.valueOf(new StringBuilder().append(repeat).append(this.toNodeName(type.getSimpleName())).append(":")));
                            if (o2 == null) {
                                accessible.set(o, o2 = type.newInstance());
                            }
                            this.save(list, type, o2, n + 2);
                        }
                        else {
                            list.add(String.valueOf(new StringBuilder().append(repeat).append(this.toNodeName(String.valueOf(new StringBuilder().append(accessible.getName()).append(": ")))).append(this.toYamlString(accessible.get(o), repeat))));
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            SpigotGuardLogger.exception("Error:", ex);
        }
    }
    
    private String toFieldName(final String s) {
        return s.toUpperCase().replaceAll("-", "_");
    }
    
    private void setAccessible(final Field field) throws Exception {
        field.setAccessible(true);
        field.setAccessible(true);
        final int modifiers = field.getModifiers();
        if (Modifier.isFinal(modifiers)) {
            try {
                final Field declaredField = Field.class.getDeclaredField("modifiers");
                declaredField.setAccessible(true);
                declaredField.setInt(field, modifiers & 0xFFFFFFEF);
            }
            catch (NoSuchFieldException ex) {
                final Method declaredMethod = Class.class.getDeclaredMethod("getDeclaredFields0", Boolean.TYPE);
                declaredMethod.setAccessible(true);
                Block_5: {
                    for (final Field field2 : (Field[])declaredMethod.invoke(Field.class, false)) {
                        if ("modifiers".equals(field2.getName())) {
                            break Block_5;
                        }
                    }
                    return;
                }
                final Field field2;
                field2.setAccessible(true);
                field2.set(field, modifiers & 0xFFFFFFEF);
            }
        }
    }
    
    private Object getInstance(String[] array, final Class clazz) {
        try {
            Class<?> clazz2 = (clazz == null) ? MethodHandles.lookup().lookupClass() : clazz;
            Object o = this;
        Label_0212:
            while (array.length > 0) {
                switch (array.length) {
                    case 1: {
                        return o;
                    }
                    default: {
                        Class<Config> clazz3 = null;
                        for (final Class clazz4 : clazz2.getDeclaredClasses()) {
                            if (Integer.valueOf(this.toFieldName(array[0]).toUpperCase().hashCode()).equals(clazz4.getSimpleName().toUpperCase().hashCode())) {
                                clazz3 = (Class<Config>)clazz4;
                                break;
                            }
                        }
                        try {
                            final Field declaredField = clazz2.getDeclaredField(this.toFieldName(array[0]));
                            this.setAccessible(declaredField);
                            Object o2 = declaredField.get(o);
                            if (o2 == null) {
                                o2 = clazz3.newInstance();
                                declaredField.set(o, o2);
                            }
                            clazz2 = clazz3;
                            o = o2;
                            array = Arrays.copyOfRange(array, 1, array.length);
                            continue;
                        }
                        catch (Exception ex2) {
                            return null;
                        }
                        break Label_0212;
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public void save(final File file) {
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            final Path path = file.toPath();
            final Path path2 = new File(file.getParentFile(), "__tmpcfg").toPath();
            final ArrayList<String> list = new ArrayList<String>();
            this.save(list, this.getClass(), this, 0);
            Files.write(path2, list, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            try {
                Files.move(path2, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (AtomicMoveNotSupportedException ex2) {
                Files.move(path2, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException ex) {
            SpigotGuardLogger.exception("Error: ", ex);
        }
    }
    
    private String toYamlString(final Object o, final String s) {
        if (o instanceof List) {
            final Collection collection = (Collection)o;
            if (collection.isEmpty()) {
                return "[]";
            }
            final StringBuilder sb = new StringBuilder();
            final Iterator<Object> iterator = collection.iterator();
            while (iterator.hasNext()) {
                sb.append(System.lineSeparator()).append(s).append("- ").append(this.toYamlString(iterator.next(), s));
            }
            return String.valueOf(sb);
        }
        else {
            if (!(o instanceof String)) {
                return (o != null) ? o.toString() : "null";
            }
            final String s2 = (String)o;
            if (s2.isEmpty()) {
                return "''";
            }
            return String.valueOf(new StringBuilder().append("\"").append(s2).append("\""));
        }
    }
    
    @Target({ ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Create {
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    public @interface Final {
    }
    
    @Target({ ElementType.FIELD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Comment {
        String[] value();
    }
    
    @Target({ ElementType.FIELD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ignore {
    }
}
