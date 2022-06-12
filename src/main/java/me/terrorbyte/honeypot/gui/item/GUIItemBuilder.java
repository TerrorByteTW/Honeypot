package me.terrorbyte.honeypot.gui.item;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.md_5.bungee.api.ChatColor;

public class GUIItemBuilder {
    private final ItemStack stack;

    public GUIItemBuilder(Material material) {
        this.stack = new ItemStack(material);
    }

    public GUIItemBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public GUIItemBuilder type(Material material) {
        stack.setType(material);
        return this;
    }

    public Material getType() {
        return stack.getType();
    }

    public GUIItemBuilder name(String name) {
        ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        stack.setItemMeta(stackMeta);
        return this;
    }

    public String getName() {
        if (!stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName())
            return null;
        return stack.getItemMeta().getDisplayName();
    }

    public GUIItemBuilder amount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public int getAmount() {
        return stack.getAmount();
    }

    public GUIItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public GUIItemBuilder lore(List<String> lore) {
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
        }

        ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setLore(lore);
        stack.setItemMeta(stackMeta);
        return this;
    }

    public List<String> getLore() {
        if (!stack.hasItemMeta() || !stack.getItemMeta().hasLore())
            return Collections.emptyList();
        return stack.getItemMeta().getLore();
    }

    public GUIItemBuilder enchant(Enchantment enchantment, int level) {
        stack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public GUIItemBuilder unenchant(Enchantment enchantment) {
        stack.removeEnchantment(enchantment);
        return this;
    }

    public GUIItemBuilder flag(ItemFlag... flag) {
        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(flag);
        stack.setItemMeta(meta);
        return this;
    }

    public GUIItemBuilder deflag(ItemFlag... flag) {
        ItemMeta meta = stack.getItemMeta();
        meta.removeItemFlags(flag);
        stack.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return get();
    }

    public ItemStack get() {
        return stack;
    }

}
