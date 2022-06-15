package org.reprogle.honeypot;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.command.ConsoleCommandSenderMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

public class TestUtils {

	private TestUtils() {
	}
	
	public static PlayerMock addOP(ServerMock server) {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        return player;
    }

	public static void assertMessage(@NotNull ConsoleCommandSenderMock consoleCommandSender, @NotNull String expectedMessage) {
        assert consoleCommandSender.nextMessage().equals(expectedMessage);
    }

    public static void assertMessage(@NotNull PlayerMock player, @NotNull String expectedMessage) {
        assert player.nextMessage().equals(expectedMessage);
    }

    public static void assertMessages(@NotNull ConsoleCommandSenderMock consoleCommandSender, @NotNull String[] expectedMessages) {
        String message = consoleCommandSender.nextMessage();
        int i = 0;
        while (message != null && i < expectedMessages.length) {
            assert message.equals(expectedMessages[i++]);
            message = consoleCommandSender.nextMessage();
        }
        assert message == null && i == expectedMessages.length;
    }

    public static void assertMessages(@NotNull PlayerMock player, @NotNull String[] expectedMessages) {
        String message = player.nextMessage();
        int i = 0;
        while (message != null && i < expectedMessages.length) {
            assert message.equals(expectedMessages[i++]);
            message = player.nextMessage();
        }
        assert message == null && i == expectedMessages.length;
    }

	public static void setPlayerPermissions(@NotNull Honeypot plugin, @NotNull PlayerMock player,
                                            @NotNull String... permissions) {
        PermissionAttachment attachment = player.addAttachment(plugin);
        for (String permission : permissions) {
            attachment.setPermission(permission, true);
        }
    }

	public static void fireJoinEvent(@NotNull ServerMock server, @NotNull PlayerMock player) {
        server.getPluginManager().callEvent(new PlayerJoinEvent(player, "Someone joined"));
        server.getScheduler().performTicks(2L);
    }

	public static void fireEntityChangeEvent(@NotNull ServerMock server, @NotNull Entity entity, @NotNull Block from, @NotNull BlockData to) {
		server.getPluginManager().callEvent(new EntityChangeBlockEvent(entity, from, to));
		server.getScheduler().performTicks(2L);
	}

	public static void fireExplosionEvent(@NotNull ServerMock server, @NotNull Entity entity, @NotNull Location location, @NotNull List<Block> blocks, @NotNull float yield){
		server.getPluginManager().callEvent(new EntityExplodeEvent(entity, location, blocks, yield));
		server.getScheduler().performTicks(2L);
	}

	public static void firePistonExtendEvent(@NotNull ServerMock server, @NotNull Block block, @NotNull List<Block> blocks, @NotNull BlockFace direction) {
		server.getPluginManager().callEvent(new BlockPistonExtendEvent(block, blocks, direction));
		server.getScheduler().performTicks(2L);
	}

	public static void firePistonRetractEvent(@NotNull ServerMock server, @NotNull Block block, @NotNull List<Block> blocks, @NotNull BlockFace direction) {
		server.getPluginManager().callEvent(new BlockPistonRetractEvent(block, blocks, direction));
		server.getScheduler().performTicks(2L);
	}

	public static void fireBlockInteractEvent(@NotNull ServerMock server, @NotNull Player who, @NotNull Action action, @NotNull ItemStack item, @NotNull Block clickedBlock, @NotNull BlockFace clickedFace){
		server.getPluginManager().callEvent(new PlayerInteractEvent(who, action, item, clickedBlock, clickedFace));
		server.getScheduler().performTicks(2L);
	}

	public static void fireBlockBreakEvent(@NotNull ServerMock server, @NotNull Player who, @NotNull Block block) {
		server.getPluginManager().callEvent(new BlockBreakEvent(block, who));
		server.getScheduler().performTicks(2L);
	}

}
