package xyz.yooniks.spigotguard.listener;

import org.bukkit.event.player.*;
import org.bukkit.event.*;

public class FaweTabCompletionListener implements Listener
{
    @EventHandler
    public void onPlayerChatTabComplete(final PlayerChatTabCompleteEvent playerChatTabCompleteEvent) {
        if (playerChatTabCompleteEvent.getChatMessage() == null) {
            return;
        }
        final String lowerCase = playerChatTabCompleteEvent.getChatMessage().toLowerCase();
        if (lowerCase.isEmpty()) {
            return;
        }
        try {
            final String s = lowerCase.split(" ")[0];
            if (s.startsWith("/to") || s.startsWith("/fastasyncworldedit:to")) {
                playerChatTabCompleteEvent.getTabCompletions().clear();
            }
        }
        catch (Exception ex) {}
    }
}
