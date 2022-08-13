package org.reprogle.honeypot.commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.TestUtils;
import org.reprogle.honeypot.commands.subcommands.HoneypotHelp;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

public class TestHelpCommand {
	
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
    public void helpDialogIsCorrect() {
		String chatPrefix = CommandFeedback.getChatPrefix();
        String expectedMessage = "\n \n \n \n \n \n-----------------------\n \n" + chatPrefix + " " + ChatColor.WHITE + "Need Help?\n" +
			"  " + "/honeypot " + ChatColor.GRAY + "create [ban | kick | warn | notify | nothing | custom]\n" +
			"  " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "remove (all | near) (optional)\n" +
			"  " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "reload\n" +
			"  " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "locate\n" + 
			"  " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "gui\n" +
			"  " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "history [query | delete | purge] \n \n" + 
			ChatColor.WHITE + "-----------------------";

		// Check to make sure players without permissions don't have access
		PlayerMock player = TestUtils.addPlayerWithoutPermissions(server);
		player.performCommand("honeypot help");
		Assertions.assertNotEquals(expectedMessage, player.nextMessage());

		// Check players with permissions have access
        PlayerMock playerWithPermissions = TestUtils.addPlayerWithPermissions(plugin, server, "honeypot.commands");
        playerWithPermissions.performCommand("honeypot help");
		TestUtils.assertMessage(playerWithPermissions, expectedMessage);

		// Check that Operators have access
		PlayerMock playerWithOp = TestUtils.addOP(server);
		playerWithOp.performCommand("honeypot help");
		TestUtils.assertMessage(playerWithOp, expectedMessage);

	}

	// Should return an empty ArrayList
	@Test
	public void testGetSubcommands() {
		PlayerMock playerWithOp = TestUtils.addOP(server);
		Assertions.assertEquals(new ArrayList<>(), new HoneypotHelp().getSubcommands(playerWithOp, new String[]{"test"}));
	}

	@Test
	public void testGetName() {
		Assertions.assertEquals("help", new HoneypotHelp().getName());
	}
}
