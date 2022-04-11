package xyz.yooniks.spigotguard.helper;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.inventory.*;

public class CustomBookOpenEvent extends Event implements Cancellable
{
    private final Player player;
    private static final HandlerList handlers;
    private boolean cancelled;
    private Hand hand;
    private ItemStack book;
    
    public void setBook(final ItemStack book) {
        this.book = book;
    }
    
    public void setHand(final Hand hand) {
        this.hand = hand;
    }
    
    public HandlerList getHandlers() {
        return CustomBookOpenEvent.handlers;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public Hand getHand() {
        return this.hand;
    }
    
    public ItemStack getBook() {
        return this.book;
    }
    
    public CustomBookOpenEvent(final Player player, final ItemStack book, final boolean b) {
        this.player = player;
        this.book = book;
        this.hand = (b ? Hand.OFF_HAND : Hand.MAIN_HAND);
    }
    
    public static HandlerList getHandlerList() {
        return CustomBookOpenEvent.handlers;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    static {
        handlers = new HandlerList();
    }
    
    public enum Hand
    {
        OFF_HAND, 
        MAIN_HAND;
        
        private static final Hand[] $VALUES;
        
        static {
            $VALUES = $values();
        }
        
        private static Hand[] $values() {
            return new Hand[] { Hand.MAIN_HAND, Hand.OFF_HAND };
        }
    }
}
