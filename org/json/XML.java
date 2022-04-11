package org.json;

import java.io.*;
import java.util.*;
import java.math.*;

public class XML
{
    public static final Character QUOT;
    public static final Character GT;
    public static final String TYPE_ATTR;
    public static final Character LT;
    public static final Character QUEST;
    public static final Character EQ;
    public static final String NULL_ATTR;
    public static final Character AMP;
    public static final Character APOS;
    public static final Character BANG;
    public static final Character SLASH;
    
    private static boolean isDecimalNotation(final String s) {
        return s.indexOf(46) > -1 || s.indexOf(101) > -1 || s.indexOf(69) > -1 || "-0".equals(s);
    }
    
    public static JSONObject toJSONObject(final Reader reader, final XMLParserConfiguration xmlParserConfiguration) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        final XMLTokener xmlTokener = new XMLTokener(reader);
        while (xmlTokener.more()) {
            xmlTokener.skipPast("<");
            if (xmlTokener.more()) {
                parse(xmlTokener, jsonObject, null, xmlParserConfiguration);
            }
        }
        return jsonObject;
    }
    
    public static String unescape(final String s) {
        final StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ++i) {
            final char char1 = s.charAt(i);
            if (char1 == '&') {
                final int index = s.indexOf(59, i);
                if (index > i) {
                    final String substring = s.substring(i + 1, index);
                    sb.append(XMLTokener.unescapeEntity(substring));
                    i += substring.length() + 1;
                }
                else {
                    sb.append(char1);
                }
            }
            else {
                sb.append(char1);
            }
        }
        return String.valueOf(sb);
    }
    
    public static JSONObject toJSONObject(final String s, final XMLParserConfiguration xmlParserConfiguration) throws JSONException {
        return toJSONObject(new StringReader(s), xmlParserConfiguration);
    }
    
    public static String toString(final Object o, final String s, final XMLParserConfiguration xmlParserConfiguration) throws JSONException {
        final StringBuilder sb = new StringBuilder();
        if (o instanceof JSONObject) {
            if (s != null) {
                sb.append('<');
                sb.append(s);
                sb.append('>');
            }
            final JSONObject jsonObject = (JSONObject)o;
            for (final String s2 : jsonObject.keySet()) {
                Object opt = jsonObject.opt(s2);
                if (opt == null) {
                    opt = "";
                }
                else if (((JSONArray)opt).getClass().isArray()) {
                    opt = new JSONArray(opt);
                }
                if (s2.equals(xmlParserConfiguration.getcDataTagName())) {
                    if (opt instanceof JSONArray) {
                        final JSONArray jsonArray = (JSONArray)opt;
                        for (int length = jsonArray.length(), i = 0; i < length; ++i) {
                            if (i > 0) {
                                sb.append('\n');
                            }
                            sb.append(escape(jsonArray.opt(i).toString()));
                        }
                    }
                    else {
                        sb.append(escape(opt.toString()));
                    }
                }
                else if (opt instanceof JSONArray) {
                    final JSONArray jsonArray2 = (JSONArray)opt;
                    for (int length2 = jsonArray2.length(), j = 0; j < length2; ++j) {
                        final Object opt2 = jsonArray2.opt(j);
                        if (opt2 instanceof JSONArray) {
                            sb.append('<');
                            sb.append(s2);
                            sb.append('>');
                            sb.append(toString(opt2, null, xmlParserConfiguration));
                            sb.append("</");
                            sb.append(s2);
                            sb.append('>');
                        }
                        else {
                            sb.append(toString(opt2, s2, xmlParserConfiguration));
                        }
                    }
                }
                else if ("".equals(opt)) {
                    sb.append('<');
                    sb.append(s2);
                    sb.append("/>");
                }
                else {
                    sb.append(toString(opt, s2, xmlParserConfiguration));
                }
            }
            if (s != null) {
                sb.append("</");
                sb.append(s);
                sb.append('>');
            }
            return String.valueOf(sb);
        }
        if (o != null && (o instanceof JSONArray || o.getClass().isArray())) {
            JSONArray jsonArray3;
            if (o.getClass().isArray()) {
                jsonArray3 = new JSONArray(o);
            }
            else {
                jsonArray3 = (JSONArray)o;
            }
            for (int length3 = jsonArray3.length(), k = 0; k < length3; ++k) {
                sb.append(toString(jsonArray3.opt(k), (s == null) ? "array" : s, xmlParserConfiguration));
            }
            return String.valueOf(sb);
        }
        final String s3 = (o == null) ? "null" : escape(o.toString());
        return (s == null) ? String.valueOf(new StringBuilder().append("\"").append(s3).append("\"")) : ((s3.length() == 0) ? String.valueOf(new StringBuilder().append("<").append(s).append("/>")) : String.valueOf(new StringBuilder().append("<").append(s).append(">").append(s3).append("</").append(s).append(">")));
    }
    
    public static String toString(final Object o) throws JSONException {
        return toString(o, null, XMLParserConfiguration.ORIGINAL);
    }
    
    public static String escape(final String s) {
        final StringBuilder sb = new StringBuilder(s.length());
        for (final int intValue : codePointIterator(s)) {
            switch (intValue) {
                case 38: {
                    sb.append("&amp;");
                    continue;
                }
                case 60: {
                    sb.append("&lt;");
                    continue;
                }
                case 62: {
                    sb.append("&gt;");
                    continue;
                }
                case 34: {
                    sb.append("&quot;");
                    continue;
                }
                case 39: {
                    sb.append("&apos;");
                    continue;
                }
                default: {
                    if (mustEscape(intValue)) {
                        sb.append("&#x");
                        sb.append(Integer.toHexString(intValue));
                        sb.append(';');
                        continue;
                    }
                    sb.appendCodePoint(intValue);
                    continue;
                }
            }
        }
        return String.valueOf(sb);
    }
    
    private static Number stringToNumber(final String s) throws NumberFormatException {
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
    
    public static String toString(final Object o, final String s) {
        return toString(o, s, XMLParserConfiguration.ORIGINAL);
    }
    
    public static void noSpace(final String s) throws JSONException {
        final int length = s.length();
        if (length == 0) {
            throw new JSONException("Empty string.");
        }
        for (int i = 0; i < length; ++i) {
            if (Character.isWhitespace(s.charAt(i))) {
                throw new JSONException(String.valueOf(new StringBuilder().append("'").append(s).append("' contains a space character.")));
            }
        }
    }
    
    private static boolean mustEscape(final int n) {
        return (Character.isISOControl(n) && n != 9 && n != 10 && n != 13) || ((n < 32 || n > 55295) && (n < 57344 || n > 65533) && (n < 65536 || n > 1114111));
    }
    
    public static Object stringToValue(final String s, final XMLXsiTypeConverter<?> xmlXsiTypeConverter) {
        if (xmlXsiTypeConverter != null) {
            return xmlXsiTypeConverter.convert(s);
        }
        return stringToValue(s);
    }
    
    private static boolean parse(final XMLTokener xmlTokener, final JSONObject jsonObject, final String s, final XMLParserConfiguration xmlParserConfiguration) throws JSONException {
        final Object nextToken = xmlTokener.nextToken();
        if (nextToken == XML.BANG) {
            final char next = xmlTokener.next();
            if (next == '-') {
                if (xmlTokener.next() == '-') {
                    xmlTokener.skipPast("-->");
                    return false;
                }
                xmlTokener.back();
            }
            else if (next == '[') {
                if ("CDATA".equals(xmlTokener.nextToken()) && xmlTokener.next() == '[') {
                    final String nextCDATA = xmlTokener.nextCDATA();
                    if (nextCDATA.length() > 0) {
                        jsonObject.accumulate(xmlParserConfiguration.getcDataTagName(), nextCDATA);
                    }
                    return false;
                }
                throw xmlTokener.syntaxError("Expected 'CDATA['");
            }
            int i = 1;
            do {
                final Object nextMeta = xmlTokener.nextMeta();
                if (nextMeta == null) {
                    throw xmlTokener.syntaxError("Missing '>' after '<!'.");
                }
                if (nextMeta == XML.LT) {
                    ++i;
                }
                else {
                    if (nextMeta != XML.GT) {
                        continue;
                    }
                    --i;
                }
            } while (i > 0);
            return false;
        }
        if (nextToken == XML.QUEST) {
            xmlTokener.skipPast("?>");
            return false;
        }
        if (nextToken == XML.SLASH) {
            final Object nextToken2 = xmlTokener.nextToken();
            if (s == null) {
                throw xmlTokener.syntaxError(String.valueOf(new StringBuilder().append("Mismatched close tag ").append(nextToken2)));
            }
            if (!nextToken2.equals(s)) {
                throw xmlTokener.syntaxError(String.valueOf(new StringBuilder().append("Mismatched ").append(s).append(" and ").append(nextToken2)));
            }
            if (xmlTokener.nextToken() != XML.GT) {
                throw xmlTokener.syntaxError("Misshaped close tag");
            }
            return true;
        }
        else {
            if (nextToken instanceof Character) {
                throw xmlTokener.syntaxError("Misshaped tag");
            }
            final String s2 = (String)nextToken;
            Object o = null;
            final JSONObject jsonObject2 = new JSONObject();
            boolean b = false;
            XMLXsiTypeConverter<?> xmlXsiTypeConverter = null;
            while (true) {
                if (o == null) {
                    o = xmlTokener.nextToken();
                }
                if (o instanceof String) {
                    final String s3 = (String)o;
                    o = xmlTokener.nextToken();
                    if (o == XML.EQ) {
                        final Object nextToken3 = xmlTokener.nextToken();
                        if (!(nextToken3 instanceof String)) {
                            throw xmlTokener.syntaxError("Missing value");
                        }
                        if (xmlParserConfiguration.isConvertNilAttributeToNull() && "xsi:nil".equals(s3) && Boolean.parseBoolean((String)nextToken3)) {
                            b = true;
                        }
                        else if (xmlParserConfiguration.getXsiTypeMap() != null && !xmlParserConfiguration.getXsiTypeMap().isEmpty() && "xsi:type".equals(s3)) {
                            xmlXsiTypeConverter = xmlParserConfiguration.getXsiTypeMap().get(nextToken3);
                        }
                        else if (!b) {
                            jsonObject2.accumulate(s3, xmlParserConfiguration.isKeepStrings() ? ((String)nextToken3) : stringToValue((String)nextToken3));
                        }
                        o = null;
                    }
                    else {
                        jsonObject2.accumulate(s3, "");
                    }
                }
                else if (o == XML.SLASH) {
                    if (xmlTokener.nextToken() != XML.GT) {
                        throw xmlTokener.syntaxError("Misshaped tag");
                    }
                    if (b) {
                        jsonObject.accumulate(s2, JSONObject.NULL);
                    }
                    else if (jsonObject2.length() > 0) {
                        jsonObject.accumulate(s2, jsonObject2);
                    }
                    else {
                        jsonObject.accumulate(s2, "");
                    }
                    return false;
                }
                else {
                    if (o != XML.GT) {
                        throw xmlTokener.syntaxError("Misshaped tag");
                    }
                    while (true) {
                        final Object nextContent = xmlTokener.nextContent();
                        if (nextContent == null) {
                            if (s2 != null) {
                                throw xmlTokener.syntaxError(String.valueOf(new StringBuilder().append("Unclosed tag ").append(s2)));
                            }
                            return false;
                        }
                        else if (nextContent instanceof String) {
                            final String s4 = (String)nextContent;
                            if (s4.length() <= 0) {
                                continue;
                            }
                            if (xmlXsiTypeConverter != null) {
                                jsonObject2.accumulate(xmlParserConfiguration.getcDataTagName(), stringToValue(s4, xmlXsiTypeConverter));
                            }
                            else {
                                jsonObject2.accumulate(xmlParserConfiguration.getcDataTagName(), xmlParserConfiguration.isKeepStrings() ? s4 : stringToValue(s4));
                            }
                        }
                        else {
                            if (nextContent == XML.LT && parse(xmlTokener, jsonObject2, s2, xmlParserConfiguration)) {
                                if (jsonObject2.length() == 0) {
                                    jsonObject.accumulate(s2, "");
                                }
                                else if (jsonObject2.length() == 1 && jsonObject2.opt(xmlParserConfiguration.getcDataTagName()) != null) {
                                    jsonObject.accumulate(s2, jsonObject2.opt(xmlParserConfiguration.getcDataTagName()));
                                }
                                else {
                                    jsonObject.accumulate(s2, jsonObject2);
                                }
                                return false;
                            }
                            continue;
                        }
                    }
                }
            }
        }
    }
    
    public static JSONObject toJSONObject(final Reader reader) throws JSONException {
        return toJSONObject(reader, XMLParserConfiguration.ORIGINAL);
    }
    
    public static JSONObject toJSONObject(final Reader reader, final boolean b) throws JSONException {
        if (b) {
            return toJSONObject(reader, XMLParserConfiguration.KEEP_STRINGS);
        }
        return toJSONObject(reader, XMLParserConfiguration.ORIGINAL);
    }
    
    static {
        TYPE_ATTR = "xsi:type";
        NULL_ATTR = "xsi:nil";
        AMP = '&';
        APOS = '\'';
        BANG = '!';
        EQ = '=';
        GT = '>';
        LT = '<';
        QUEST = '?';
        QUOT = '\"';
        SLASH = '/';
    }
    
    private static Iterable<Integer> codePointIterator(final String s) {
        return new Iterable<Integer>(s) {
            final String val$string;
            
            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>(this) {
                    private int nextIndex = 0;
                    final XML$1 this$0;
                    private int length = this.this$0.val$string.length();
                    
                    @Override
                    public Object next() {
                        return this.next();
                    }
                    
                    @Override
                    public Integer next() {
                        final int codePoint = this.this$0.val$string.codePointAt(this.nextIndex);
                        this.nextIndex += Character.charCount(codePoint);
                        return codePoint;
                    }
                    
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                    
                    @Override
                    public boolean hasNext() {
                        return this.nextIndex < this.length;
                    }
                };
            }
        };
    }
    
    public static JSONObject toJSONObject(final String s) throws JSONException {
        return toJSONObject(s, XMLParserConfiguration.ORIGINAL);
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
    
    public static JSONObject toJSONObject(final String s, final boolean b) throws JSONException {
        return toJSONObject(new StringReader(s), b);
    }
}
