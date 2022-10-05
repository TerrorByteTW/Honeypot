package org.reprogle.honeypot.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.storagemanager.HoneypotBlockManager;

public class BlockBurnEventListener implements Listener {

	/**
	 * Create package listener to hide implicit one
	 */
	BlockBurnEventListener() {

	}

	@EventHandler(priority = EventPriority.LOW)
	public static void onBlockBurnEvent(BlockBurnEvent event) {
		Block block = event.getBlock();

		if (HoneypotBlockManager.getInstance().isHoneypotBlock(block)) {
			Honeypot.getHoneypotLogger().log("BlockBurnEvent being called for Honeypot: " + block.getX() + ", "
					+ block.getY() + ", " + block.getZ());
			event.setCancelled(true);

			Block[] adjacentBlocks = new Block[] {
					block.getRelative(BlockFace.UP),
					block.getRelative(BlockFace.DOWN),
					block.getRelative(BlockFace.NORTH),
					block.getRelative(BlockFace.SOUTH),
					block.getRelative(BlockFace.EAST),
					block.getRelative(BlockFace.WEST)
			};

			// Proactively put out any fires adjacent the burning block, to reduce future
			// processing
			for (Block adjacentBlock : adjacentBlocks) {
				if (adjacentBlock.getType() == Material.FIRE
						&& adjacentBlock.getRelative(BlockFace.DOWN).getType() != Material.NETHERRACK) {
					adjacentBlock.setType(Material.AIR);
				}
			}

			Block aboveBlock = block.getRelative(BlockFace.UP);
			if (aboveBlock.getType() == Material.FIRE) {
				aboveBlock.setType(Material.AIR);
			}
		}
	}

}
