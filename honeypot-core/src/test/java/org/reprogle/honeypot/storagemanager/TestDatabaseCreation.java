package org.reprogle.honeypot.storagemanager;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.util.List;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.storagemanager.sqlite.Database;
import org.reprogle.honeypot.storagemanager.sqlite.SQLite;

import be.seeseemelk.mockbukkit.Coordinate;
import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;

public class TestDatabaseCreation {

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

	@Test
	public void testSQL() {
		Database db;
		db = new SQLite(plugin);
		Assertions.assertNotNull(db);
	}

	@Test
	public void testConnection() {
		Database db;
		db = new SQLite(plugin);
		Connection connection = db.getSQLConnection();

		Assertions.assertNotNull(connection);

	}

	@Test
	public void testDBPushPull() {
		// Create a block for use in the GUI
		WorldMock worldMock = server.addSimpleWorld("world");
		BlockMock block = worldMock.createBlock(new Coordinate(0, 65, 9));	
		block.setType(Material.DIAMOND_ORE);
		HoneypotBlockStorageManager.createBlock(block, "kick");

		List<HoneypotBlockObject> blocks = HoneypotBlockStorageManager.getAllHoneypots();

		Assertions.assertEquals("kick", blocks.get(0).getAction());
		Assertions.assertEquals("0, 65, 9", blocks.get(0).getCoordinates());
		Assertions.assertEquals("world", blocks.get(0).getWorld());

	}
	
}
