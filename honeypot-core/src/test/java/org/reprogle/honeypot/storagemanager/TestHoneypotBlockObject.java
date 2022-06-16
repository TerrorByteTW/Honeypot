package org.reprogle.honeypot.storagemanager;

import org.bukkit.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reprogle.honeypot.Honeypot;

import be.seeseemelk.mockbukkit.Coordinate;
import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;

public class TestHoneypotBlockObject {
	
	public static Honeypot plugin;
	public static ServerMock server;
	public HoneypotBlockObject hpo;

	@BeforeAll
	public static void setUp() {
		server = MockBukkit.mock();
		plugin = MockBukkit.load(Honeypot.class);
	}

	@AfterAll
	public static void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	public void testCreation() {
		if (hpo == null) {
			createHoneypotBlockObject(server);
		}

		Assertions.assertNotNull(hpo);
	}

	@Test
	public void testGetCoordinates() {
		if (hpo == null) {
			createHoneypotBlockObject(server);
		}

		Assertions.assertEquals("0, 5, 10", hpo.getCoordinates());
	}

	@Test
	public void testGetLocation() {
		if (hpo == null) {
			createHoneypotBlockObject(server);
		}

        Location location = new Location(server.getWorld("world"), 0, 5, 10);
		Assertions.assertEquals(location, hpo.getLocation());
	} 

	@Test
	public void testGetAction() {
		if (hpo == null) {
			createHoneypotBlockObject(server);
		}

		Assertions.assertEquals("ban", hpo.getAction());
	}

	@Test
	public void testGetWorld() {
		if (hpo == null) {
			createHoneypotBlockObject(server);
		}

		Assertions.assertEquals("world", hpo.getWorld());
	}

	@Test
	public void testgetBlock() {
		if (hpo == null) {
			createHoneypotBlockObject(server);
		}

        BlockMock block = (BlockMock) server.getWorld("world").getBlockAt(0, 5, 10);
		Assertions.assertEquals(block, hpo.getBlock());
	} 


	public HoneypotBlockObject createHoneypotBlockObject(ServerMock server) {
		WorldMock world = server.addSimpleWorld("world");
		BlockMock block = world.getBlockAt(new Coordinate(0, 5, 10));

		return hpo = new HoneypotBlockObject(block, "ban");
	}

	public BlockMock createRegularBlock(ServerMock server) {
		WorldMock world = server.addSimpleWorld("world");
		BlockMock block = world.getBlockAt(new Coordinate(0, 10, 20));

		return block;
	}

}
