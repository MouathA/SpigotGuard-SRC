package org.json;

public class HTTPTokener extends JSONTokener
{
    public HTTPTokener(final String s) {
        super(s);
    }
    
    public String nextToken() throws JSONException {
        final StringBuilder sb = new StringBuilder();
        char c;
        do {
            c = this.next();
        } while (Character.isWhitespace(c));
        if (c != '\"' && c != '\'') {
            while (c != '\0' && !Character.isWhitespace(c)) {
                sb.append(c);
                c = this.next();
            }
            return String.valueOf(sb);
        }
        final char c2 = c;
        while (true) {
            final char next = this.next();
            if (next < ' ') {
                throw this.syntaxError("Unterminated string.");
            }
            if (next == c2) {
                return String.valueOf(sb);
            }
            sb.append(next);
        }
    }
}
