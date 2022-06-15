package org.reprogle.honeypot;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
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
import be.seeseemelk.mockbukkit.entity.PlayerMock;

public class TestUtils {

	private TestUtils() {
	}
	
	public static PlayerMock addOP(ServerMock server) {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        return player;
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

    public static void setPlayerPermissions(@NotNull Honeypot plugin, @NotNull PlayerMock player,
                                            @NotNull String... permissions) {
        PermissionAttachment attachment = player.addAttachment(plugin);
        for (String permission : permissions) {
            attachment.setPermission(permission, true);
        }
    }

}
