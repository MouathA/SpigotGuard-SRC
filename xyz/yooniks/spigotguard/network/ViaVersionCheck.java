package xyz.yooniks.spigotguard.network;

import org.bukkit.entity.*;
import us.myles.ViaVersion.*;

public class ViaVersionCheck
{
    private static boolean viaVersionOld;
    private static boolean viaVersionNew;
    
    public static boolean shouldChangeCompressor(final Player player) {
        if (ViaVersionCheck.viaVersionOld) {
            try {
                final int playerVersion = ViaVersionPlugin.getInstance().getApi().getPlayerVersion((Object)player);
                System.out.println(String.valueOf(new StringBuilder().append("[SG-Old VV] Player ").append(player.getName()).append(" version: ").append(playerVersion)));
                return playerVersion == 47;
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (ViaVersionCheck.viaVersionNew) {
            try {
                final int playerVersion2 = com.viaversion.viaversion.ViaVersionPlugin.getInstance().getApi().getPlayerVersion((Object)player);
                System.out.println(String.valueOf(new StringBuilder().append("[SG-New VV] Player ").append(player.getName()).append(" version: ").append(playerVersion2)));
                return playerVersion2 == 47;
            }
            catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
        return false;
    }
    
    static {
        ViaVersionCheck.viaVersionOld = false;
        ViaVersionCheck.viaVersionNew = false;
        try {
            Class.forName("us.myles.ViaVersion.ViaVersionPlugin");
            ViaVersionCheck.viaVersionOld = true;
        }
        catch (Exception ex) {
            ViaVersionCheck.viaVersionOld = false;
        }
        try {
            Class.forName("com.viaversion.viaversion.ViaVersionPlugin");
            ViaVersionCheck.viaVersionNew = true;
        }
        catch (Exception ex2) {
            ViaVersionCheck.viaVersionNew = false;
        }
    }
}
