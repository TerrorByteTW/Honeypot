package org.reprogle.honeypot.commands;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.TestUtils;

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
	
    public void helpDialogIsCorrect() {
        String expectedMessage =
		"\n \n \n \n \n \n-----------------------\n \n" + ChatColor.AQUA + "[Honeypot]" + ChatColor.RESET + " " + ChatColor.WHITE + "Need Help?\n" +
		"     " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "create [ban | kick | warn | notify | nothing]\n" +
		"     " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "remove (all | near) (optional)\n" +
		"     " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "reload\n" +
		"     " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "locate\n" + 
		"     " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "gui\n \n" + 
		ChatColor.WHITE + "-----------------------";
        PlayerMock player = server.addPlayer();
        player.performCommand("honeypot help");
        TestUtils.assertMessage(player, expectedMessage);
    }
}
