package org.json;

public class JSONPointerException extends JSONException
{
    private static final long serialVersionUID = 8872944667561856751L;
    
    public JSONPointerException(final String s) {
        super(s);
    }
    
    public JSONPointerException(final String s, final Throwable t) {
        super(s, t);
    }
}
