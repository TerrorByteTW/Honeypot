package org.reprogle.honeypot.common.events;

import com.google.inject.Inject;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;

public class BlockPlaceEventListener implements Listener, IHoneypotEvent {
    private final HoneypotRegionManager regionManager;

    @Inject
    public BlockPlaceEventListener(HoneypotRegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void blockPlaceEvent(BlockPlaceEvent event) {
        // Check to see if the event is canceled before doing any logic.
        if (event.isCancelled())
            return;

        // Early return if the block isn't a Honeypot
        if (!regionManager.isHoneypotBlock(event.getBlock())) return;

        event.setCancelled(true);
    }

    // Believe it or not, placing water or lava does not trigger a BlockPlaceEvent
    @EventHandler(priority = EventPriority.LOWEST)
    public void bucketUseEvent(PlayerInteractEvent event) {

        // Check to see if the event is canceled before doing any logic.
        if (event.useInteractedBlock() == Event.Result.DENY)
            return;

        // Early return if not a right-click or no item in hand
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null)
            return;

        // Early return for null clicked block
        if (event.getClickedBlock() == null)
            return;

        // We only care about buckets
        if (event.getItem().getType() != Material.BUCKET && event.getItem().getType() != Material.WATER_BUCKET && event.getItem().getType() != Material.LAVA_BUCKET)
            return;

        if (!regionManager.isHoneypotBlock(event.getClickedBlock()) ||
            regionManager.isHoneypotBlock(event.getClickedBlock().getRelative(event.getBlockFace())))
            return;

        event.getInteractionPoint();

        event.setCancelled(true);
    }
}
