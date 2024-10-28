/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright TerrorByte & Honeypot Contributors (c) 2022 - 2024
 *
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.events;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotPlayerInteractEvent;
import org.reprogle.honeypot.api.events.HoneypotPrePlayerInteractEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.storagemanager.pdc.DataStoreManager;
import org.reprogle.honeypot.common.utils.ActionHandler;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;

import java.util.List;
import java.util.Objects;

public class PlayerInteractEventListener implements Listener {

    private final HoneypotConfigManager configManager;
    private final HoneypotBlockManager blockManager;
    private final HoneypotLogger logger;
    private final ActionHandler actionHandler;
    private final AdapterManager adapterManager;

    /**
     * Create a private constructor to hide the implicit one
     */
    @Inject
    PlayerInteractEventListener(HoneypotConfigManager configManager, HoneypotBlockManager blockManager,
                                HoneypotLogger logger, ActionHandler actionHandler,
                                AdapterManager adapterManager) {
        this.configManager = configManager;
        this.blockManager = blockManager;
        this.logger = logger;
        this.actionHandler = actionHandler;
        this.adapterManager = adapterManager;
    }

    // Player interact event
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings({"unchecked"})
    public void playerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getTargetBlockExact(5) == null)
            return;
        if (!(player.getTargetBlockExact(5).getState() instanceof Container))
            return;
        if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
            return;

        Block block = player.getTargetBlockExact(5);

        // We want to filter on inventories upon opening, not just creation (Like in the
        // HoneypotCreate class) because
        // inventories can be both broken AND open :)
        if (configManager.getPluginConfig().getBoolean("filters.inventories")) {
            List<String> allowedBlocks = (List<String>) configManager.getPluginConfig()
                    .getList("allowed-inventories");
            boolean allowed = false;

            for (String blockType : allowedBlocks) {
                if (Objects.requireNonNull(block).getType().name()
                        .equals(blockType)) {
                    allowed = true;
                    break;
                }
            }

            if (!allowed) {
                return;
            }
        }

        try {
            if (!Objects.requireNonNull(block).getType().equals(Material.ENDER_CHEST)
                    && blockManager.isHoneypotBlock(Objects.requireNonNull(block))) {

                // If any of the adapters state that this is a disallowed action, don't bother doing anything since it was already blocked
                if (!adapterManager.checkAllAdapters(player, Objects.requireNonNull(player.getTargetBlockExact(5)).getLocation())) {
                    return;
                }

                // Fire HoneypotPrePlayerInteractEvent
                HoneypotPrePlayerInteractEvent hppie = new HoneypotPrePlayerInteractEvent(player,
                        event.getClickedBlock());
                Bukkit.getPluginManager().callEvent(hppie);

                if (hppie.isCancelled())
                    return;

                if (!(player.hasPermission("honeypot.exempt")
                        || player.hasPermission("honeypot.*") || player.isOp())) {
                    if (!configManager.getPluginConfig().getBoolean("always-allow-container-access"))
                        event.setCancelled(true);
                    executeAction(event);
                }

                HoneypotPlayerInteractEvent hpie = new HoneypotPlayerInteractEvent(player,
                        event.getClickedBlock());
                Bukkit.getPluginManager().callEvent(hpie);
            }
        } catch (NullPointerException npe) {
            // Do nothing as it's most likely an entity. If this event is triggered, the
            // player will either be targeting
            // a block or entity, and there is no other option for it to be null.
        }
    }

    private void executeAction(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = player.getTargetBlockExact(5);

        assert block != null;
        String action = blockManager.getAction(block);

        if (action == null) {
            logger.debug(Component.text("A PlayerInteractEvent was called for player: " + player.getName() + ", UUID of " + player.getUniqueId() + ". However, the action was null, so this must be a FAKE HONEYPOT. Please investigate the block at " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
            return;
        }

        logger.debug(Component.text("PlayerInteractEvent being called for player: " + player.getName() + ", UUID of " + player.getUniqueId() + ". Action is: " + action));

        actionHandler.handleCustomAction(action, block, player);
    }
}
