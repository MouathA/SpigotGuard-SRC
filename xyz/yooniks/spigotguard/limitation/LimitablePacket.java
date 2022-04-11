package xyz.yooniks.spigotguard.limitation;

public class LimitablePacket
{
    private final boolean cancelOnly;
    private final int limit;
    
    public LimitablePacket(final int limit, final boolean cancelOnly) {
        this.limit = limit;
        this.cancelOnly = cancelOnly;
    }
    
    public boolean isCancelOnly() {
        return this.cancelOnly;
    }
    
    public int getLimit() {
        return this.limit;
    }
}
