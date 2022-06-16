package org.reprogle.honeypot.events;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.TestUtils;
import org.reprogle.honeypot.commands.CommandFeedback;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.md_5.bungee.api.chat.ClickEvent;	
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

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
		PlayerMock player = TestUtils.addPlayerWithoutPermissions(server);

		TextComponent message = new TextComponent(
            CommandFeedback.sendCommandFeedback("updateavailable"));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
            "https://github.com/TerrrorByte/Honeypot"));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
        	new Text("Click me to download the latest update!")));

		/* 
		 * Since our player join event uses async methods, we need to wait a couple seconds for those to 
		 * complete before checking if the message is null. The tests runs faster than the async method and always
		 * returns null unless we wait first.
		 */
		TestUtils.fireJoinEvent(server, playerWithOp);
		latch.await(2, TimeUnit.SECONDS);
		Assertions.assertNotNull(playerWithOp.nextComponentMessage());

		TestUtils.fireJoinEvent(server, playerWithPermissions);
		latch.await(2, TimeUnit.SECONDS);
		Assertions.assertNotNull(playerWithPermissions.nextComponentMessage());

		TestUtils.fireJoinEvent(server, player);
		latch.await(2, TimeUnit.SECONDS);
		Assertions.assertNull(player.nextComponentMessage());
	}

}
