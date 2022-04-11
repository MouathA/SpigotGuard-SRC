package xyz.yooniks.spigotguard.classloader;

import java.io.*;

public class SpigotGuardClassLoaded implements Serializable
{
    private final Serializable serializable;
    static final long serialVersionUID = 423116223121231L;
    
    public Serializable getSerializable() {
        return this.serializable;
    }
    
    public SpigotGuardClassLoaded(final Serializable serializable) {
        this.serializable = serializable;
    }
}
