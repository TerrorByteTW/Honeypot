package org.reprogle.honeypot.events;

import org.bukkit.Material;
import org.bukkit.event.block.BlockFromToEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.storagemanager.HoneypotBlockObject;

import be.seeseemelk.mockbukkit.Coordinate;
import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;

public class TestBlockFromToEvent {

	public static Honeypot plugin;
	public static ServerMock server;
	public HoneypotBlockObject hbo;

	@BeforeAll
	public static void setUp() {
		server = MockBukkit.mock();
		plugin = MockBukkit.load(Honeypot.class);
	}

	@AfterAll
	public static void tearDown() {
		MockBukkit.unmock();
	}


	// This test is always returning True, even if I delete the custom BlockFromToEventListener the plugin registered. I need to figure out how to simulate the
	// water flowing into the torch in order to verify this is working. Calling the BlockFromToEvent manually doesn't seem to do anything unfortunately
	@Test
	public void testBlockFromTo() {
		WorldMock world = server.addSimpleWorld("world");

		BlockMock torch = world.createBlock(new Coordinate(0, 65, 0));
		torch.setType(Material.TORCH);
		Honeypot.getBlockManager().createBlock(torch, "nothing");

		BlockMock water = world.createBlock(new Coordinate(0, 65, 1));
		water.setType(Material.WATER);

		server.getPluginManager().callEvent(new BlockFromToEvent(water, torch));
		server.getScheduler().performTicks(20L);

		Assertions.assertTrue(Honeypot.getBlockManager().isHoneypotBlock(torch));

	}
	
}
