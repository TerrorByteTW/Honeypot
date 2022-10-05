package org.reprogle.honeypot.events;

import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.storagemanager.HoneypotBlockManager;

public class StructureGrowEventListener implements Listener {

	/**
	 * Create package constructor to hide implicit one
	 */
	StructureGrowEventListener() {

	}

	@EventHandler(priority = EventPriority.LOW)
	public static void onStructureGrowEvent(StructureGrowEvent event) {
		for (int i = 0; i < event.getBlocks().size(); i++) {
			BlockState block = event.getBlocks().get(i);

			if (HoneypotBlockManager.getInstance().isHoneypotBlock(block.getBlock())) {
				Honeypot.getHoneypotLogger().log("StuctureGrowEvent being cancelled for Honeypot located at "
						+ block.getX() + ", " + block.getY() + ", " + block.getZ());
				event.setCancelled(true);
			}
		}
	}

}
