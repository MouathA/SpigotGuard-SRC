package org.json;

import java.math.*;
import java.util.*;
import java.lang.reflect.*;
import java.io.*;

public class JSONArray implements Iterable<Object>
{
    private final ArrayList<Object> myArrayList;
    
    public float optFloat(final int n) {
        return this.optFloat(n, Float.NaN);
    }
    
    public JSONArray put(final int n, final Map<?, ?> map) throws JSONException {
        this.put(n, new JSONObject(map));
        return this;
    }
    
    public JSONArray putAll(final Iterable<?> iterable) {
        this.addAll(iterable, false);
        return this;
    }
    
    public JSONArray put(final int n, final boolean b) throws JSONException {
        return this.put(n, b ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public JSONArray getJSONArray(final int n) throws JSONException {
        final Object value = this.get(n);
        if (value instanceof JSONArray) {
            return (JSONArray)value;
        }
        throw wrongValueFormatException(n, "JSONArray", null);
    }
    
    public double optDouble(final int n, final double n2) {
        final Number optNumber = this.optNumber(n, null);
        if (optNumber == null) {
            return n2;
        }
        return optNumber.doubleValue();
    }
    
    public JSONArray put(final double n) throws JSONException {
        return this.put((Object)n);
    }
    
    public Object query(final JSONPointer jsonPointer) {
        return jsonPointer.queryFrom(this);
    }
    
    public Object query(final String s) {
        return this.query(new JSONPointer(s));
    }
    
    public JSONArray put(final float n) throws JSONException {
        return this.put((Object)n);
    }
    
    public JSONArray put(final boolean b) {
        return this.put(b ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public float optFloat(final int n, final float n2) {
        final Number optNumber = this.optNumber(n, null);
        if (optNumber == null) {
            return n2;
        }
        return optNumber.floatValue();
    }
    
    private static JSONException wrongValueFormatException(final int n, final String s, final Object o, final Throwable t) {
        return new JSONException(String.valueOf(new StringBuilder().append("JSONArray[").append(n).append("] is not a ").append(s).append(" (").append(o).append(").")), t);
    }
    
    public JSONArray putAll(final JSONArray jsonArray) {
        this.myArrayList.addAll(jsonArray.myArrayList);
        return this;
    }
    
    public JSONArray put(final Object o) {
        JSONObject.testValidity(o);
        this.myArrayList.add(o);
        return this;
    }
    
    public JSONArray(final JSONArray jsonArray) {
        if (jsonArray == null) {
            this.myArrayList = new ArrayList<Object>();
        }
        else {
            this.myArrayList = new ArrayList<Object>(jsonArray.myArrayList);
        }
    }
    
    public JSONObject toJSONObject(final JSONArray jsonArray) throws JSONException {
        if (jsonArray == null || jsonArray.isEmpty() || this.isEmpty()) {
            return null;
        }
        final JSONObject jsonObject = new JSONObject(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); ++i) {
            jsonObject.put(jsonArray.getString(i), this.opt(i));
        }
        return jsonObject;
    }
    
    public Object get(final int n) throws JSONException {
        final Object opt = this.opt(n);
        if (opt == null) {
            throw new JSONException(String.valueOf(new StringBuilder().append("JSONArray[").append(n).append("] not found.")));
        }
        return opt;
    }
    
    public String join(final String s) throws JSONException {
        final int length = this.length();
        if (length == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(JSONObject.valueToString(this.myArrayList.get(0)));
        for (int i = 1; i < length; ++i) {
            sb.append(s).append(JSONObject.valueToString(this.myArrayList.get(i)));
        }
        return String.valueOf(sb);
    }
    
    public JSONArray optJSONArray(final int n) {
        final Object opt = this.opt(n);
        return (opt instanceof JSONArray) ? ((JSONArray)opt) : null;
    }
    
    public float getFloat(final int n) throws JSONException {
        final Object value = this.get(n);
        if (value instanceof Number) {
            return (float)value;
        }
        try {
            return Float.parseFloat(value.toString());
        }
        catch (Exception ex) {
            throw wrongValueFormatException(n, "float", ex);
        }
    }
    
    public JSONArray(final Object o) throws JSONException {
        this();
        if (!o.getClass().isArray()) {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
        this.addAll(o, true);
    }
    
    public JSONArray putAll(final Collection<?> collection) {
        this.addAll(collection, false);
        return this;
    }
    
    private void addAll(final Collection<?> collection, final boolean b) {
        this.myArrayList.ensureCapacity(this.myArrayList.size() + collection.size());
        if (b) {
            final Iterator<?> iterator = collection.iterator();
            while (iterator.hasNext()) {
                this.put(JSONObject.wrap(iterator.next()));
            }
        }
        else {
            final Iterator<?> iterator2 = collection.iterator();
            while (iterator2.hasNext()) {
                this.put(iterator2.next());
            }
        }
    }
    
    public JSONArray put(final int n, final int n2) throws JSONException {
        return this.put(n, (Object)n2);
    }
    
    public JSONArray put(final Collection<?> collection) {
        return this.put(new JSONArray(collection));
    }
    
    public JSONArray(final JSONTokener jsonTokener) throws JSONException {
        this();
        if (jsonTokener.nextClean() != '[') {
            throw jsonTokener.syntaxError("A JSONArray text must start with '['");
        }
        final char nextClean = jsonTokener.nextClean();
        if (nextClean == '\0') {
            throw jsonTokener.syntaxError("Expected a ',' or ']'");
        }
        if (nextClean == ']') {
            return;
        }
        jsonTokener.back();
        while (true) {
            if (jsonTokener.nextClean() == ',') {
                jsonTokener.back();
                this.myArrayList.add(JSONObject.NULL);
            }
            else {
                jsonTokener.back();
                this.myArrayList.add(jsonTokener.nextValue());
            }
            switch (jsonTokener.nextClean()) {
                case '\0': {
                    throw jsonTokener.syntaxError("Expected a ',' or ']'");
                }
                case ',': {
                    final char nextClean2 = jsonTokener.nextClean();
                    if (nextClean2 == '\0') {
                        throw jsonTokener.syntaxError("Expected a ',' or ']'");
                    }
                    if (nextClean2 == ']') {
                        return;
                    }
                    jsonTokener.back();
                    continue;
                }
                case ']': {}
                default: {
                    throw jsonTokener.syntaxError("Expected a ',' or ']'");
                }
            }
        }
    }
    
    public JSONArray(final int n) throws JSONException {
        if (n < 0) {
            throw new JSONException("JSONArray initial capacity cannot be negative.");
        }
        this.myArrayList = new ArrayList<Object>(n);
    }
    
    public JSONArray(final Iterable<?> iterable) {
        this();
        if (iterable == null) {
            return;
        }
        this.addAll(iterable, true);
    }
    
    public long getLong(final int n) throws JSONException {
        final Object value = this.get(n);
        if (value instanceof Number) {
            return ((Number)value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        }
        catch (Exception ex) {
            throw wrongValueFormatException(n, "long", ex);
        }
    }
    
    public void clear() {
        this.myArrayList.clear();
    }
    
    public JSONArray(final Collection<?> collection) {
        if (collection == null) {
            this.myArrayList = new ArrayList<Object>();
        }
        else {
            this.myArrayList = new ArrayList<Object>(collection.size());
            this.addAll(collection, true);
        }
    }
    
    public <E extends Enum<E>> E optEnum(final Class<E> clazz, final int n) {
        return this.optEnum(clazz, n, (E)null);
    }
    
    public JSONArray() {
        this.myArrayList = new ArrayList<Object>();
    }
    
    public BigDecimal getBigDecimal(final int n) throws JSONException {
        final Object value = this.get(n);
        final BigDecimal objectToBigDecimal = JSONObject.objectToBigDecimal(value, null);
        if (objectToBigDecimal == null) {
            throw wrongValueFormatException(n, "BigDecimal", value, null);
        }
        return objectToBigDecimal;
    }
    
    public JSONArray put(final long n) {
        return this.put((Object)n);
    }
    
    public boolean isEmpty() {
        return this.myArrayList.isEmpty();
    }
    
    public JSONArray put(final int n) {
        return this.put((Object)n);
    }
    
    public boolean similar(final Object o) {
        if (!(o instanceof JSONArray)) {
            return false;
        }
        final int length = this.length();
        if (length != ((JSONArray)o).length()) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            final Object value = this.myArrayList.get(i);
            final Object value2 = ((JSONArray)o).myArrayList.get(i);
            if (value != value2) {
                if (value == null) {
                    return false;
                }
                if (value instanceof JSONObject) {
                    if (!((JSONObject)value).similar(value2)) {
                        return false;
                    }
                }
                else if (value instanceof JSONArray) {
                    if (!((JSONArray)value).similar(value2)) {
                        return false;
                    }
                }
                else {
                    if (value instanceof Number && value2 instanceof Number) {
                        return JSONObject.isNumberSimilar((Number)value, (Number)value2);
                    }
                    if (!value.equals(value2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public int getInt(final int n) throws JSONException {
        final Object value = this.get(n);
        if (value instanceof Number) {
            return ((Number)value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        }
        catch (Exception ex) {
            throw wrongValueFormatException(n, "int", ex);
        }
    }
    
    public <E extends Enum<E>> E getEnum(final Class<E> clazz, final int n) throws JSONException {
        final Enum optEnum = this.optEnum((Class<Enum>)clazz, n);
        if (optEnum == null) {
            throw wrongValueFormatException(n, String.valueOf(new StringBuilder().append("enum of type ").append(JSONObject.quote(clazz.getSimpleName()))), null);
        }
        return (E)optEnum;
    }
    
    public boolean optBoolean(final int n, final boolean b) {
        try {
            return this.getBoolean(n);
        }
        catch (Exception ex) {
            return b;
        }
    }
    
    public JSONArray put(final int n, final Collection<?> collection) throws JSONException {
        return this.put(n, new JSONArray(collection));
    }
    
    private static JSONException wrongValueFormatException(final int n, final String s, final Throwable t) {
        return new JSONException(String.valueOf(new StringBuilder().append("JSONArray[").append(n).append("] is not a ").append(s).append(".")), t);
    }
    
    public String toString(final int n) throws JSONException {
        final StringWriter stringWriter = new StringWriter();
        synchronized (stringWriter.getBuffer()) {
            return this.write(stringWriter, n, 0).toString();
        }
    }
    
    public boolean isNull(final int n) {
        return JSONObject.NULL.equals(this.opt(n));
    }
    
    @Override
    public Iterator<Object> iterator() {
        return this.myArrayList.iterator();
    }
    
    public JSONArray putAll(final Object o) throws JSONException {
        this.addAll(o, false);
        return this;
    }
    
    public Number optNumber(final int n, final Number n2) {
        final Object opt = this.opt(n);
        if (JSONObject.NULL.equals(opt)) {
            return n2;
        }
        if (opt instanceof Number) {
            return (Number)opt;
        }
        if (opt instanceof String) {
            try {
                return JSONObject.stringToNumber((String)opt);
            }
            catch (Exception ex) {
                return n2;
            }
        }
        return n2;
    }
    
    private void addAll(final Iterable<?> iterable, final boolean b) {
        if (b) {
            final Iterator<?> iterator = iterable.iterator();
            while (iterator.hasNext()) {
                this.put(JSONObject.wrap(iterator.next()));
            }
        }
        else {
            final Iterator<?> iterator2 = iterable.iterator();
            while (iterator2.hasNext()) {
                this.put(iterator2.next());
            }
        }
    }
    
    public Object opt(final int n) {
        return (n < 0 || n >= this.length()) ? null : this.myArrayList.get(n);
    }
    
    public JSONObject getJSONObject(final int n) throws JSONException {
        final Object value = this.get(n);
        if (value instanceof JSONObject) {
            return (JSONObject)value;
        }
        throw wrongValueFormatException(n, "JSONObject", null);
    }
    
    public String optString(final int n, final String s) {
        final Object opt = this.opt(n);
        return JSONObject.NULL.equals(opt) ? s : opt.toString();
    }
    
    public int optInt(final int n) {
        return this.optInt(n, 0);
    }
    
    public Object optQuery(final JSONPointer jsonPointer) {
        try {
            return jsonPointer.queryFrom(this);
        }
        catch (JSONPointerException ex) {
            return null;
        }
    }
    
    public JSONObject optJSONObject(final int n) {
        final Object opt = this.opt(n);
        return (opt instanceof JSONObject) ? ((JSONObject)opt) : null;
    }
    
    public BigInteger getBigInteger(final int n) throws JSONException {
        final Object value = this.get(n);
        final BigInteger objectToBigInteger = JSONObject.objectToBigInteger(value, null);
        if (objectToBigInteger == null) {
            throw wrongValueFormatException(n, "BigInteger", value, null);
        }
        return objectToBigInteger;
    }
    
    public JSONArray(final String s) throws JSONException {
        this(new JSONTokener(s));
    }
    
    public Object optQuery(final String s) {
        return this.optQuery(new JSONPointer(s));
    }
    
    public long optLong(final int n, final long n2) {
        final Number optNumber = this.optNumber(n, null);
        if (optNumber == null) {
            return n2;
        }
        return optNumber.longValue();
    }
    
    public double getDouble(final int n) throws JSONException {
        final Object value = this.get(n);
        if (value instanceof Number) {
            return ((Number)value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        }
        catch (Exception ex) {
            throw wrongValueFormatException(n, "double", ex);
        }
    }
    
    public Writer write(final Writer writer) throws JSONException {
        return this.write(writer, 0, 0);
    }
    
    public JSONArray put(final int i, final Object o) throws JSONException {
        if (i < 0) {
            throw new JSONException(String.valueOf(new StringBuilder().append("JSONArray[").append(i).append("] not found.")));
        }
        if (i < this.length()) {
            JSONObject.testValidity(o);
            this.myArrayList.set(i, o);
            return this;
        }
        if (i == this.length()) {
            return this.put(o);
        }
        this.myArrayList.ensureCapacity(i + 1);
        while (i != this.length()) {
            this.myArrayList.add(JSONObject.NULL);
        }
        return this.put(o);
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
    
    public List<Object> toList() {
        final ArrayList<List<Object>> list = (ArrayList<List<Object>>)new ArrayList<JSONObject>(this.myArrayList.size());
        for (final JSONArray next : this.myArrayList) {
            if (next == null || JSONObject.NULL.equals(next)) {
                list.add((JSONObject)null);
            }
            else if (next instanceof JSONArray) {
                list.add((JSONObject)next.toList());
            }
            else if (next instanceof JSONObject) {
                list.add((JSONObject)((JSONObject)next).toMap());
            }
            else {
                list.add((JSONObject)next);
            }
        }
        return (List<Object>)list;
    }
    
    public Number getNumber(final int n) throws JSONException {
        final Object value = this.get(n);
        try {
            if (value instanceof Number) {
                return (Number)value;
            }
            return JSONObject.stringToNumber(value.toString());
        }
        catch (Exception ex) {
            throw wrongValueFormatException(n, "number", ex);
        }
    }
    
    public boolean optBoolean(final int n) {
        return this.optBoolean(n, false);
    }
    
    public BigInteger optBigInteger(final int n, final BigInteger bigInteger) {
        return JSONObject.objectToBigInteger(this.opt(n), bigInteger);
    }
    
    public Object remove(final int n) {
        return (n >= 0 && n < this.length()) ? this.myArrayList.remove(n) : null;
    }
    
    public String getString(final int n) throws JSONException {
        final Object value = this.get(n);
        if (value instanceof String) {
            return (String)value;
        }
        throw wrongValueFormatException(n, "String", null);
    }
    
    public BigDecimal optBigDecimal(final int n, final BigDecimal bigDecimal) {
        return JSONObject.objectToBigDecimal(this.opt(n), bigDecimal);
    }
    
    public int length() {
        return this.myArrayList.size();
    }
    
    public JSONArray put(final int n, final float n2) throws JSONException {
        return this.put(n, (Object)n2);
    }
    
    public boolean getBoolean(final int n) throws JSONException {
        final Object value = this.get(n);
        if (value.equals(Boolean.FALSE) || (value instanceof String && Integer.valueOf(66658563).equals(((String)value).toUpperCase().hashCode()))) {
            return false;
        }
        if (value.equals(Boolean.TRUE) || (value instanceof String && Integer.valueOf(2583950).equals(((String)value).toUpperCase().hashCode()))) {
            return true;
        }
        throw wrongValueFormatException(n, "boolean", null);
    }
    
    public JSONArray put(final int n, final long n2) throws JSONException {
        return this.put(n, (Object)n2);
    }
    
    public int optInt(final int n, final int n2) {
        final Number optNumber = this.optNumber(n, null);
        if (optNumber == null) {
            return n2;
        }
        return optNumber.intValue();
    }
    
    public double optDouble(final int n) {
        return this.optDouble(n, Double.NaN);
    }
    
    private void addAll(final Object o, final boolean b) throws JSONException {
        if (o.getClass().isArray()) {
            final int length = Array.getLength(o);
            this.myArrayList.ensureCapacity(this.myArrayList.size() + length);
            if (b) {
                for (int i = 0; i < length; ++i) {
                    this.put(JSONObject.wrap(Array.get(o, i)));
                }
            }
            else {
                for (int j = 0; j < length; ++j) {
                    this.put(Array.get(o, j));
                }
            }
        }
        else if (o instanceof JSONArray) {
            this.myArrayList.addAll(((JSONArray)o).myArrayList);
        }
        else if (o instanceof Collection) {
            this.addAll((Collection<?>)o, b);
        }
        else {
            if (!(o instanceof Iterable)) {
                throw new JSONException("JSONArray initial value should be a string or collection or array.");
            }
            this.addAll((Iterable<?>)o, b);
        }
    }
    
    public String optString(final int n) {
        return this.optString(n, "");
    }
    
    public long optLong(final int n) {
        return this.optLong(n, 0L);
    }
    
    public <E extends Enum<E>> E optEnum(final Class<E> clazz, final int n, final E e) {
        try {
            final Object opt = this.opt(n);
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
    
    public JSONArray put(final Map<?, ?> map) {
        return this.put(new JSONObject(map));
    }
    
    public Number optNumber(final int n) {
        return this.optNumber(n, null);
    }
    
    public JSONArray put(final int n, final double n2) throws JSONException {
        return this.put(n, (Object)n2);
    }
    
    public Writer write(final Writer writer, final int n, final int n2) throws JSONException {
        try {
            int n3 = 0;
            final int length = this.length();
            writer.write(91);
            Label_0186: {
                if (length == 1) {
                    try {
                        JSONObject.writeValue(writer, this.myArrayList.get(0), n, n2);
                        break Label_0186;
                    }
                    catch (Exception ex) {
                        throw new JSONException("Unable to write JSONArray value at index: 0", ex);
                    }
                }
                if (length != 0) {
                    final int n4 = n2 + n;
                    for (int i = 0; i < length; ++i) {
                        if (n3 != 0) {
                            writer.write(44);
                        }
                        if (n > 0) {
                            writer.write(10);
                        }
                        JSONObject.indent(writer, n4);
                        try {
                            JSONObject.writeValue(writer, this.myArrayList.get(i), n, n4);
                        }
                        catch (Exception ex2) {
                            throw new JSONException(String.valueOf(new StringBuilder().append("Unable to write JSONArray value at index: ").append(i)), ex2);
                        }
                        n3 = 1;
                    }
                    if (n > 0) {
                        writer.write(10);
                    }
                    JSONObject.indent(writer, n2);
                }
            }
            writer.write(93);
            return writer;
        }
        catch (IOException ex3) {
            throw new JSONException(ex3);
        }
    }
}
