package org.reprogle.honeypot.gui;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reprogle.honeypot.Honeypot;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

public class TestGuiCreation {

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
	public void testGuiCreate() {
		Assertions.assertNotNull(Honeypot.getGUI());
	}

	@Test
	public void testGuiMenuCreate() {
		GUIMenu mainMenu = Honeypot.getGUI().create("Honeypot Main Menu", 1);
		Assertions.assertNotNull(mainMenu);
		
	}

}
