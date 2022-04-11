package org.json;

import java.util.regex.*;
import java.math.*;
import java.lang.annotation.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class JSONObject
{
    public static final Object NULL;
    static final Pattern NUMBER_PATTERN;
    private final Map<String, Object> map;
    
    public JSONArray optJSONArray(final String s) {
        final Object opt = this.opt(s);
        return (opt instanceof JSONArray) ? ((JSONArray)opt) : null;
    }
    
    public static Object wrap(final Object o) {
        try {
            if (JSONObject.NULL.equals(o)) {
                return JSONObject.NULL;
            }
            if (o instanceof JSONObject || o instanceof JSONArray || JSONObject.NULL.equals(o) || o instanceof JSONString || o instanceof Byte || o instanceof Character || o instanceof Short || o instanceof Integer || o instanceof Long || o instanceof Boolean || o instanceof Float || o instanceof Double || o instanceof String || o instanceof BigInteger || o instanceof BigDecimal || o instanceof Enum) {
                return o;
            }
            if (o instanceof Collection) {
                return new JSONArray((Collection<?>)o);
            }
            if (o.getClass().isArray()) {
                return new JSONArray(o);
            }
            if (o instanceof Map) {
                return new JSONObject((Map<?, ?>)o);
            }
            final Package package1 = o.getClass().getPackage();
            final String s = (package1 != null) ? package1.getName() : "";
            if (s.startsWith("java.") || s.startsWith("javax.") || o.getClass().getClassLoader() == null) {
                return o.toString();
            }
            return new JSONObject(o);
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public BigInteger getBigInteger(final String s) throws JSONException {
        final Object value = this.get(s);
        final BigInteger objectToBigInteger = objectToBigInteger(value, null);
        if (objectToBigInteger != null) {
            return objectToBigInteger;
        }
        throw wrongValueFormatException(s, "BigInteger", value, null);
    }
    
    public BigDecimal optBigDecimal(final String s, final BigDecimal bigDecimal) {
        return objectToBigDecimal(this.opt(s), bigDecimal);
    }
    
    public BigInteger optBigInteger(final String s, final BigInteger bigInteger) {
        return objectToBigInteger(this.opt(s), bigInteger);
    }
    
    public JSONObject(final String s) throws JSONException {
        this(new JSONTokener(s));
    }
    
    public JSONObject getJSONObject(final String s) throws JSONException {
        final Object value = this.get(s);
        if (value instanceof JSONObject) {
            return (JSONObject)value;
        }
        throw wrongValueFormatException(s, "JSONObject", null);
    }
    
    public float optFloat(final String s, final float n) {
        final Number optNumber = this.optNumber(s);
        if (optNumber == null) {
            return n;
        }
        return optNumber.floatValue();
    }
    
    public <E extends Enum<E>> E optEnum(final Class<E> clazz, final String s, final E e) {
        try {
            final Object opt = this.opt(s);
            if (JSONObject.NULL.equals(opt)) {
                return e;
            }
            if (clazz.isAssignableFrom(((Enum<E>)opt).getClass())) {
                return (E)opt;
            }
            return Enum.valueOf(clazz, opt.toString());
        }
        catch (IllegalArgumentException ex) {
            return e;
        }
        catch (NullPointerException ex2) {
            return e;
        }
    }
    
    public JSONObject putOpt(final String s, final Object o) throws JSONException {
        if (s != null && o != null) {
            return this.put(s, o);
        }
        return this;
    }
    
    public Object query(final JSONPointer jsonPointer) {
        return jsonPointer.queryFrom(this);
    }
    
    public String toString(final int n) throws JSONException {
        final StringWriter stringWriter = new StringWriter();
        synchronized (stringWriter.getBuffer()) {
            return this.write(stringWriter, n, 0).toString();
        }
    }
    
    public float getFloat(final String s) throws JSONException {
        final Object value = this.get(s);
        if (value instanceof Number) {
            return ((Number)value).floatValue();
        }
        try {
            return Float.parseFloat(value.toString());
        }
        catch (Exception ex) {
            throw wrongValueFormatException(s, "float", ex);
        }
    }
    
    private static int getAnnotationDepth(final Method method, final Class<? extends Annotation> clazz) {
        if (method == null || clazz == null) {
            return -1;
        }
        if (method.isAnnotationPresent(clazz)) {
            return 1;
        }
        final Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.getSuperclass() == null) {
            return -1;
        }
        for (final Class clazz2 : declaringClass.getInterfaces()) {
            try {
                final int annotationDepth = getAnnotationDepth(clazz2.getMethod(method.getName(), (Class[])method.getParameterTypes()), clazz);
                if (annotationDepth > 0) {
                    return annotationDepth + 1;
                }
            }
            catch (SecurityException ex) {}
            catch (NoSuchMethodException ex2) {}
        }
        try {
            final int annotationDepth2 = getAnnotationDepth(declaringClass.getSuperclass().getMethod(method.getName(), method.getParameterTypes()), clazz);
            if (annotationDepth2 > 0) {
                return annotationDepth2 + 1;
            }
            return -1;
        }
        catch (SecurityException ex3) {
            return -1;
        }
        catch (NoSuchMethodException ex4) {
            return -1;
        }
    }
    
    public Object remove(final String s) {
        return this.map.remove(s);
    }
    
    public <E extends Enum<E>> E getEnum(final Class<E> clazz, final String s) throws JSONException {
        final Enum optEnum = this.optEnum((Class<Enum>)clazz, s);
        if (optEnum == null) {
            throw wrongValueFormatException(s, String.valueOf(new StringBuilder().append("enum of type ").append(quote(clazz.getSimpleName()))), null);
        }
        return (E)optEnum;
    }
    
    public static Writer quote(final String s, final Writer writer) throws IOException {
        if (s == null || s.isEmpty()) {
            writer.write("\"\"");
            return writer;
        }
        int char1 = 0;
        final int length = s.length();
        writer.write(34);
        for (int i = 0; i < length; ++i) {
            final int n = char1;
            char1 = s.charAt(i);
            switch (char1) {
                case 34:
                case 92: {
                    writer.write(92);
                    writer.write(char1);
                    break;
                }
                case 47: {
                    if (n == 60) {
                        writer.write(92);
                    }
                    writer.write(char1);
                    break;
                }
                case 8: {
                    writer.write("\\b");
                    break;
                }
                case 9: {
                    writer.write("\\t");
                    break;
                }
                case 10: {
                    writer.write("\\n");
                    break;
                }
                case 12: {
                    writer.write("\\f");
                    break;
                }
                case 13: {
                    writer.write("\\r");
                    break;
                }
                default: {
                    if (char1 < 32 || (char1 >= 128 && char1 < 160) || (char1 >= 8192 && char1 < 8448)) {
                        writer.write("\\u");
                        final String hexString = Integer.toHexString(char1);
                        writer.write("0000", 0, 4 - hexString.length());
                        writer.write(hexString);
                        break;
                    }
                    writer.write(char1);
                    break;
                }
            }
        }
        writer.write(34);
        return writer;
    }
    
    public boolean isNull(final String s) {
        return JSONObject.NULL.equals(this.opt(s));
    }
    
    public JSONObject put(final String s, final long n) throws JSONException {
        return this.put(s, (Object)n);
    }
    
    public Object optQuery(final JSONPointer jsonPointer) {
        try {
            return jsonPointer.queryFrom(this);
        }
        catch (JSONPointerException ex) {
            return null;
        }
    }
    
    private void populateMap(final Object o) {
        final Class<?> class1 = o.getClass();
        for (final Method method : (class1.getClassLoader() != null) ? class1.getMethods() : class1.getDeclaredMethods()) {
            final int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && method.getParameterTypes().length == 0 && !method.isBridge() && method.getReturnType() != Void.TYPE && isValidMethodName(method.getName())) {
                final String keyNameFromMethod = getKeyNameFromMethod(method);
                if (keyNameFromMethod != null && !keyNameFromMethod.isEmpty()) {
                    try {
                        final Object invoke = method.invoke(o, new Object[0]);
                        if (invoke != null) {
                            this.map.put(keyNameFromMethod, wrap(invoke));
                            if (invoke instanceof Closeable) {
                                try {
                                    ((Closeable)invoke).close();
                                }
                                catch (IOException ex) {}
                            }
                        }
                    }
                    catch (IllegalAccessException ex2) {}
                    catch (IllegalArgumentException ex3) {}
                    catch (InvocationTargetException ex4) {}
                }
            }
        }
    }
    
    public JSONObject put(final String s, final double n) throws JSONException {
        return this.put(s, (Object)n);
    }
    
    public JSONArray names() {
        if (this.map.isEmpty()) {
            return null;
        }
        return new JSONArray(this.map.keySet());
    }
    
    protected static Number stringToNumber(final String s) throws NumberFormatException {
        final char char1 = s.charAt(0);
        if ((char1 < '0' || char1 > '9') && char1 != '-') {
            throw new NumberFormatException(String.valueOf(new StringBuilder().append("val [").append(s).append("] is not a valid number.")));
        }
        if (isDecimalNotation(s)) {
            try {
                final BigDecimal bigDecimal = new BigDecimal(s);
                if (char1 == '-' && BigDecimal.ZERO.compareTo(bigDecimal) == 0) {
                    return -0.0;
                }
                return bigDecimal;
            }
            catch (NumberFormatException ex) {
                try {
                    final Double value = Double.valueOf(s);
                    if (value.isNaN() || value.isInfinite()) {
                        throw new NumberFormatException(String.valueOf(new StringBuilder().append("val [").append(s).append("] is not a valid number.")));
                    }
                    return value;
                }
                catch (NumberFormatException ex2) {
                    throw new NumberFormatException(String.valueOf(new StringBuilder().append("val [").append(s).append("] is not a valid number.")));
                }
            }
        }
        if (char1 == '0' && s.length() > 1) {
            final char char2 = s.charAt(1);
            if (char2 >= '0' && char2 <= '9') {
                throw new NumberFormatException(String.valueOf(new StringBuilder().append("val [").append(s).append("] is not a valid number.")));
            }
        }
        else if (char1 == '-' && s.length() > 2) {
            final char char3 = s.charAt(1);
            final char char4 = s.charAt(2);
            if (char3 == '0' && char4 >= '0' && char4 <= '9') {
                throw new NumberFormatException(String.valueOf(new StringBuilder().append("val [").append(s).append("] is not a valid number.")));
            }
        }
        final BigInteger bigInteger = new BigInteger(s);
        if (bigInteger.bitLength() <= 31) {
            return bigInteger.intValue();
        }
        if (bigInteger.bitLength() <= 63) {
            return bigInteger.longValue();
        }
        return bigInteger;
    }
    
    public Map<String, Object> toMap() {
        final HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
        for (final Map.Entry<String, Object> entry : this.entrySet()) {
            Object o;
            if (entry.getValue() == null || JSONObject.NULL.equals(entry.getValue())) {
                o = null;
            }
            else if (entry.getValue() instanceof JSONObject) {
                o = entry.getValue().toMap();
            }
            else if (entry.getValue() instanceof JSONArray) {
                o = entry.getValue().toList();
            }
            else {
                o = entry.getValue();
            }
            hashMap.put(entry.getKey(), o);
        }
        return (Map<String, Object>)hashMap;
    }
    
    public JSONObject optJSONObject(final String s) {
        final Object opt = this.opt(s);
        return (opt instanceof JSONObject) ? ((JSONObject)opt) : null;
    }
    
    protected static boolean isDecimalNotation(final String s) {
        return s.indexOf(46) > -1 || s.indexOf(101) > -1 || s.indexOf(69) > -1 || "-0".equals(s);
    }
    
    static {
        NUMBER_PATTERN = Pattern.compile("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?");
        NULL = new Null(null);
    }
    
    public JSONObject(final JSONTokener jsonTokener) throws JSONException {
        this();
        if (jsonTokener.nextClean() != '{') {
            throw jsonTokener.syntaxError("A JSONObject text must begin with '{'");
        }
        while (true) {
            switch (jsonTokener.nextClean()) {
                case '\0': {
                    throw jsonTokener.syntaxError("A JSONObject text must end with '}'");
                }
                case '}': {}
                default: {
                    jsonTokener.back();
                    final String string = jsonTokener.nextValue().toString();
                    if (jsonTokener.nextClean() != ':') {
                        throw jsonTokener.syntaxError("Expected a ':' after a key");
                    }
                    if (string != null) {
                        if (this.opt(string) != null) {
                            throw jsonTokener.syntaxError(String.valueOf(new StringBuilder().append("Duplicate key \"").append(string).append("\"")));
                        }
                        final Object nextValue = jsonTokener.nextValue();
                        if (nextValue != null) {
                            this.put(string, nextValue);
                        }
                    }
                    switch (jsonTokener.nextClean()) {
                        case ',':
                        case ';': {
                            if (jsonTokener.nextClean() == '}') {
                                return;
                            }
                            jsonTokener.back();
                            continue;
                        }
                        case '}': {
                            return;
                        }
                        default: {
                            throw jsonTokener.syntaxError("Expected a ',' or '}'");
                        }
                    }
                    break;
                }
            }
        }
    }
    
    public int getInt(final String s) throws JSONException {
        final Object value = this.get(s);
        if (value instanceof Number) {
            return ((Number)value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        }
        catch (Exception ex) {
            throw wrongValueFormatException(s, "int", ex);
        }
    }
    
    static BigDecimal objectToBigDecimal(final Object o, final BigDecimal bigDecimal) {
        if (JSONObject.NULL.equals(o)) {
            return bigDecimal;
        }
        if (o instanceof BigDecimal) {
            return (BigDecimal)o;
        }
        if (o instanceof BigInteger) {
            return new BigDecimal((BigInteger)o);
        }
        if (o instanceof Double || o instanceof Float) {
            if (!numberIsFinite((Number)o)) {
                return bigDecimal;
            }
            return new BigDecimal(((Number)o).doubleValue());
        }
        else {
            if (o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte) {
                return new BigDecimal(((Number)o).longValue());
            }
            try {
                return new BigDecimal(o.toString());
            }
            catch (Exception ex) {
                return bigDecimal;
            }
        }
    }
    
    public Object opt(final String s) {
        return (s == null) ? null : this.map.get(s);
    }
    
    public long optLong(final String s, final long n) {
        final Number optNumber = this.optNumber(s, null);
        if (optNumber == null) {
            return n;
        }
        return optNumber.longValue();
    }
    
    public JSONObject append(final String s, final Object o) throws JSONException {
        testValidity(o);
        final Object opt = this.opt(s);
        if (opt == null) {
            this.put(s, new JSONArray().put(o));
        }
        else {
            if (!(opt instanceof JSONArray)) {
                throw wrongValueFormatException(s, "JSONArray", null, null);
            }
            this.put(s, ((JSONArray)opt).put(o));
        }
        return this;
    }
    
    public static Object stringToValue(final String s) {
        if ("".equals(s)) {
            return s;
        }
        if (Integer.valueOf(s.toUpperCase().hashCode()).equals("true".toUpperCase().hashCode())) {
            return Boolean.TRUE;
        }
        if (Integer.valueOf(s.toUpperCase().hashCode()).equals("false".toUpperCase().hashCode())) {
            return Boolean.FALSE;
        }
        if (Integer.valueOf(s.toUpperCase().hashCode()).equals("null".toUpperCase().hashCode())) {
            return JSONObject.NULL;
        }
        final char char1 = s.charAt(0);
        if (char1 < '0' || char1 > '9') {
            if (char1 != '-') {
                return s;
            }
        }
        try {
            return stringToNumber(s);
        }
        catch (Exception ex) {}
        return s;
    }
    
    private static String getKeyNameFromMethod(final Method method) {
        final int annotationDepth = getAnnotationDepth(method, JSONPropertyIgnore.class);
        if (annotationDepth > 0) {
            final int annotationDepth2 = getAnnotationDepth(method, JSONPropertyName.class);
            if (annotationDepth2 < 0 || annotationDepth <= annotationDepth2) {
                return null;
            }
        }
        final JSONPropertyName jsonPropertyName = getAnnotation(method, JSONPropertyName.class);
        if (jsonPropertyName != null && jsonPropertyName.value() != null && !jsonPropertyName.value().isEmpty()) {
            return jsonPropertyName.value();
        }
        final String name = method.getName();
        String s;
        if (name.startsWith("get") && name.length() > 3) {
            s = name.substring(3);
        }
        else {
            if (!name.startsWith("is") || name.length() <= 2) {
                return null;
            }
            s = name.substring(2);
        }
        if (s.length() == 0 || Character.isLowerCase(s.charAt(0))) {
            return null;
        }
        if (s.length() == 1) {
            s = s.toLowerCase(Locale.ROOT);
        }
        else if (!Character.isUpperCase(s.charAt(1))) {
            s = String.valueOf(new StringBuilder().append(s.substring(0, 1).toLowerCase(Locale.ROOT)).append(s.substring(1)));
        }
        return s;
    }
    
    public boolean optBoolean(final String s, final boolean b) {
        final Object opt = this.opt(s);
        if (JSONObject.NULL.equals(opt)) {
            return b;
        }
        if (opt instanceof Boolean) {
            return (boolean)opt;
        }
        try {
            return this.getBoolean(s);
        }
        catch (Exception ex) {
            return b;
        }
    }
    
    public static String numberToString(final Number n) throws JSONException {
        if (n == null) {
            throw new JSONException("Null pointer");
        }
        testValidity(n);
        String s = n.toString();
        if (s.indexOf(46) > 0 && s.indexOf(101) < 0 && s.indexOf(69) < 0) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }
    
    public JSONObject(final String s, final Locale locale) throws JSONException {
        this();
        final ResourceBundle bundle = ResourceBundle.getBundle(s, locale, Thread.currentThread().getContextClassLoader());
        final Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            final String nextElement = keys.nextElement();
            if (nextElement != null) {
                final String[] split = nextElement.split("\\.");
                final int n = split.length - 1;
                JSONObject jsonObject = this;
                for (final String s2 : split) {
                    JSONObject optJSONObject = jsonObject.optJSONObject(s2);
                    if (optJSONObject == null) {
                        optJSONObject = new JSONObject();
                        jsonObject.put(s2, optJSONObject);
                    }
                    jsonObject = optJSONObject;
                }
                jsonObject.put(split[n], bundle.getString(nextElement));
            }
        }
    }
    
    protected Set<Map.Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }
    
    public Writer write(final Writer writer, final int n, final int n2) throws JSONException {
        try {
            int n3 = 0;
            final int length = this.length();
            writer.write(123);
            if (length == 1) {
                final Map.Entry<String, Object> entry = this.entrySet().iterator().next();
                final String s = entry.getKey();
                writer.write(quote(s));
                writer.write(58);
                if (n > 0) {
                    writer.write(32);
                }
                try {
                    writeValue(writer, entry.getValue(), n, n2);
                }
                catch (Exception ex) {
                    throw new JSONException(String.valueOf(new StringBuilder().append("Unable to write JSONObject value for key: ").append(s)), ex);
                }
            }
            else if (length != 0) {
                final int n4 = n2 + n;
                for (final Map.Entry<String, Object> entry2 : this.entrySet()) {
                    if (n3 != 0) {
                        writer.write(44);
                    }
                    if (n > 0) {
                        writer.write(10);
                    }
                    indent(writer, n4);
                    final String s2 = entry2.getKey();
                    writer.write(quote(s2));
                    writer.write(58);
                    if (n > 0) {
                        writer.write(32);
                    }
                    try {
                        writeValue(writer, entry2.getValue(), n, n4);
                    }
                    catch (Exception ex2) {
                        throw new JSONException(String.valueOf(new StringBuilder().append("Unable to write JSONObject value for key: ").append(s2)), ex2);
                    }
                    n3 = 1;
                }
                if (n > 0) {
                    writer.write(10);
                }
                indent(writer, n2);
            }
            writer.write(125);
            return writer;
        }
        catch (IOException ex3) {
            throw new JSONException(ex3);
        }
    }
    
    public int optInt(final String s, final int n) {
        final Number optNumber = this.optNumber(s, null);
        if (optNumber == null) {
            return n;
        }
        return optNumber.intValue();
    }
    
    public JSONObject put(final String s, final Collection<?> collection) throws JSONException {
        return this.put(s, new JSONArray(collection));
    }
    
    public int optInt(final String s) {
        return this.optInt(s, 0);
    }
    
    public double getDouble(final String s) throws JSONException {
        final Object value = this.get(s);
        if (value instanceof Number) {
            return ((Number)value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        }
        catch (Exception ex) {
            throw wrongValueFormatException(s, "double", ex);
        }
    }
    
    public <E extends Enum<E>> E optEnum(final Class<E> clazz, final String s) {
        return this.optEnum(clazz, s, (E)null);
    }
    
    public JSONObject(final Object o, final String... array) {
        this(array.length);
        final Class<?> class1 = o.getClass();
        for (int i = 0; i < array.length; ++i) {
            final String s = array[i];
            try {
                this.putOpt(s, class1.getField(s).get(o));
            }
            catch (Exception ex) {}
        }
    }
    
    static boolean isNumberSimilar(final Number n, final Number n2) {
        if (!numberIsFinite(n) || !numberIsFinite(n2)) {
            return false;
        }
        if (n.getClass().equals(n2.getClass()) && n instanceof Comparable) {
            return ((Comparable)n).compareTo(n2) == 0;
        }
        final BigDecimal objectToBigDecimal = objectToBigDecimal(n, null);
        final BigDecimal objectToBigDecimal2 = objectToBigDecimal(n2, null);
        return objectToBigDecimal != null && objectToBigDecimal2 != null && objectToBigDecimal.compareTo(objectToBigDecimal2) == 0;
    }
    
    public double optDouble(final String s) {
        return this.optDouble(s, Double.NaN);
    }
    
    public long optLong(final String s) {
        return this.optLong(s, 0L);
    }
    
    public JSONObject(final Object o) {
        this();
        this.populateMap(o);
    }
    
    static BigInteger objectToBigInteger(final Object o, final BigInteger bigInteger) {
        if (JSONObject.NULL.equals(o)) {
            return bigInteger;
        }
        if (o instanceof BigInteger) {
            return (BigInteger)o;
        }
        if (o instanceof BigDecimal) {
            return ((BigDecimal)o).toBigInteger();
        }
        if (o instanceof Double || o instanceof Float) {
            if (!numberIsFinite((Number)o)) {
                return bigInteger;
            }
            return new BigDecimal(((Number)o).doubleValue()).toBigInteger();
        }
        else {
            if (o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte) {
                return BigInteger.valueOf(((Number)o).longValue());
            }
            try {
                final String string = o.toString();
                if (isDecimalNotation(string)) {
                    return new BigDecimal(string).toBigInteger();
                }
                return new BigInteger(string);
            }
            catch (Exception ex) {
                return bigInteger;
            }
        }
    }
    
    public JSONObject put(final String s, final Map<?, ?> map) throws JSONException {
        return this.put(s, new JSONObject(map));
    }
    
    public static String[] getNames(final JSONObject jsonObject) {
        if (jsonObject.isEmpty()) {
            return null;
        }
        return jsonObject.keySet().toArray(new String[jsonObject.length()]);
    }
    
    public boolean optBoolean(final String s) {
        return this.optBoolean(s, false);
    }
    
    public boolean has(final String s) {
        return this.map.containsKey(s);
    }
    
    private static boolean numberIsFinite(final Number n) {
        return (!(n instanceof Double) || (!((Double)n).isInfinite() && !((Double)n).isNaN())) && (!(n instanceof Float) || (!((Float)n).isInfinite() && !((Float)n).isNaN()));
    }
    
    public JSONObject increment(final String s) throws JSONException {
        final Object opt = this.opt(s);
        if (opt == null) {
            this.put(s, 1);
        }
        else if (opt instanceof Integer) {
            this.put(s, (int)opt + 1);
        }
        else if (opt instanceof Long) {
            this.put(s, (long)opt + 1L);
        }
        else if (opt instanceof BigInteger) {
            this.put(s, ((BigInteger)opt).add(BigInteger.ONE));
        }
        else if (opt instanceof Float) {
            this.put(s, (float)opt + 1.0f);
        }
        else if (opt instanceof Double) {
            this.put(s, (double)opt + 1.0);
        }
        else {
            if (!(opt instanceof BigDecimal)) {
                throw new JSONException(String.valueOf(new StringBuilder().append("Unable to increment [").append(quote(s)).append("].")));
            }
            this.put(s, ((BigDecimal)opt).add(BigDecimal.ONE));
        }
        return this;
    }
    
    public Number getNumber(final String s) throws JSONException {
        final Object value = this.get(s);
        try {
            if (value instanceof Number) {
                return (Number)value;
            }
            return stringToNumber(value.toString());
        }
        catch (Exception ex) {
            throw wrongValueFormatException(s, "number", ex);
        }
    }
    
    public JSONObject(final JSONObject jsonObject, final String... array) {
        this(array.length);
        for (int i = 0; i < array.length; ++i) {
            try {
                this.putOnce(array[i], jsonObject.opt(array[i]));
            }
            catch (Exception ex) {}
        }
    }
    
    public JSONArray toJSONArray(final JSONArray jsonArray) throws JSONException {
        if (jsonArray == null || jsonArray.isEmpty()) {
            return null;
        }
        final JSONArray jsonArray2 = new JSONArray();
        for (int i = 0; i < jsonArray.length(); ++i) {
            jsonArray2.put(this.opt(jsonArray.getString(i)));
        }
        return jsonArray2;
    }
    
    protected JSONObject(final int n) {
        this.map = new HashMap<String, Object>(n);
    }
    
    public JSONObject put(final String s, final int n) throws JSONException {
        return this.put(s, (Object)n);
    }
    
    public String optString(final String s) {
        return this.optString(s, "");
    }
    
    public Iterator<String> keys() {
        return this.keySet().iterator();
    }
    
    public boolean similar(final Object o) {
        try {
            if (!(o instanceof JSONObject)) {
                return false;
            }
            if (!this.keySet().equals(((JSONObject)o).keySet())) {
                return false;
            }
            for (final Map.Entry<String, Object> entry : this.entrySet()) {
                final String s = entry.getKey();
                final Number value = entry.getValue();
                final Object value2 = ((JSONObject)o).get(s);
                if (value == value2) {
                    continue;
                }
                if (value == null) {
                    return false;
                }
                if (value instanceof JSONObject) {
                    if (!((JSONObject)value).similar(value2)) {
                        return false;
                    }
                    continue;
                }
                else if (value instanceof JSONArray) {
                    if (!((JSONArray)value).similar(value2)) {
                        return false;
                    }
                    continue;
                }
                else {
                    if (value instanceof Number && value2 instanceof Number) {
                        return isNumberSimilar(value, (Number)value2);
                    }
                    if (!value.equals(value2)) {
                        return false;
                    }
                    continue;
                }
            }
            return true;
        }
        catch (Throwable t) {
            return false;
        }
    }
    
    public JSONObject accumulate(final String s, final Object o) throws JSONException {
        testValidity(o);
        final Object opt = this.opt(s);
        if (opt == null) {
            this.put(s, (o instanceof JSONArray) ? new JSONArray().put(o) : o);
        }
        else if (opt instanceof JSONArray) {
            ((JSONArray)opt).put(o);
        }
        else {
            this.put(s, new JSONArray().put(opt).put(o));
        }
        return this;
    }
    
    @Override
    public String toString() {
        try {
            return this.toString(0);
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    static final Writer writeValue(final Writer writer, final Object o, final int n, final int n2) throws JSONException, IOException {
        if (o == null || o.equals(null)) {
            writer.write("null");
        }
        else if (o instanceof JSONString) {
            String jsonString;
            try {
                jsonString = ((JSONString)o).toJSONString();
            }
            catch (Exception ex) {
                throw new JSONException(ex);
            }
            writer.write((jsonString != null) ? jsonString.toString() : quote(o.toString()));
        }
        else if (o instanceof Number) {
            final String numberToString = numberToString((Number)o);
            if (JSONObject.NUMBER_PATTERN.matcher(numberToString).matches()) {
                writer.write(numberToString);
            }
            else {
                quote(numberToString, writer);
            }
        }
        else if (o instanceof Boolean) {
            writer.write(o.toString());
        }
        else if (o instanceof Enum) {
            writer.write(quote(((Enum)o).name()));
        }
        else if (o instanceof JSONObject) {
            ((JSONObject)o).write(writer, n, n2);
        }
        else if (o instanceof JSONArray) {
            ((JSONArray)o).write(writer, n, n2);
        }
        else if (o instanceof Map) {
            new JSONObject((Map<?, ?>)o).write(writer, n, n2);
        }
        else if (o instanceof Collection) {
            new JSONArray((Collection<?>)o).write(writer, n, n2);
        }
        else if (o.getClass().isArray()) {
            new JSONArray(o).write(writer, n, n2);
        }
        else {
            quote(o.toString(), writer);
        }
        return writer;
    }
    
    public static void testValidity(final Object o) throws JSONException {
        if (o instanceof Number && !numberIsFinite((Number)o)) {
            throw new JSONException("JSON does not allow non-finite numbers.");
        }
    }
    
    public Object query(final String s) {
        return this.query(new JSONPointer(s));
    }
    
    public long getLong(final String s) throws JSONException {
        final Object value = this.get(s);
        if (value instanceof Number) {
            return ((Number)value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        }
        catch (Exception ex) {
            throw wrongValueFormatException(s, "long", ex);
        }
    }
    
    public float optFloat(final String s) {
        return this.optFloat(s, Float.NaN);
    }
    
    private static boolean isValidMethodName(final String s) {
        return !"getClass".equals(s) && !"getDeclaringClass".equals(s);
    }
    
    public Set<String> keySet() {
        return this.map.keySet();
    }
    
    private static JSONException wrongValueFormatException(final String s, final String s2, final Object o, final Throwable t) {
        return new JSONException(String.valueOf(new StringBuilder().append("JSONObject[").append(quote(s)).append("] is not a ").append(s2).append(" (").append(o).append(").")), t);
    }
    
    private static <A extends Annotation> A getAnnotation(final Method method, final Class<A> clazz) {
        if (method == null || clazz == null) {
            return null;
        }
        if (method.isAnnotationPresent(clazz)) {
            return method.getAnnotation(clazz);
        }
        final Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.getSuperclass() == null) {
            return null;
        }
        for (final Class clazz2 : declaringClass.getInterfaces()) {
            try {
                return (A)getAnnotation(clazz2.getMethod(method.getName(), (Class[])method.getParameterTypes()), (Class<Annotation>)clazz);
            }
            catch (SecurityException ex) {}
            catch (NoSuchMethodException ex2) {}
        }
        try {
            return (A)getAnnotation(declaringClass.getSuperclass().getMethod(method.getName(), method.getParameterTypes()), (Class<Annotation>)clazz);
        }
        catch (SecurityException ex3) {
            return null;
        }
        catch (NoSuchMethodException ex4) {
            return null;
        }
    }
    
    public JSONObject put(final String s, final boolean b) throws JSONException {
        return this.put(s, b ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public double optDouble(final String s, final double n) {
        final Number optNumber = this.optNumber(s);
        if (optNumber == null) {
            return n;
        }
        return optNumber.doubleValue();
    }
    
    private static JSONException wrongValueFormatException(final String s, final String s2, final Throwable t) {
        return new JSONException(String.valueOf(new StringBuilder().append("JSONObject[").append(quote(s)).append("] is not a ").append(s2).append(".")), t);
    }
    
    public JSONObject() {
        this.map = new HashMap<String, Object>();
    }
    
    public static String doubleToString(final double n) {
        if (Double.isInfinite(n) || Double.isNaN(n)) {
            return "null";
        }
        String s = Double.toString(n);
        if (s.indexOf(46) > 0 && s.indexOf(101) < 0 && s.indexOf(69) < 0) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }
    
    public Writer write(final Writer writer) throws JSONException {
        return this.write(writer, 0, 0);
    }
    
    public BigDecimal getBigDecimal(final String s) throws JSONException {
        final Object value = this.get(s);
        final BigDecimal objectToBigDecimal = objectToBigDecimal(value, null);
        if (objectToBigDecimal != null) {
            return objectToBigDecimal;
        }
        throw wrongValueFormatException(s, "BigDecimal", value, null);
    }
    
    public static String valueToString(final Object o) throws JSONException {
        return JSONWriter.valueToString(o);
    }
    
    public Object optQuery(final String s) {
        return this.optQuery(new JSONPointer(s));
    }
    
    public int length() {
        return this.map.size();
    }
    
    public void clear() {
        this.map.clear();
    }
    
    public JSONObject put(final String s, final float n) throws JSONException {
        return this.put(s, (Object)n);
    }
    
    public JSONObject put(final String s, final Object o) throws JSONException {
        if (s == null) {
            throw new NullPointerException("Null key.");
        }
        if (o != null) {
            testValidity(o);
            this.map.put(s, o);
        }
        else {
            this.remove(s);
        }
        return this;
    }
    
    public boolean isEmpty() {
        return this.map.isEmpty();
    }
    
    public String getString(final String s) throws JSONException {
        final Object value = this.get(s);
        if (value instanceof String) {
            return (String)value;
        }
        throw wrongValueFormatException(s, "string", null);
    }
    
    public Number optNumber(final String s) {
        return this.optNumber(s, null);
    }
    
    public JSONObject(final Map<?, ?> map) {
        if (map == null) {
            this.map = new HashMap<String, Object>();
        }
        else {
            this.map = new HashMap<String, Object>(map.size());
            for (final Map.Entry<K, Object> entry : map.entrySet()) {
                if (entry.getKey() == null) {
                    throw new NullPointerException("Null key.");
                }
                final Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                this.map.put(String.valueOf(entry.getKey()), wrap(value));
            }
        }
    }
    
    public String optString(final String s, final String s2) {
        final Object opt = this.opt(s);
        return JSONObject.NULL.equals(opt) ? s2 : opt.toString();
    }
    
    public static String[] getNames(final Object o) {
        if (o == null) {
            return null;
        }
        final Field[] fields = o.getClass().getFields();
        final int length = fields.length;
        if (length == 0) {
            return null;
        }
        final String[] array = new String[length];
        for (int i = 0; i < length; ++i) {
            array[i] = fields[i].getName();
        }
        return array;
    }
    
    public JSONArray getJSONArray(final String s) throws JSONException {
        final Object value = this.get(s);
        if (value instanceof JSONArray) {
            return (JSONArray)value;
        }
        throw wrongValueFormatException(s, "JSONArray", null);
    }
    
    public boolean getBoolean(final String s) throws JSONException {
        final Object value = this.get(s);
        if (value.equals(Boolean.FALSE) || (value instanceof String && Integer.valueOf(66658563).equals(((String)value).toUpperCase().hashCode()))) {
            return false;
        }
        if (value.equals(Boolean.TRUE) || (value instanceof String && Integer.valueOf(2583950).equals(((String)value).toUpperCase().hashCode()))) {
            return true;
        }
        throw wrongValueFormatException(s, "Boolean", null);
    }
    
    public static String quote(final String s) {
        final StringWriter stringWriter = new StringWriter();
        synchronized (stringWriter.getBuffer()) {
            try {
                return quote(s, stringWriter).toString();
            }
            catch (IOException ex) {
                return "";
            }
        }
    }
    
    static final void indent(final Writer writer, final int n) throws IOException {
        for (int i = 0; i < n; ++i) {
            writer.write(32);
        }
    }
    
    public Number optNumber(final String s, final Number n) {
        final Object opt = this.opt(s);
        if (JSONObject.NULL.equals(opt)) {
            return n;
        }
        if (opt instanceof Number) {
            return (Number)opt;
        }
        try {
            return stringToNumber(opt.toString());
        }
        catch (Exception ex) {
            return n;
        }
    }
    
    public JSONObject putOnce(final String s, final Object o) throws JSONException {
        if (s == null || o == null) {
            return this;
        }
        if (this.opt(s) != null) {
            throw new JSONException(String.valueOf(new StringBuilder().append("Duplicate key \"").append(s).append("\"")));
        }
        return this.put(s, o);
    }
    
    public Object get(final String s) throws JSONException {
        if (s == null) {
            throw new JSONException("Null key.");
        }
        final Object opt = this.opt(s);
        if (opt == null) {
            throw new JSONException(String.valueOf(new StringBuilder().append("JSONObject[").append(quote(s)).append("] not found.")));
        }
        return opt;
    }
    
    private static final class Null
    {
        @Override
        public String toString() {
            return "null";
        }
        
        @Override
        public boolean equals(final Object o) {
            return o == null || o == this;
        }
        
        @Override
        public int hashCode() {
            return 0;
        }
        
        Null(final JSONObject$1 object) {
            this();
        }
        
        @Override
        protected final Object clone() {
            return this;
        }
        
        private Null() {
        }
    }
}
