package org.reprogle.honeypot;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;

public class TestConfigAccess {

	@BeforeAll
	public static void setUp() {
		MockBukkit.mock();
		MockBukkit.load(Honeypot.class);
	}

	@AfterAll
	public static void tearDown() {
		MockBukkit.unmock();
	}

	// Verify Configs are successfully created
	@Test
	public void testConfig() throws IOException {
		Assertions.assertNotNull(HoneypotConfigManager.getPluginConfig());
		Assertions.assertNotNull(HoneypotConfigManager.getGuiConfig());
	}

}
