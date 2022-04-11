package org.json;

import java.util.*;
import java.io.*;

public class Cookie
{
    public static String escape(final String s) {
        final String trim = s.trim();
        final int length = trim.length();
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            final char char1 = trim.charAt(i);
            if (char1 < ' ' || char1 == '+' || char1 == '%' || char1 == '=' || char1 == ';') {
                sb.append('%');
                sb.append(Character.forDigit((char)(char1 >>> 4 & 0xF), 16));
                sb.append(Character.forDigit((char)(char1 & '\u000f'), 16));
            }
            else {
                sb.append(char1);
            }
        }
        return String.valueOf(sb);
    }
    
    public static String unescape(final String s) {
        final int length = s.length();
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            char char1 = s.charAt(i);
            if (char1 == '+') {
                char1 = ' ';
            }
            else if (char1 == '%' && i + 2 < length) {
                final int dehexchar = JSONTokener.dehexchar(s.charAt(i + 1));
                final int dehexchar2 = JSONTokener.dehexchar(s.charAt(i + 2));
                if (dehexchar >= 0 && dehexchar2 >= 0) {
                    char1 = (char)(dehexchar * 16 + dehexchar2);
                    i += 2;
                }
            }
            sb.append(char1);
        }
        return String.valueOf(sb);
    }
    
    public static String toString(final JSONObject jsonObject) throws JSONException {
        final StringBuilder sb = new StringBuilder();
        String trim = null;
        String trim2 = null;
        for (final String s : jsonObject.keySet()) {
            if (Integer.valueOf(s.toUpperCase().hashCode()).equals("name".toUpperCase().hashCode())) {
                trim = jsonObject.getString(s).trim();
            }
            if (Integer.valueOf(s.toUpperCase().hashCode()).equals("value".toUpperCase().hashCode())) {
                trim2 = jsonObject.getString(s).trim();
            }
            if (trim != null && trim2 != null) {
                break;
            }
        }
        if (trim == null || "".equals(trim.trim())) {
            throw new JSONException("Cookie does not have a name");
        }
        if (trim2 == null) {
            trim2 = "";
        }
        sb.append(escape(trim));
        sb.append("=");
        sb.append(escape(trim2));
        for (final String s2 : jsonObject.keySet()) {
            if (!Integer.valueOf(s2.toUpperCase().hashCode()).equals("name".toUpperCase().hashCode())) {
                if (Integer.valueOf(s2.toUpperCase().hashCode()).equals("value".toUpperCase().hashCode())) {
                    continue;
                }
                final Object opt = jsonObject.opt(s2);
                if (opt instanceof Boolean) {
                    if (!Boolean.TRUE.equals(opt)) {
                        continue;
                    }
                    sb.append(';').append(escape(s2));
                }
                else {
                    sb.append(';').append(escape(s2)).append('=').append(escape(opt.toString()));
                }
            }
        }
        return String.valueOf(sb);
    }
    
    public static JSONObject toJSONObject(final String s) {
        final JSONObject jsonObject = new JSONObject();
        final JSONTokener jsonTokener = new JSONTokener(s);
        final String unescape = unescape(jsonTokener.nextTo('=').trim());
        if ("".equals(unescape)) {
            throw new JSONException("Cookies must have a 'name'");
        }
        jsonObject.put("name", unescape);
        jsonTokener.next('=');
        jsonObject.put("value", unescape(jsonTokener.nextTo(';')).trim());
        jsonTokener.next();
        while (jsonTokener.more()) {
            final String lowerCase = unescape(jsonTokener.nextTo("=;")).trim().toLowerCase(Locale.ROOT);
            if (Integer.valueOf(lowerCase.toUpperCase().hashCode()).equals("name".toUpperCase().hashCode())) {
                throw new JSONException("Illegal attribute name: 'name'");
            }
            if (Integer.valueOf(lowerCase.toUpperCase().hashCode()).equals("value".toUpperCase().hashCode())) {
                throw new JSONException("Illegal attribute name: 'value'");
            }
            Serializable s2;
            if (jsonTokener.next() != '=') {
                s2 = Boolean.TRUE;
            }
            else {
                s2 = unescape(jsonTokener.nextTo(';')).trim();
                jsonTokener.next();
            }
            if ("".equals(lowerCase) || "".equals(s2)) {
                continue;
            }
            jsonObject.put(lowerCase, s2);
        }
        return jsonObject;
    }
}
