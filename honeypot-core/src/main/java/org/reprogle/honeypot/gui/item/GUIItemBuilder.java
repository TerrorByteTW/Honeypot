/*
 * Honeypot is a tool for griefing auto-moderation
 * Copyright TerrorByte (c) 2022-2023
 * Copyright Honeypot Contributors (c) 2022-2023
 *
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.gui.item;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
		lore.replaceAll(textToTranslate -> ChatColor.translateAlternateColorCodes('&', textToTranslate));

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
