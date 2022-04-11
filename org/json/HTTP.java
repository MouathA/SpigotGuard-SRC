package org.json;

import java.util.*;

public class HTTP
{
    public static final String CRLF;
    
    static {
        CRLF = "\r\n";
    }
    
    public static String toString(final JSONObject jsonObject) throws JSONException {
        final StringBuilder sb = new StringBuilder();
        if (jsonObject.has("Status-Code") && jsonObject.has("Reason-Phrase")) {
            sb.append(jsonObject.getString("HTTP-Version"));
            sb.append(' ');
            sb.append(jsonObject.getString("Status-Code"));
            sb.append(' ');
            sb.append(jsonObject.getString("Reason-Phrase"));
        }
        else {
            if (!jsonObject.has("Method") || !jsonObject.has("Request-URI")) {
                throw new JSONException("Not enough material for an HTTP header.");
            }
            sb.append(jsonObject.getString("Method"));
            sb.append(' ');
            sb.append('\"');
            sb.append(jsonObject.getString("Request-URI"));
            sb.append('\"');
            sb.append(' ');
            sb.append(jsonObject.getString("HTTP-Version"));
        }
        sb.append("\r\n");
        for (final String s : jsonObject.keySet()) {
            final String optString = jsonObject.optString(s);
            if (!"HTTP-Version".equals(s) && !"Status-Code".equals(s) && !"Reason-Phrase".equals(s) && !"Method".equals(s) && !"Request-URI".equals(s) && !JSONObject.NULL.equals(optString)) {
                sb.append(s);
                sb.append(": ");
                sb.append(jsonObject.optString(s));
                sb.append("\r\n");
            }
        }
        sb.append("\r\n");
        return String.valueOf(sb);
    }
    
    public static JSONObject toJSONObject(final String s) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        final HTTPTokener httpTokener = new HTTPTokener(s);
        final String nextToken = httpTokener.nextToken();
        if (nextToken.toUpperCase(Locale.ROOT).startsWith("HTTP")) {
            jsonObject.put("HTTP-Version", nextToken);
            jsonObject.put("Status-Code", httpTokener.nextToken());
            jsonObject.put("Reason-Phrase", httpTokener.nextTo('\0'));
            httpTokener.next();
        }
        else {
            jsonObject.put("Method", nextToken);
            jsonObject.put("Request-URI", httpTokener.nextToken());
            jsonObject.put("HTTP-Version", httpTokener.nextToken());
        }
        while (httpTokener.more()) {
            final String nextTo = httpTokener.nextTo(':');
            httpTokener.next(':');
            jsonObject.put(nextTo, httpTokener.nextTo('\0'));
            httpTokener.next();
        }
        return jsonObject;
    }
}
