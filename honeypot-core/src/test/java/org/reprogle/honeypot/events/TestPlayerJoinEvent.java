package org.reprogle.honeypot.events;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.TestUtils;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

public class TestPlayerJoinEvent {
	
	public static Honeypot plugin;
	public static ServerMock server;
	public final CountDownLatch latch = new CountDownLatch(2);

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
	public void testPlayerJoinEvent() throws InterruptedException {

		PlayerMock playerWithOp = TestUtils.addOP(server);
		PlayerMock playerWithPermissions = TestUtils.addPlayerWithPermissions(plugin, server, "honeypot.update");
		PlayerMock playerWithWildcard = TestUtils.addPlayerWithPermissions(plugin, server, "honeypot.*");
		PlayerMock player = TestUtils.addPlayerWithoutPermissions(server);

		/* 
		 * Since our player join event uses async methods, we need to wait a couple seconds for those to 
		 * complete before checking if the message is null. The tests runs faster than the async method and always
		 * returns null unless we wait first. Then, we check if the player received the message, which they should have
		 * since we always return that message during testing.
		 */
		TestUtils.fireJoinEvent(server, playerWithOp);
		latch.await(2, TimeUnit.SECONDS);
		Assertions.assertNotNull(playerWithOp.nextComponentMessage());

		TestUtils.fireJoinEvent(server, playerWithPermissions);
		latch.await(2, TimeUnit.SECONDS);
		Assertions.assertNotNull(playerWithPermissions.nextComponentMessage());

		TestUtils.fireJoinEvent(server, playerWithWildcard);
		latch.await(2, TimeUnit.SECONDS);
		Assertions.assertNotNull(playerWithWildcard.nextComponentMessage());

		TestUtils.fireJoinEvent(server, player);
		latch.await(2, TimeUnit.SECONDS);
		Assertions.assertNull(player.nextComponentMessage());
	}

}
