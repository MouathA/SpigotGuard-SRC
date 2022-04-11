package xyz.yooniks.spigotguard.api.inventory;

import java.util.*;

public interface PhasmatosInventoryAPI
{
    List<PhasmatosInventory> getInventories();
    
    PhasmatosInventory findByTitleAndSize(final String p0, final int p1);
    
    PhasmatosInventory findByTitle(final String p0);
    
    void addInventory(final PhasmatosInventory p0);
}
