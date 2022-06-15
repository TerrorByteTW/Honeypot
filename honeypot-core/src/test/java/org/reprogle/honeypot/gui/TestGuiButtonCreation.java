package org.reprogle.honeypot.gui;

import org.bukkit.block.Block;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.gui.item.GUIItemBuilder;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

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

	/**
	 * Tests multiple GUI functions at once. Technically Unit tests should test the smallest portion of code,
	 * but if this test passes, then we know most of the code is functional. Any piece of this code failing
	 * would result in the unit test failing as well
	 */

	/*
	 *
	 * 	Commenting out for now due to a bug in MockButton not allowing me to create proper ItemMetaMock.
	 *
	@Test
	public void testGUIItemBuilderFromHoneypotInWorld() {
		// Create a block for use in the GUI
		WorldMock worldMock = server.addSimpleWorld("world");
		BlockMock block = worldMock.createBlock(new Coordinate(0, 65, 9));	
		HoneypotBlockStorageManager.createBlock(block, "kick");
		List<HoneypotBlockObject> honeypotBlocks = HoneypotBlockStorageManager.getAllHoneypots();

		GUIItemBuilder itemToTest = createDummyitem(honeypotBlocks.get(0).getBlock());

		GUIItemBuilder trueItem;
		trueItem = new GUIItemBuilder(Material.DIAMOND_ORE);
		trueItem.lore("Click to teleport to Honeypot");
		trueItem.name("Honeypot: 0, 65, 9");

		// Check if the item in the world equals the item we created
		Assertions.assertEquals(trueItem, itemToTest);

	}

	@Test
	public void testButtons() {
		WorldMock worldMock = server.addSimpleWorld("world");
		BlockMock block = worldMock.createBlock(new Coordinate(0, 65, 9));	
		HoneypotBlockObject hpBlock = new HoneypotBlockObject("world", "0, 65, 9", "kick");

		GUIItemBuilder itemToTest = createDummyitem(hpBlock.getBlock());

		GUIButton button = new GUIButton(itemToTest.build()).withListener((InventoryClickEvent event) -> {
			event.getWhoClicked().sendMessage(ChatColor.ITALIC.toString() + ChatColor.GRAY.toString() + "Whoosh!");
			event.getWhoClicked().teleport(hpBlock.getLocation().add(0.5, 1, 0.5));
			event.getWhoClicked().closeInventory();
		});

		Assertions.assertNotNull(button);
	}
	*/

	public GUIItemBuilder createDummyitem(Block block) {
		GUIItemBuilder item;
		item = new GUIItemBuilder(block.getType());
		item.lore("Click to teleport to Honeypot");
		item.name("Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ());

		return item;
	}

}
