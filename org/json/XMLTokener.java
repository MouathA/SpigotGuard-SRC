package org.json;

import java.util.*;
import java.io.*;

public class XMLTokener extends JSONTokener
{
    public static final HashMap<String, Character> entity;
    
    public void skipPast(final String s) {
        int n = 0;
        final int length = s.length();
        final char[] array = new char[length];
        for (int i = 0; i < length; ++i) {
            final char next = this.next();
            if (next == '\0') {
                return;
            }
            array[i] = next;
        }
        while (true) {
            int n2 = n;
            boolean b = true;
            for (int j = 0; j < length; ++j) {
                if (array[n2] != s.charAt(j)) {
                    b = false;
                    break;
                }
                if (++n2 >= length) {
                    n2 -= length;
                }
            }
            if (b) {
                return;
            }
            final char next2 = this.next();
            if (next2 == '\0') {
                return;
            }
            array[n] = next2;
            if (++n < length) {
                continue;
            }
            n -= length;
        }
    }
    
    public String nextCDATA() throws JSONException {
        final StringBuilder sb = new StringBuilder();
        while (this.more()) {
            sb.append(this.next());
            final int length = sb.length() - 3;
            if (length >= 0 && sb.charAt(length) == ']' && sb.charAt(length + 1) == ']' && sb.charAt(length + 2) == '>') {
                sb.setLength(length);
                return String.valueOf(sb);
            }
        }
        throw this.syntaxError("Unclosed CDATA");
    }
    
    public Object nextEntity(final char c) throws JSONException {
        final StringBuilder sb = new StringBuilder();
        char next;
        while (true) {
            next = this.next();
            if (!Character.isLetterOrDigit(next) && next != '#') {
                break;
            }
            sb.append(Character.toLowerCase(next));
        }
        if (next == ';') {
            return unescapeEntity(String.valueOf(sb));
        }
        throw this.syntaxError(String.valueOf(new StringBuilder().append("Missing ';' in XML entity: &").append((Object)sb)));
    }
    
    static {
        (entity = new HashMap<String, Character>(8)).put("amp", XML.AMP);
        XMLTokener.entity.put("apos", XML.APOS);
        XMLTokener.entity.put("gt", XML.GT);
        XMLTokener.entity.put("lt", XML.LT);
        XMLTokener.entity.put("quot", XML.QUOT);
    }
    
    public Object nextToken() throws JSONException {
        char c;
        do {
            c = this.next();
        } while (Character.isWhitespace(c));
        switch (c) {
            case '\0': {
                throw this.syntaxError("Misshaped element");
            }
            case '<': {
                throw this.syntaxError("Misplaced '<'");
            }
            case '>': {
                return XML.GT;
            }
            case '/': {
                return XML.SLASH;
            }
            case '=': {
                return XML.EQ;
            }
            case '!': {
                return XML.BANG;
            }
            case '?': {
                return XML.QUEST;
            }
            case '\"':
            case '\'': {
                final char c2 = c;
                final StringBuilder sb = new StringBuilder();
                while (true) {
                    final char next = this.next();
                    if (next == '\0') {
                        throw this.syntaxError("Unterminated string");
                    }
                    if (next == c2) {
                        return String.valueOf(sb);
                    }
                    if (next == '&') {
                        sb.append(this.nextEntity(next));
                    }
                    else {
                        sb.append(next);
                    }
                }
                break;
            }
            default: {
                final StringBuilder sb2 = new StringBuilder();
                while (true) {
                    sb2.append(c);
                    c = this.next();
                    if (Character.isWhitespace(c)) {
                        return String.valueOf(sb2);
                    }
                    switch (c) {
                        case '\0': {
                            return String.valueOf(sb2);
                        }
                        case '!':
                        case '/':
                        case '=':
                        case '>':
                        case '?':
                        case '[':
                        case ']': {
                            this.back();
                            return String.valueOf(sb2);
                        }
                        case '\"':
                        case '\'':
                        case '<': {
                            throw this.syntaxError("Bad character in a name");
                        }
                        default: {
                            continue;
                        }
                    }
                }
                break;
            }
        }
    }
    
    public Object nextMeta() throws JSONException {
        char next;
        do {
            next = this.next();
        } while (Character.isWhitespace(next));
        switch (next) {
            case '\0': {
                throw this.syntaxError("Misshaped meta tag");
            }
            case '<': {
                return XML.LT;
            }
            case '>': {
                return XML.GT;
            }
            case '/': {
                return XML.SLASH;
            }
            case '=': {
                return XML.EQ;
            }
            case '!': {
                return XML.BANG;
            }
            case '?': {
                return XML.QUEST;
            }
            case '\"':
            case '\'': {
                char next2;
                do {
                    next2 = this.next();
                    if (next2 == '\0') {
                        throw this.syntaxError("Unterminated string");
                    }
                } while (next2 != next);
                return Boolean.TRUE;
            }
            default: {
                while (true) {
                    final char next3 = this.next();
                    if (Character.isWhitespace(next3)) {
                        return Boolean.TRUE;
                    }
                    switch (next3) {
                        case '\0': {
                            throw this.syntaxError("Unterminated string");
                        }
                        case '!':
                        case '\"':
                        case '\'':
                        case '/':
                        case '<':
                        case '=':
                        case '>':
                        case '?': {
                            this.back();
                            return Boolean.TRUE;
                        }
                        default: {
                            continue;
                        }
                    }
                }
                break;
            }
        }
    }
    
    public XMLTokener(final String s) {
        super(s);
    }
    
    static String unescapeEntity(final String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        if (s.charAt(0) == '#') {
            int n;
            if (s.charAt(1) == 'x' || s.charAt(1) == 'X') {
                n = Integer.parseInt(s.substring(2), 16);
            }
            else {
                n = Integer.parseInt(s.substring(1));
            }
            return new String(new int[] { n }, 0, 1);
        }
        final Character c = XMLTokener.entity.get(s);
        if (c == null) {
            return String.valueOf(new StringBuilder().append('&').append(s).append(';'));
        }
        return c.toString();
    }
    
    public Object nextContent() throws JSONException {
        char c;
        do {
            c = this.next();
        } while (Character.isWhitespace(c));
        if (c == '\0') {
            return null;
        }
        if (c == '<') {
            return XML.LT;
        }
        final StringBuilder sb = new StringBuilder();
        while (c != '\0') {
            if (c == '<') {
                this.back();
                return String.valueOf(sb).trim();
            }
            if (c == '&') {
                sb.append(this.nextEntity(c));
            }
            else {
                sb.append(c);
            }
            c = this.next();
        }
        return String.valueOf(sb).trim();
    }
    
    public XMLTokener(final Reader reader) {
        super(reader);
    }
}
