package org.json;

import java.util.*;

public class CookieList
{
    public static JSONObject toJSONObject(final String s) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        final JSONTokener jsonTokener = new JSONTokener(s);
        while (jsonTokener.more()) {
            final String unescape = Cookie.unescape(jsonTokener.nextTo('='));
            jsonTokener.next('=');
            jsonObject.put(unescape, Cookie.unescape(jsonTokener.nextTo(';')));
            jsonTokener.next();
        }
        return jsonObject;
    }
    
    public static String toString(final JSONObject jsonObject) throws JSONException {
        int n = 0;
        final StringBuilder sb = new StringBuilder();
        for (final String s : jsonObject.keySet()) {
            final Object opt = jsonObject.opt(s);
            if (!JSONObject.NULL.equals(opt)) {
                if (n != 0) {
                    sb.append(';');
                }
                sb.append(Cookie.escape(s));
                sb.append("=");
                sb.append(Cookie.escape(opt.toString()));
                n = 1;
            }
        }
        return String.valueOf(sb);
    }
}
