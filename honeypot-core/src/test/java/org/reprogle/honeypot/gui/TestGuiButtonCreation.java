package org.reprogle.honeypot.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.gui.button.GUIButton;
import org.reprogle.honeypot.gui.item.GUIItemBuilder;
import org.reprogle.honeypot.storagemanager.HoneypotBlockObject;

import be.seeseemelk.mockbukkit.Coordinate;
import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;

public class TestGuiButtonCreation {

	public static Honeypot plugin;

	public static ServerMock server;

	@BeforeAll
	public static void setUp() {
		server = MockBukkit.mock();
		plugin = MockBukkit.load(Honeypot.class);
	}

	@AfterAll
	public static void tearDown() {
		MockBukkit.unmock();
	}

	/*
	 * This test will test for GUIItemBuilder and GUIButton
	 */
	@Test
	public void testButtons() {
		WorldMock worldMock = server.addSimpleWorld("world");
		BlockMock block = worldMock.createBlock(new Coordinate(0, 65, 9));	
		block.setType(Material.DIAMOND_ORE);
		HoneypotBlockObject hpBlock = new HoneypotBlockObject(block, "kick");

		GUIButton button = createDummyButton(hpBlock);

		// Ensure the button is actually created and the material is the material we passed in to it earlier
		Assertions.assertNotNull(button);
		Assertions.assertEquals(Material.DIAMOND_ORE, button.getIcon().getType());
	}

	// I am not sure how to test setting the listener since GUIButtonListener uses a package constructor
	@Test
	public void testGetListener() {
		GUIItemBuilder item;
		item = new GUIItemBuilder(Material.DIAMOND_ORE);

		GUIButton button = new GUIButton(item.build()).withListener((InventoryClickEvent event) -> {});

		Assertions.assertNotNull(button.getListener());
	}

	@Test
	public void getIcon() {
		GUIItemBuilder item;
		item = new GUIItemBuilder(Material.DIAMOND_ORE);

		GUIButton button = new GUIButton(item.build()).withListener((InventoryClickEvent event) -> {});

		Assertions.assertEquals(Material.DIAMOND_ORE, button.getIcon().getType());
	}

	@Test
	public void setIcon() {
		GUIItemBuilder item;
		item = new GUIItemBuilder(Material.DIAMOND_ORE);

		GUIButton button = new GUIButton(item.build()).withListener((InventoryClickEvent event) -> {});
		button.setIcon(new ItemStack(Material.DIAMOND_BLOCK));

		Assertions.assertEquals(Material.DIAMOND_BLOCK, button.getIcon().getType());
	}

	public GUIButton createDummyButton(HoneypotBlockObject block) {
		GUIItemBuilder item;
		item = new GUIItemBuilder(block.getBlock().getType());
		item.lore("Click to teleport to Honeypot");
		item.name("Honeypot: " + block.getCoordinates());

		GUIButton button = new GUIButton(item.build()).withListener((InventoryClickEvent event) -> {
			event.getWhoClicked().sendMessage(ChatColor.ITALIC.toString() + ChatColor.GRAY.toString() + "Whoosh!");
			event.getWhoClicked().teleport(block.getBlock().getLocation().add(0.5, 1, 0.5));
			event.getWhoClicked().closeInventory();
		});

		return button;
	}

	public GUIItemBuilder createDummyItem(Block block) {
		GUIItemBuilder item;
		item = new GUIItemBuilder(block.getType());
		item.lore("Click to teleport to Honeypot");
		item.name("Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ());

		return item;
	}

}
