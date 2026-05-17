package org.reprogle.honeypot.common.events;

import com.google.inject.Inject;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.reprogle.bytelib.ByteLibPlugin;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.subcommands.Create;
import org.reprogle.honeypot.common.commands.subcommands.GUI;
import org.reprogle.honeypot.common.storageproviders.HoneypotRegionObject;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;
import org.reprogle.honeypot.common.utils.RegionOutliner;

import java.util.Arrays;
import java.util.List;

public class HoneypotWandListeners implements Listener, IHoneypotEvent {

    private final ByteLibPlugin plugin;
    private final HoneypotLogger logger;
    private final BytePluginConfig config;
    private final HoneypotRegionManager regionManager;
    private final CommandFeedback commandFeedback;

    private final NamespacedKey key;

    @Inject
    public HoneypotWandListeners(ByteLibPlugin plugin, HoneypotLogger logger, BytePluginConfig config, HoneypotRegionManager regionManager, CommandFeedback commandFeedback) {
        this.plugin = plugin;
        this.logger = logger;
        this.config = config;
        this.regionManager = regionManager;
        this.commandFeedback = commandFeedback;
        this.key = new NamespacedKey(plugin, "region_wand");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the player is creating a region
        if (!Create.playersCreatingRegions.containsKey(event.getPlayer().getUniqueId()))
            return;

        logger.debug(Component.text("Player " + event.getPlayer() + " is creating a region & interacted with a block, running checks"), false);

        // Check that it was a right-click
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // Check that they right-clicked a block
        if (event.getClickedBlock() == null) return;

        if (event.getHand() == null) return;

        // Check that the item has a display name
        ItemStack itemInHand = event.getPlayer().getInventory().getItem(event.getHand());
        if (itemInHand.getItemMeta() == null) return;
        if (!itemInHand.getItemMeta().hasDisplayName()) return;

        // Check that the item is the correct material
        Material wand = GUI.safeGetMaterial(config.config().getString("region-wand"));
        if (!itemInHand.getType().equals(wand)) return;

        // Check the PDC, which proves that the wand is real and not a renamed item or something
        NamespacedKey key = new NamespacedKey(plugin, "region_wand");
        PersistentDataContainerView view = itemInHand.getPersistentDataContainer();
        if (!view.has(key)) return;

        event.setCancelled(true);

        logger.debug(Component.text("Player " + event.getPlayer() + " is creating a region, checks passed. Assigning positions"), false);

        var region = Create.playersCreatingRegions.get(event.getPlayer().getUniqueId());
        if (region.pos1 == null) {
            region.pos1 = event.getClickedBlock().getLocation();
            Create.playersCreatingRegions.put(event.getPlayer().getUniqueId(), region);
            event.getPlayer().sendMessage(commandFeedback.sendCommandFeedback("creating-region.progress"));
        } else {
            region.pos2 = event.getClickedBlock().getLocation();
            Create.playersCreatingRegions.put(event.getPlayer().getUniqueId(), region);

            if (regionManager.checkForOverlap(region.pos1, region.pos2)) {
                event.getPlayer().sendMessage(commandFeedback.sendCommandFeedback("creating-region.overlap"));
                region.pos1 = null;
                region.pos2 = null;

                Create.playersCreatingRegions.put(event.getPlayer().getUniqueId(), region);
                List<HoneypotRegionObject> honeypots = Registry.getRegionStore().getNearbyHoneypotRegions(event.getPlayer().getLocation(), config.config().getInt("search-range", 5));

                for (HoneypotRegionObject honeypot : honeypots) {
                    if (honeypot.isSingleBlockRegion()) {
                        RegionOutliner.spawnSlime(plugin, event.getPlayer().getWorld(), honeypot.getPos1().getX(), honeypot.getPos1().getY(), honeypot.getPos1().getZ());
                    } else {
                        RegionOutliner.outlineBoundingBox(plugin, event.getPlayer(), honeypot.getPos1(), honeypot.getPos2());
                    }
                }
                return;
            }

            regionManager.createRegion(region.pos1, region.pos2, region.getAction());

            event.getPlayer().sendMessage(commandFeedback.sendCommandFeedback("creating-region.end"));
            RegionOutliner.outlineBoundingBox(plugin, event.getPlayer(), region.pos1, region.pos2);

            Create.playersCreatingRegions.remove(event.getPlayer().getUniqueId());
            removeItemFromPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onItemFrameChange(PlayerItemFrameChangeEvent event) {
        if (!event.getAction().equals(PlayerItemFrameChangeEvent.ItemFrameChangeAction.PLACE)) return;
        if (!event.getItemStack().getPersistentDataContainer().has(key)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        logger.debug(Component.text("Player " + event.getPlayer() + " disconnected, removing region wand from inventory if it exists"), true);
        removeItemFromPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        if (!Create.playersCreatingRegions.containsKey(event.getPlayer().getUniqueId())) return;
        if (!item.getPersistentDataContainer().has(key)) return;

        Create.playersCreatingRegions.remove(event.getPlayer().getUniqueId());
        event.getItemDrop().remove();

        logger.debug(Component.text("Region wand dropped by " + event.getPlayer() + ", cancelling region creation"), true);
        event.getPlayer().sendMessage(commandFeedback.sendCommandFeedback("creating-region.cancel"));
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        // Ensure the inventory actually exists
        if (event.getClickedInventory() == null) return;

        // If it was the player's own inventory (the "E" key normally, not like their inventory when viewing a chest or something), return
        if (event.getView().getTopInventory().getType() == InventoryType.CRAFTING) return;

        // Check to make sure the click was a person
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check for if the player used their 1-9 keys to put the wand item into an inventory
        // We do this before checking if the item in the slot is null or not because, when using keyboard clicks, it absolutely *can* be null
        if (event.getClick().isKeyboardClick()) {
            int button = event.getHotbarButton();

            // -1 means offhand swap
            ItemStack item = button == -1 ? player.getInventory().getItemInOffHand() : player.getInventory().getItem(button);

            if (item == null) return;
            if (item.getPersistentDataContainer().has(key)) {
                event.setCancelled(true);
                return;
            }
        }

        // Ensure the item actually exists (not air)
        if (event.getCurrentItem() == null) return;

        // Ensure the item has the key
        if (!event.getCurrentItem().getPersistentDataContainer().has(key)) return;

        // Cancel the event
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        if (Create.playersCreatingRegions.containsKey(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(commandFeedback.sendCommandFeedback("creating-region.cancel"));
            removeItemFromPlayer(event.getPlayer());
        }
    }

    private void removeItemFromPlayer(Player player) {
        Create.playersCreatingRegions.remove(player.getUniqueId());
        Inventory playerInv = player.getInventory();
        Arrays.stream(playerInv.getContents())
            .filter(item -> item != null && item.getPersistentDataContainer().has(key))
            .forEach(playerInv::removeItem);
    }
}
