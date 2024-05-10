package org.reprogle.honeypot.common.events;

import com.google.inject.Inject;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

public class BlockFormEventListener implements Listener {

	private final HoneypotLogger logger;
	private final HoneypotBlockManager blockManager;

	/**
	 * Create package listener to hide implicit one
	 */
	@Inject
	BlockFormEventListener(HoneypotLogger logger, HoneypotBlockManager blockManager) {
		this.logger = logger;
		this.blockManager = blockManager;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockFormEvent(BlockFormEvent event) {
		Block block = event.getBlock();

		if (blockManager.isHoneypotBlock(block)) {
			logger.debug("BlockFormEvent being called for Honeypot: " + block.getX() + ", "
					+ block.getY() + ", " + block.getZ());
			event.setCancelled(true);
		}
	}

}
