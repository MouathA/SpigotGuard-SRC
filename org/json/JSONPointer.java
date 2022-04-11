package org.json;

import java.io.*;
import java.util.*;
import java.net.*;

public class JSONPointer
{
    private final List<String> refTokens;
    private static final String ENCODING;
    
    public Object queryFrom(final Object o) throws JSONPointerException {
        if (this.refTokens.isEmpty()) {
            return o;
        }
        Object o2 = o;
        for (final String s : this.refTokens) {
            if (o2 instanceof JSONObject) {
                o2 = ((JSONObject)o2).opt(unescape(s));
            }
            else {
                if (!(o2 instanceof JSONArray)) {
                    throw new JSONPointerException(String.format("value [%s] is not an array or object therefore its key %s cannot be resolved", o2, s));
                }
                o2 = readByIndexToken(o2, s);
            }
        }
        return o2;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("");
        final Iterator<String> iterator = this.refTokens.iterator();
        while (iterator.hasNext()) {
            sb.append('/').append(escape(iterator.next()));
        }
        return String.valueOf(sb);
    }
    
    static {
        ENCODING = "utf-8";
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public JSONPointer(final String s) {
        if (s == null) {
            throw new NullPointerException("pointer cannot be null");
        }
        if (s.isEmpty() || s.equals("#")) {
            this.refTokens = Collections.emptyList();
            return;
        }
        String s2 = null;
        Label_0109: {
            if (s.startsWith("#/")) {
                final String substring = s.substring(2);
                try {
                    s2 = URLDecoder.decode(substring, "utf-8");
                    break Label_0109;
                }
                catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (!s.startsWith("/")) {
                throw new IllegalArgumentException("a JSON pointer should start with '/' or '#/'");
            }
            s2 = s.substring(1);
        }
        this.refTokens = new ArrayList<String>();
        int i = -1;
        do {
            final int n = i + 1;
            i = s2.indexOf(47, n);
            if (n == i || n == s2.length()) {
                this.refTokens.add("");
            }
            else if (i >= 0) {
                this.refTokens.add(unescape(s2.substring(n, i)));
            }
            else {
                this.refTokens.add(unescape(s2.substring(n)));
            }
        } while (i >= 0);
    }
    
    private static String unescape(final String s) {
        return s.replace("~1", "/").replace("~0", "~");
    }
    
    private static Object readByIndexToken(final Object o, final String s) throws JSONPointerException {
        try {
            final int int1 = Integer.parseInt(s);
            final JSONArray jsonArray = (JSONArray)o;
            if (int1 >= jsonArray.length()) {
                throw new JSONPointerException(String.format("index %s is out of bounds - the array has %d elements", s, jsonArray.length()));
            }
            try {
                return jsonArray.get(int1);
            }
            catch (JSONException ex) {
                throw new JSONPointerException(String.valueOf(new StringBuilder().append("Error reading value at index position ").append(int1)), ex);
            }
        }
        catch (NumberFormatException ex2) {
            throw new JSONPointerException(String.format("%s is not an array index", s), ex2);
        }
    }
    
    public JSONPointer(final List<String> list) {
        this.refTokens = new ArrayList<String>(list);
    }
    
    public String toURIFragment() {
        try {
            final StringBuilder sb = new StringBuilder("#");
            final Iterator<String> iterator = this.refTokens.iterator();
            while (iterator.hasNext()) {
                sb.append('/').append(URLEncoder.encode(iterator.next(), "utf-8"));
            }
            return String.valueOf(sb);
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static String escape(final String s) {
        return s.replace("~", "~0").replace("/", "~1");
    }
    
    public static class Builder
    {
        private final List<String> refTokens;
        
        public Builder append(final String s) {
            if (s == null) {
                throw new NullPointerException("token cannot be null");
            }
            this.refTokens.add(s);
            return this;
        }
        
        public Builder() {
            this.refTokens = new ArrayList<String>();
        }
        
        public JSONPointer build() {
            return new JSONPointer(this.refTokens);
        }
        
        public Builder append(final int n) {
            this.refTokens.add(String.valueOf(n));
            return this;
        }
    }
}
