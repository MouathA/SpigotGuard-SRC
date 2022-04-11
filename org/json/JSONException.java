package org.json;

public class JSONException extends RuntimeException
{
    private static final long serialVersionUID = 0L;
    
    public JSONException(final String s, final Throwable t) {
        super(s, t);
    }
    
    public JSONException(final Throwable t) {
        super(t.getMessage(), t);
    }
    
    public JSONException(final String s) {
        super(s);
    }
}
