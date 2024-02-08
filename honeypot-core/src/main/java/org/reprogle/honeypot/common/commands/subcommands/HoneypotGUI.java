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

package org.reprogle.honeypot.common.commands.subcommands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotPreCreateEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.gui.GUIMenu;
import org.reprogle.honeypot.common.gui.button.GUIButton;
import org.reprogle.honeypot.common.gui.item.GUIItemBuilder;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockObject;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;
import org.reprogle.honeypot.common.utils.integrations.GriefPreventionAdapter;
import org.reprogle.honeypot.common.utils.integrations.LandsAdapter;
import org.reprogle.honeypot.common.utils.integrations.WorldGuardAdapter;

import static org.reprogle.honeypot.common.utils.folia.Scheduler.FOLIA;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class HoneypotGUI implements HoneypotSubCommand {

	@Override
	public String getName() {
		return "gui";
	}

	@Override
	@SuppressWarnings("java:S1192")
	public void perform(Player p, String[] args) {
		p.openInventory(mainMenu(p, args).getInventory());
	}

	@SuppressWarnings({ "java:S1192", "java:S1121" })
	private static void customHoneypotsInventory(Player p) {
		GUIMenu customHoneypotsGUI = Honeypot.getGUI().create("Custom Honeypot", 3);
		List<String> types = new ArrayList<>();

		Set<Object> keys = HoneypotConfigManager.getHoneypotsConfig().getKeys();
		for (Object key : keys) {
			types.add(key.toString());
		}

		ConcurrentMap<String, BehaviorProvider> map = Honeypot.getRegistry().getBehaviorProviders();
		map.forEach((providerName, provider) -> types.add(providerName));

		for (String type : types) {

			GUIItemBuilder item;

			if (Honeypot.getRegistry().getBehaviorProvider(type) == null) {
				String action = HoneypotConfigManager.getHoneypotsConfig().getString(type + ".type");

				switch (action) {
				case "command" -> item = new GUIItemBuilder(Material.COMMAND_BLOCK);
				case "permission" -> item = new GUIItemBuilder(Material.TRIPWIRE_HOOK);
				case "broadcast" -> item = new GUIItemBuilder(Material.BOOK);
				default -> item = new GUIItemBuilder(Material.PAPER);
				}
			}
			else {
				item = new GUIItemBuilder(Honeypot.getRegistry().getBehaviorProvider(type).getIcon());
			}

			item.name(type);
			item.lore("Click to create a Honeypot of this type");
			GUIButton button = new GUIButton(item.build())
					.withListener((InventoryClickEvent event) -> createHoneypotFromGUI(event, type));

			customHoneypotsGUI.addButton(button);
		}

		p.openInventory(customHoneypotsGUI.getInventory());
	}

	@SuppressWarnings("java:S1192")
	private static void allHoneypotsInventory(Player p) {
		if (!(p.hasPermission("honeypot.locate"))) {
			p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
			return;
		}

		GUIMenu allBlocksGUI = Honeypot.getGUI().create("Honeypots {currentPage}/{maxPage}", 3);

		for (HoneypotBlockObject honeypotBlock : HoneypotBlockManager.getInstance().getAllHoneypots()) {
			GUIItemBuilder item;

			if (Boolean.TRUE.equals(HoneypotConfigManager.getGuiConfig().getBoolean("display-button-as-honeypot"))) {
				item = new GUIItemBuilder(honeypotBlock.getBlock().getType());
				item.lore("Click to teleport to Honeypot");
				item.name("Honeypot: " + honeypotBlock.getCoordinates());
			}
			else {
				item = new GUIItemBuilder(
						Material.getMaterial(HoneypotConfigManager.getGuiConfig().getString("default-gui-button")));
				item.lore("Click to teleport to Honeypot");
				item.name("Honeypot: " + honeypotBlock.getCoordinates());
			}

			GUIButton button = new GUIButton(item.build()).withListener((InventoryClickEvent event) -> {
				event.getWhoClicked().sendMessage(ChatColor.ITALIC + ChatColor.GRAY.toString() + "Whoosh!");

				// In the future, we're going to make this nice and pretty. Until then, ew.
				if (FOLIA) {
					event.getWhoClicked().teleportAsync(honeypotBlock.getLocation().add(0.5, 1, 0.5));
				}
				else {
					event.getWhoClicked().teleport(honeypotBlock.getLocation().add(0.5, 1, 0.5));
				}
				event.getWhoClicked().closeInventory();
			});

			allBlocksGUI.addButton(button);

		}

		p.openInventory(allBlocksGUI.getInventory());
	}

	private static void historyQueryInventory(Player p) {
		if (!(p.hasPermission("honeypot.history"))) {
			p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
			return;
		}

		GUIMenu historyQueryGUI = Honeypot.getGUI().create("Query Player History", 3);

		for (Player player : Bukkit.getOnlinePlayers()) {
			GUIItemBuilder item;

			ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
			assert skullMeta != null;
			skullMeta.setOwningPlayer(player);
			skullItem.setItemMeta(skullMeta);

			item = new GUIItemBuilder(skullItem);
			item.name(player.getName());

			GUIButton button = new GUIButton(item.build()).withListener((InventoryClickEvent event) -> {
				event.getWhoClicked().closeInventory();
				Bukkit.dispatchCommand(event.getWhoClicked(), "honeypot history query " + item.getName());
			});

			historyQueryGUI.addButton(button);

		}

		p.openInventory(historyQueryGUI.getInventory());
	}

	@SuppressWarnings({ "java:S3776", "java:S1192" })
	private static void removeHoneypotInventory(Player p) {
		if (!(p.hasPermission("honeypot.remove"))) {
			p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
			return;
		}

		GUIMenu removeGUI = Honeypot.getGUI().create("Remove Honeypots", 1);

		GUIItemBuilder removeAllItem = new GUIItemBuilder(Material
				.getMaterial(HoneypotConfigManager.getGuiConfig().getString("remove-buttons.remove-all-button")));
		removeAllItem.name("Remove all Honeypots");

		GUIItemBuilder removeNearItem = new GUIItemBuilder(Material
				.getMaterial(HoneypotConfigManager.getGuiConfig().getString("remove-buttons.remove-near-button")));
		removeNearItem.name("Remove nearby Honeypots");

		GUIItemBuilder removeTargetItem = new GUIItemBuilder(Material
				.getMaterial(HoneypotConfigManager.getGuiConfig().getString("remove-buttons.remove-target-button")));
		removeTargetItem.name("Remove the Honeypot you're targeting");

		GUIButton removeAllButton = new GUIButton(removeAllItem.build()).withListener((InventoryClickEvent event) -> {
			event.getWhoClicked().closeInventory();
			HoneypotBlockManager.getInstance().deleteAllHoneypotBlocks();
			p.sendMessage(CommandFeedback.sendCommandFeedback("deletedall"));
		});

		GUIButton removeNearButton = new GUIButton(removeNearItem.build()).withListener((InventoryClickEvent event) -> {
			event.getWhoClicked().closeInventory();
			final double radius = HoneypotConfigManager.getPluginConfig().getDouble("search-range");
			final double xCoord = p.getLocation().getX();
			final double yCoord = p.getLocation().getY();
			final double zCoord = p.getLocation().getZ();

			// For every x value within radius
			for (double x = xCoord - radius; x < xCoord + radius; x++) {
				// For every y value within radius
				for (double y = yCoord - radius; y < yCoord + radius; y++) {
					// For every z value within radius
					for (double z = zCoord - radius; z < zCoord + radius; z++) {

						// Check the block at coords x,y,z to see if it's a Honeypot
						final Block b = new Location(p.getWorld(), x, y, z).getBlock();

						// If it is a honeypot do this
						if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(b))) {
							HoneypotBlockManager.getInstance().deleteBlock(b);

						}
					}
				}
			}

			p.sendMessage(CommandFeedback.sendCommandFeedback("deletednear"));
		});

		GUIButton removeTargetButton = new GUIButton(removeTargetItem.build())
				.withListener((InventoryClickEvent event) -> {
					Block block;
					event.getWhoClicked().closeInventory();

					if (event.getWhoClicked().getTargetBlockExact(5) != null) {
						block = event.getWhoClicked().getTargetBlockExact(5);
					}
					else {
						event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("notlookingatblock"));
						return;
					}

					if (block == null) {
						event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("notlookingatblock"));
						return;
					}

					if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(block))) {
						HoneypotBlockManager.getInstance().deleteBlock(block);
						p.sendMessage(CommandFeedback.sendCommandFeedback("success", false));
					}
					else {
						event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("notapot"));
					}
				});

		removeGUI.setButton(3, removeTargetButton);
		removeGUI.setButton(4, removeNearButton);
		removeGUI.setButton(5, removeAllButton);

		p.openInventory(removeGUI.getInventory());

	}

	@SuppressWarnings({ "unchecked", "java:S3776", "java:S6541" })
	private static void createHoneypotFromGUI(InventoryClickEvent event, String action) {
		Block block;
		WorldGuardAdapter wga = AdapterManager.getWorldGuardAdapter();
		GriefPreventionAdapter gpa = AdapterManager.getGriefPreventionAdapter();
		LandsAdapter la = AdapterManager.getLandsAdapter();

		// Get block the player is looking at
		if (event.getWhoClicked().getTargetBlockExact(5) != null) {
			block = event.getWhoClicked().getTargetBlockExact(5);
		}
		else {
			event.getWhoClicked().closeInventory();
			event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("notlookingatblock"));
			return;
		}

		// Check if in a WorldGuard region and the flag is set to deny. If it is, don't
		// bother continuing
		if (wga != null && !wga.isAllowed((Player) event.getWhoClicked(), block.getLocation())) {
			event.getWhoClicked().closeInventory();
			event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("worldguard"));
			return;
		}

		// Check if in a GriefPrevention region.
		if (gpa != null && !gpa.isAllowed((Player) event.getWhoClicked(), block.getLocation())) {
			event.getWhoClicked().closeInventory();
			event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("griefprevention"));
			return;
		}

		// Check if in a Lands land
		if (la != null && !la.isAllowed((Player) event.getWhoClicked(), block.getLocation())) {
			event.getWhoClicked().closeInventory();
			event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("lands"));
			return;
		}

		if (HoneypotConfigManager.getPluginConfig().getBoolean("filters.blocks")
				|| HoneypotConfigManager.getPluginConfig().getBoolean("filters.inventories")) {
			List<String> allowedBlocks = (List<String>) HoneypotConfigManager.getPluginConfig()
					.getList("allowed-blocks");
			List<String> allowedInventories = (List<String>) HoneypotConfigManager.getPluginConfig()
					.getList("allowed-inventories");
			boolean allowed = false;

			if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("filters.blocks"))) {
				for (String blockType : allowedBlocks) {
					if (block.getType().name().equals(blockType)) {
						allowed = true;
						break;
					}
				}
			}

			if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("filters.inventories"))) {
				for (String blockType : allowedInventories) {
					if (block.getType().name().equals(blockType)) {
						allowed = true;
						break;
					}
				}
			}

			if (!allowed) {
				event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("againstfilter"));
				return;
			}
		}

		event.getWhoClicked().closeInventory();
		if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(block))) {
			event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("alreadyexists"));

			// If it does not have a honeypot tag or the honeypot tag does not equal 1,
			// create one
		}
		else {

			// Fire HoneypotPreCreateEvent
			HoneypotPreCreateEvent hpce = new HoneypotPreCreateEvent((Player) event.getWhoClicked(), block);
			Bukkit.getPluginManager().callEvent(hpce);

			if (hpce.isCancelled())
				return;

			HoneypotBlockManager.getInstance().createBlock(block, action);
			event.getWhoClicked().sendMessage(CommandFeedback.sendCommandFeedback("success", true));

			// Fire HoneypotCreateEvent
			HoneypotPreCreateEvent hce = new HoneypotPreCreateEvent((Player) event.getWhoClicked(), block);
			Bukkit.getPluginManager().callEvent(hce);
		}
	}

	@SuppressWarnings("java:S3776")
	public static GUIMenu mainMenu(Player p, String[] args) {
		GUIMenu mainMenu = Honeypot.getGUI().create("Honeypot Main Menu", 1);
		GUIItemBuilder createItem;
		GUIItemBuilder removeItem;
		GUIItemBuilder listItem;
		GUIItemBuilder locateItem;
		GUIItemBuilder historyItem;

		createItem = new GUIItemBuilder(
				Material.getMaterial(HoneypotConfigManager.getGuiConfig().getString("main-buttons.create-button")));
		createItem.name("Create a Honeypot");

		removeItem = new GUIItemBuilder(
				Material.getMaterial(HoneypotConfigManager.getGuiConfig().getString("main-buttons.remove-button")));
		removeItem.name("Remove a Honeypot");

		listItem = new GUIItemBuilder(
				Material.getMaterial(HoneypotConfigManager.getGuiConfig().getString("main-buttons.list-button")));
		listItem.name("List all Honeypots");

		locateItem = new GUIItemBuilder(
				Material.getMaterial(HoneypotConfigManager.getGuiConfig().getString("main-buttons.locate-button")));
		locateItem.name("Locate nearby Honeypots");

		historyItem = new GUIItemBuilder(
				Material.getMaterial(HoneypotConfigManager.getGuiConfig().getString("main-buttons.history-button")));
		historyItem.name("Query player history");

		GUIButton createButton = new GUIButton(createItem.build())
				.withListener((InventoryClickEvent event) -> customHoneypotsInventory(p));

		GUIButton removeButton = new GUIButton(removeItem.build())
				.withListener((InventoryClickEvent event) -> removeHoneypotInventory(p));

		GUIButton listButton = new GUIButton(listItem.build())
				.withListener((InventoryClickEvent event) -> allHoneypotsInventory(p));

		GUIButton locateButton = new GUIButton(locateItem.build()).withListener((InventoryClickEvent event) -> {
			event.getWhoClicked().closeInventory();
			if (!(p.hasPermission("honeypot.locate"))) {
				p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
				return;
			}

			final double radius = HoneypotConfigManager.getPluginConfig().getDouble("search-range");
			final double xCoord = p.getLocation().getX();
			final double yCoord = p.getLocation().getY();
			final double zCoord = p.getLocation().getZ();
			boolean potFound = false;

			// For every x value within radius
			for (double x = xCoord - radius; x < xCoord + radius; x++) {
				// For every y value within radius
				for (double y = yCoord - radius; y < yCoord + radius; y++) {
					// For every z value within radius
					for (double z = zCoord - radius; z < zCoord + radius; z++) {

						// Check the block at coords x,y,z to see if it's a Honeypot
						final Block b = new Location(p.getWorld(), x, y, z).getBlock();

						// If it is a honeypot do this
						if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(b))) {
							potFound = true;

							// Create a dumb, invisible, invulnerable, block-sized glowing slime and spawn
							// it inside the block
							Slime slime = (Slime) Objects.requireNonNull(Bukkit.getWorld(b.getWorld().getName()))
									.spawnEntity(b.getLocation().add(0.5, 0, 0.5), EntityType.SLIME);
							slime.setSize(2);
							slime.setAI(false);
							slime.setGlowing(true);
							slime.setInvulnerable(true);
							slime.setHealth(1000.0);
							slime.setInvisible(true);

							// After 5 seconds, remove the slime. Setting its health to 0 causes the death
							// animation, removing it just makes it go away. Poof!
							new BukkitRunnable() {

								@Override
								public void run() {
									slime.remove();
								}
							}.runTaskLater(Honeypot.plugin, 20L * 5); // 20 ticks in 1 second * 5 seconds equals 100
																		// ticks
						}
					}
				}
			}

			if (potFound) {
				p.sendMessage(CommandFeedback.sendCommandFeedback("foundpot"));
			}
			else {
				p.sendMessage(CommandFeedback.sendCommandFeedback("nopotfound"));
			}
		});

		GUIButton historyButton = new GUIButton(historyItem.build())
				.withListener((InventoryClickEvent event) -> historyQueryInventory(p));

		mainMenu.setButton(2, createButton);
		mainMenu.setButton(3, removeButton);
		mainMenu.setButton(4, listButton);
		mainMenu.setButton(5, locateButton);
		mainMenu.setButton(6, historyButton);

		return mainMenu;
	}

	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		return new ArrayList<>();
	}

	// We will let the individual GUIs handle their own permission checking, but the
	// overall permissions of the GUI will be handled here
	@Override
	public List<HoneypotPermission> getRequiredPermissions() {
		List<HoneypotPermission> permissions = new ArrayList<>();
		permissions.add(new HoneypotPermission("honeypot.gui"));
		return permissions;
	}

	public static void callAllHoneypotsInventory(Player p) {
		allHoneypotsInventory(p);
	}
}
