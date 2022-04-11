package xyz.yooniks.spigotguard.nms;

public enum NMSVersion
{
    ONE_DOT_EIGHT_R3("v1_8", 0), 
    ONE_DOT_NINE_R2("v1_9", 1), 
    ONE_DOT_THIRTEEN("v1_13", 3), 
    ONE_DOT_SIXTEEN_R2("v1_16_R2", 7), 
    ONE_DOT_FIVETEEN("v1_15", 5), 
    ONE_DOT_FOURTEEN("v1_14", 4);
    
    int version;
    private static final NMSVersion[] $VALUES;
    String name;
    
    ONE_DOT_SEVENTEEN_R1("v1_17_R1", 9), 
    UNSUPPORTED("unsupported", 10), 
    ONE_DOT_SIXTEEN("v1_16", 6), 
    ONE_DOT_SIXTEEN_R3("v1_16_R3", 8), 
    ONE_DOT_TVELVE_R1("v1_12", 2), 
    ONE_DOT_SEVEN_R4("v1_7", -1);
    
    public static NMSVersion find(final String s) {
        NMSVersion unsupported = null;
        for (final NMSVersion nmsVersion : values()) {
            if (s.contains(nmsVersion.name)) {
                unsupported = nmsVersion;
            }
        }
        if (unsupported == null) {
            unsupported = NMSVersion.UNSUPPORTED;
        }
        return unsupported;
    }
    
    public int getVersion() {
        return this.version;
    }
    
    public String getName() {
        return this.name;
    }
    
    private static NMSVersion[] $values() {
        return new NMSVersion[] { NMSVersion.ONE_DOT_SEVEN_R4, NMSVersion.ONE_DOT_EIGHT_R3, NMSVersion.ONE_DOT_NINE_R2, NMSVersion.ONE_DOT_TVELVE_R1, NMSVersion.ONE_DOT_THIRTEEN, NMSVersion.ONE_DOT_FOURTEEN, NMSVersion.ONE_DOT_FIVETEEN, NMSVersion.ONE_DOT_SIXTEEN, NMSVersion.ONE_DOT_SIXTEEN_R2, NMSVersion.ONE_DOT_SIXTEEN_R3, NMSVersion.ONE_DOT_SEVENTEEN_R1, NMSVersion.UNSUPPORTED };
    }
    
    static {
        $VALUES = $values();
    }
    
    private NMSVersion(final String name, final int version) {
        this.name = name;
        this.version = version;
    }
}
