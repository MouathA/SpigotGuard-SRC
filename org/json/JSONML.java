package org.json;

import java.util.*;

public class JSONML
{
    public static JSONArray toJSONArray(final String s) throws JSONException {
        return (JSONArray)parse(new XMLTokener(s), true, null, false);
    }
    
    public static JSONArray toJSONArray(final XMLTokener xmlTokener, final boolean b) throws JSONException {
        return (JSONArray)parse(xmlTokener, true, null, b);
    }
    
    private static Object parse(final XMLTokener xmlTokener, final boolean b, final JSONArray jsonArray, final boolean b2) throws JSONException {
        while (xmlTokener.more()) {
            final Object nextContent = xmlTokener.nextContent();
            if (nextContent == XML.LT) {
                final Object nextToken = xmlTokener.nextToken();
                if (nextToken instanceof Character) {
                    if (nextToken == XML.SLASH) {
                        final Object nextToken2 = xmlTokener.nextToken();
                        if (!(nextToken2 instanceof String)) {
                            throw new JSONException(String.valueOf(new StringBuilder().append("Expected a closing name instead of '").append(nextToken2).append("'.")));
                        }
                        if (xmlTokener.nextToken() != XML.GT) {
                            throw xmlTokener.syntaxError("Misshaped close tag");
                        }
                        return nextToken2;
                    }
                    else if (nextToken == XML.BANG) {
                        final char next = xmlTokener.next();
                        if (next == '-') {
                            if (xmlTokener.next() == '-') {
                                xmlTokener.skipPast("-->");
                            }
                            else {
                                xmlTokener.back();
                            }
                        }
                        else if (next == '[') {
                            if (!xmlTokener.nextToken().equals("CDATA") || xmlTokener.next() != '[') {
                                throw xmlTokener.syntaxError("Expected 'CDATA['");
                            }
                            if (jsonArray == null) {
                                continue;
                            }
                            jsonArray.put(xmlTokener.nextCDATA());
                        }
                        else {
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
                        }
                    }
                    else {
                        if (nextToken != XML.QUEST) {
                            throw xmlTokener.syntaxError("Misshaped tag");
                        }
                        xmlTokener.skipPast("?>");
                    }
                }
                else {
                    if (!(nextToken instanceof String)) {
                        throw xmlTokener.syntaxError(String.valueOf(new StringBuilder().append("Bad tagName '").append(nextToken).append("'.")));
                    }
                    final String s = (String)nextToken;
                    final JSONArray jsonArray2 = new JSONArray();
                    final JSONObject jsonObject = new JSONObject();
                    if (b) {
                        jsonArray2.put(s);
                        if (jsonArray != null) {
                            jsonArray.put(jsonArray2);
                        }
                    }
                    else {
                        jsonObject.put("tagName", s);
                        if (jsonArray != null) {
                            jsonArray.put(jsonObject);
                        }
                    }
                    Object o = null;
                    while (true) {
                        if (o == null) {
                            o = xmlTokener.nextToken();
                        }
                        if (o == null) {
                            throw xmlTokener.syntaxError("Misshaped tag");
                        }
                        if (!(o instanceof String)) {
                            if (b && jsonObject.length() > 0) {
                                jsonArray2.put(jsonObject);
                            }
                            if (o == XML.SLASH) {
                                if (xmlTokener.nextToken() != XML.GT) {
                                    throw xmlTokener.syntaxError("Misshaped tag");
                                }
                                if (jsonArray != null) {
                                    break;
                                }
                                if (b) {
                                    return jsonArray2;
                                }
                                return jsonObject;
                            }
                            else {
                                if (o != XML.GT) {
                                    throw xmlTokener.syntaxError("Misshaped tag");
                                }
                                final String s2 = (String)parse(xmlTokener, b, jsonArray2, b2);
                                if (s2 == null) {
                                    break;
                                }
                                if (!s2.equals(s)) {
                                    throw xmlTokener.syntaxError(String.valueOf(new StringBuilder().append("Mismatched '").append(s).append("' and '").append(s2).append("'")));
                                }
                                if (!b && jsonArray2.length() > 0) {
                                    jsonObject.put("childNodes", jsonArray2);
                                }
                                if (jsonArray != null) {
                                    break;
                                }
                                if (b) {
                                    return jsonArray2;
                                }
                                return jsonObject;
                            }
                        }
                        else {
                            final String s3 = (String)o;
                            if (!b && ("tagName".equals(s3) || "childNode".equals(s3))) {
                                throw xmlTokener.syntaxError("Reserved attribute.");
                            }
                            o = xmlTokener.nextToken();
                            if (o == XML.EQ) {
                                final Object nextToken3 = xmlTokener.nextToken();
                                if (!(nextToken3 instanceof String)) {
                                    throw xmlTokener.syntaxError("Missing value");
                                }
                                jsonObject.accumulate(s3, b2 ? ((String)nextToken3) : XML.stringToValue((String)nextToken3));
                                o = null;
                            }
                            else {
                                jsonObject.accumulate(s3, "");
                            }
                        }
                    }
                }
            }
            else {
                if (jsonArray == null) {
                    continue;
                }
                jsonArray.put((nextContent instanceof String) ? (b2 ? XML.unescape((String)nextContent) : XML.stringToValue((String)nextContent)) : nextContent);
            }
        }
        throw xmlTokener.syntaxError("Bad XML");
    }
    
