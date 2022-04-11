package xyz.yooniks.spigotguard;

public class SpigotGuardInternalException extends Exception
{
    public SpigotGuardInternalException(final String s, final Throwable t, final boolean b, final boolean b2) {
        super(s, t, b, b2);
    }
    
    public SpigotGuardInternalException(final String s) {
        super(s);
    }
    
    public SpigotGuardInternalException(final String s, final Throwable t) {
        super(s, t);
    }
    
    public SpigotGuardInternalException(final Throwable t) {
        super(t);
    }
}
