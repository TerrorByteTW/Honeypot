package org.reprogle.honeypot.config;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.HoneypotConfigManager;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

public class TestConfigAccess {

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

	// Verify Configs are successfully created
	@Test
	public void testConfig() throws IOException {
		Assertions.assertDoesNotThrow(() -> HoneypotConfigManager.setupConfig(plugin));
		Assertions.assertNotNull(HoneypotConfigManager.getPluginConfig());
		Assertions.assertNotNull(HoneypotConfigManager.getGuiConfig());
	}

}