    public static JSONObject toJSONObject(final String s, final boolean b) throws JSONException {
        return (JSONObject)parse(new XMLTokener(s), false, null, b);
    }
    
    public static JSONObject toJSONObject(final XMLTokener xmlTokener) throws JSONException {
        return (JSONObject)parse(xmlTokener, false, null, false);
    }
    
    public static JSONObject toJSONObject(final String s) throws JSONException {
        return (JSONObject)parse(new XMLTokener(s), false, null, false);
    }
    
    public static String toString(final JSONArray jsonArray) throws JSONException {
        final StringBuilder sb = new StringBuilder();
        final String string = jsonArray.getString(0);
        XML.noSpace(string);
        final String escape = XML.escape(string);
        sb.append('<');
        sb.append(escape);
        final Object opt = jsonArray.opt(1);
        int i;
        if (opt instanceof JSONObject) {
            i = 2;
            final JSONObject jsonObject = (JSONObject)opt;
            for (final String s : jsonObject.keySet()) {
                final Object opt2 = jsonObject.opt(s);
                XML.noSpace(s);
                if (opt2 != null) {
                    sb.append(' ');
                    sb.append(XML.escape(s));
                    sb.append('=');
                    sb.append('\"');
                    sb.append(XML.escape(opt2.toString()));
                    sb.append('\"');
                }
            }
        }
        else {
            i = 1;
        }
        final int length = jsonArray.length();
        if (i >= length) {
            sb.append('/');
            sb.append('>');
        }
        else {
            sb.append('>');
            do {
                final Object value = jsonArray.get(i);
                ++i;
                if (value != null) {
                    if (value instanceof String) {
                        sb.append(XML.escape(value.toString()));
                    }
                    else if (value instanceof JSONObject) {
                        sb.append(toString((JSONObject)value));
                    }
                    else if (value instanceof JSONArray) {
                        sb.append(toString((JSONArray)value));
                    }
                    else {
                        sb.append(value.toString());
                    }
                }
            } while (i < length);
            sb.append('<');
            sb.append('/');
            sb.append(escape);
            sb.append('>');
        }
        return String.valueOf(sb);
    }
    
    public static JSONArray toJSONArray(final String s, final boolean b) throws JSONException {
        return (JSONArray)parse(new XMLTokener(s), true, null, b);
    }
    
    public static String toString(final JSONObject jsonObject) throws JSONException {
        final StringBuilder sb = new StringBuilder();
        final String optString = jsonObject.optString("tagName");
        if (optString == null) {
            return XML.escape(jsonObject.toString());
        }
        XML.noSpace(optString);
        final String escape = XML.escape(optString);
        sb.append('<');
        sb.append(escape);
        for (final String s : jsonObject.keySet()) {
            if (!"tagName".equals(s) && !"childNodes".equals(s)) {
                XML.noSpace(s);
                final Object opt = jsonObject.opt(s);
                if (opt == null) {
                    continue;
                }
                sb.append(' ');
                sb.append(XML.escape(s));
                sb.append('=');
                sb.append('\"');
                sb.append(XML.escape(opt.toString()));
                sb.append('\"');
            }
        }
        final JSONArray optJSONArray = jsonObject.optJSONArray("childNodes");
        if (optJSONArray == null) {
            sb.append('/');
            sb.append('>');
        }
        else {
            sb.append('>');
            for (int length = optJSONArray.length(), i = 0; i < length; ++i) {
                final Object value = optJSONArray.get(i);
                if (value != null) {
                    if (value instanceof String) {
                        sb.append(XML.escape(value.toString()));
                    }
                    else if (value instanceof JSONObject) {
                        sb.append(toString((JSONObject)value));
                    }
                    else if (value instanceof JSONArray) {
                        sb.append(toString((JSONArray)value));
                    }
                    else {
                        sb.append(value.toString());
                    }
                }
            }
            sb.append('<');
            sb.append('/');
            sb.append(escape);
            sb.append('>');
        }
        return String.valueOf(sb);
    }
    
    public static JSONObject toJSONObject(final XMLTokener xmlTokener, final boolean b) throws JSONException {
        return (JSONObject)parse(xmlTokener, false, null, b);
    }
    
    public static JSONArray toJSONArray(final XMLTokener xmlTokener) throws JSONException {
        return (JSONArray)parse(xmlTokener, true, null, false);
    }
}
