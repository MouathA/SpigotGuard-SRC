package xyz.yooniks.spigotguard.api.inventory;

import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.configuration.*;
import org.bukkit.*;
import org.bukkit.enchantments.*;
import java.util.*;

public class ItemBuilder
{
    private ItemStack item;
    
    public ItemBuilder withLore(final List<String> list) {
        final ItemMeta itemMeta = this.item.getItemMeta();
        itemMeta.setLore((List)MessageHelper.colored(list));
        this.item.setItemMeta(itemMeta);
        return this;
    }
    
    public static ItemBuilder withSection(final ConfigurationSection configurationSection) {
        if (configurationSection == null) {
            return new ItemBuilder(Material.GRASS).withName("Section is null");
        }
        final ItemBuilder itemBuilder = new ItemBuilder(Material.GRASS);
        if (configurationSection.isString("material")) {
            final Material matchMaterial = Material.matchMaterial(configurationSection.getString("material"));
            if (matchMaterial != null) {
                itemBuilder.withType(matchMaterial);
            }
        }
        if (configurationSection.isList("lore")) {
            itemBuilder.withLore(configurationSection.getStringList("lore"));
        }
        if (configurationSection.isString("name")) {
            itemBuilder.withName(configurationSection.getString("name"));
        }
        if (configurationSection.isInt("amount")) {
            itemBuilder.withAmount(configurationSection.getInt("amount"));
        }
        if (configurationSection.isInt("data")) {
            itemBuilder.withDurability((short)configurationSection.getInt("data"));
        }
        if (configurationSection.isList("enchants")) {
            final Iterator<String> iterator = (Iterator<String>)configurationSection.getStringList("enchants").iterator();
            while (iterator.hasNext()) {
                final String[] split = iterator.next().split(";");
                if (split.length < 1) {
                    continue;
                }
                final Enchantment byName = Enchantment.getByName(split[0]);
                if (byName == null) {
                    continue;
                }
                int int1;
                try {
                    int1 = Integer.parseInt(split[1]);
                }
                catch (NumberFormatException ex) {
                    continue;
                }
                itemBuilder.addEnchantment(byName, int1);
            }
        }
        return itemBuilder;
    }
    
    public ItemBuilder addEnchantment(final Enchantment enchantment, final int n) {
        final ItemMeta itemMeta = this.item.getItemMeta();
        itemMeta.addEnchant(enchantment, n, true);
        this.item.setItemMeta(itemMeta);
        return this;
    }
    
    public ItemStack build() {
        return this.item;
    }
    
    public ItemBuilder(final ItemStack item) {
        this.item = item;
    }
    
    public ItemBuilder(final Material material) {
        if (material == Material.AIR) {
            throw new IllegalArgumentException("Material cannot be AIR!");
        }
        this.item = new ItemStack(material);
    }
    
    public ItemBuilder withDurability(final short durability) {
        this.item.setDurability(durability);
        return this;
    }
    
    public ItemBuilder(final Material material, final int n, final short n2) {
        if (material == Material.AIR) {
            throw new IllegalArgumentException("Material cannot be AIR!");
        }
        this.item = new ItemStack(material, n, n2);
    }
    
    public ItemBuilder withType(final Material type) {
        if (type == Material.AIR) {
            throw new IllegalArgumentException("Material cannot be AIR!");
        }
        if (this.item == null) {
            this.item = new ItemStack(type);
            return this;
        }
        this.item.setType(type);
        return this;
    }
    
    public ItemBuilder withAmount(final int amount) {
        this.item.setAmount(amount);
        return this;
    }
    
    public ItemBuilder withName(final String s) {
        final ItemMeta itemMeta = this.item.getItemMeta();
        itemMeta.setDisplayName(MessageHelper.colored(s));
        this.item.setItemMeta(itemMeta);
        return this;
    }
    
    public ItemBuilder withLore(final String... array) {
        return this.withLore(Arrays.asList(array));
    }
}
