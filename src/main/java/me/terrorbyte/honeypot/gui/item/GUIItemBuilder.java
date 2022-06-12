package me.terrorbyte.honeypot.gui.item;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.md_5.bungee.api.ChatColor;

public class GUIItemBuilder {
	private final ItemStack STACK;

	public GUIItemBuilder (Material material) {
        this.STACK = new ItemStack(material);
    }

	public GUIItemBuilder (ItemStack stack) {
        this.STACK = stack;
    }

	public GUIItemBuilder type(Material material) {
        STACK.setType(material);
        return this;
    }

	public Material getType() {
        return STACK.getType();
    }

	public GUIItemBuilder name(String name) {
        ItemMeta stackMeta = STACK.getItemMeta();
        stackMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        STACK.setItemMeta(stackMeta);
        return this;
    }

	public String getName() {
        if (!STACK.hasItemMeta() || !STACK.getItemMeta().hasDisplayName()) return null;
        return STACK.getItemMeta().getDisplayName();
    }

	public GUIItemBuilder amount(int amount) {
        STACK.setAmount(amount);
        return this;
    }

	public int getAmount() {
        return STACK.getAmount();
    }

	public GUIItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

	public GUIItemBuilder lore(List<String> lore) {
        for(int i = 0; i < lore.size(); i++){
            lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
        }

        ItemMeta stackMeta = STACK.getItemMeta();
        stackMeta.setLore(lore);
        STACK.setItemMeta(stackMeta);
        return this;
    }

	public List<String> getLore() {
        if (!STACK.hasItemMeta() || !STACK.getItemMeta().hasLore()) return null;
        return STACK.getItemMeta().getLore();
    }

	public GUIItemBuilder enchant(Enchantment enchantment, int level) {
        STACK.addUnsafeEnchantment(enchantment, level);
        return this;
    }

	public GUIItemBuilder unenchant(Enchantment enchantment) {
        STACK.removeEnchantment(enchantment);
        return this;
    }

	public GUIItemBuilder flag(ItemFlag ...flag) {
        ItemMeta meta = STACK.getItemMeta();
        meta.addItemFlags(flag);
        STACK.setItemMeta(meta);
        return this;
    }

	public GUIItemBuilder deflag(ItemFlag ...flag) {
        ItemMeta meta = STACK.getItemMeta();
        meta.removeItemFlags(flag);
        STACK.setItemMeta(meta);
        return this;
    }

	public ItemStack build() {
        return get();
    }

	public ItemStack get() {
        return STACK;
    }

}
