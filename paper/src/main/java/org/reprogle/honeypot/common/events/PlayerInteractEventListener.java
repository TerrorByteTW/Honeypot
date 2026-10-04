/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) TerrorByte and Honeypot Contributors 2022 - 2025.
 *
 * This program is free software: You can redistribute it and/or modify it under
 *  the terms of the Mozilla Public License 2.0 as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including,
 * without limitation, warranties that the Covered Software is free of defects, merchantable,
 * fit for a particular purpose or non-infringing. See the MPL 2.0 license for more details.
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
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.api.events.*;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.utils.ActionHandler;
import org.reprogle.honeypot.common.utils.HoneypotLogger;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;

import java.util.List;
import java.util.Objects;

public class PlayerInteractEventListener implements Listener, IHoneypotEvent {

    private final BytePluginConfig config;
    private final HoneypotRegionManager regionManager;
    private final HoneypotLogger logger;
    private final ActionHandler actionHandler;
    private final AdapterManager adapterManager;

    @Inject
    PlayerInteractEventListener(BytePluginConfig config, HoneypotRegionManager regionManager,
                                HoneypotLogger logger, ActionHandler actionHandler,
                                AdapterManager adapterManager) {
        this.config = config;
        this.regionManager = regionManager;
        this.logger = logger;
        this.actionHandler = actionHandler;
        this.adapterManager = adapterManager;
    }

    // Player interact event
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings({"unchecked"})
    public void playerInteractEvent(PlayerInteractEvent event) {
        // Container actions on open only apply if they're enabled and inventory clicks aren't being used instead
        if (!config.config().getBoolean("container-actions.enable-container-actions")
                || config.config().getBoolean("container-actions.use-inventory-click"))
            return;

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
        if (config.config().getBoolean("filters.inventories")) {
            List<String> allowedBlocks = (List<String>) config.config()
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
                    && regionManager.isHoneypotBlock(Objects.requireNonNull(block))) {

                // If any of the adapters state that this is a disallowed action, don't bother doing anything since it was already blocked
                if (!adapterManager.checkAllAdapters(player, Objects.requireNonNull(player.getTargetBlockExact(5)).getLocation())) {
                    return;
                }

                // Fire HoneypotPrePlayerInteractEvent
                var hppie = new HoneypotPrePlayerInteractEvent(player,
                        event.getClickedBlock());
                var hpte = new HoneypotPreTriggerEvent(player,
                        event.getClickedBlock(), TriggerType.INVENTORY_OPEN);

                // Both events are always fired, and cancelling either one stops processing
                boolean preInteract = hppie.callEvent();
                boolean preTrigger = hpte.callEvent();
                if (!preInteract || !preTrigger)
                    return;

                logger.debug(Component.text("PlayerInteractEvent being called for player: " + player.getName() + ", UUID of " + player.getUniqueId() + " on Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ()));

                if (actionHandler.checkAndHandle(player, block, TriggerType.INVENTORY_OPEN) != ActionHandler.TriggerResult.EXEMPT
                        && !config.config().getBoolean("always-allow-container-access"))
                    event.setCancelled(true);

                new HoneypotPlayerInteractEvent(player,
                        event.getClickedBlock()).callEvent();
                new HoneypotTriggerEvent(player, block,
                        TriggerType.INVENTORY_OPEN).callEvent();
            }
        } catch (NullPointerException npe) {
            // Do nothing as it's most likely an entity. If this event is triggered, the
            // player will either be targeting
            // a block or entity, and there is no other option for it to be null.
        }
    }

    // Detects players trying to set a Honeypot on fire, such as lighting it with flint and steel or a fire charge
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerIgniteEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // getItem() is the item in the hand this event fired for, so each hand is only checked once
        if (event.getItem() == null) return;
        Material item = event.getItem().getType();
        if (item != Material.FLINT_AND_STEEL && item != Material.FIRE_CHARGE) return;

        Block block = event.getClickedBlock();
        if (block == null || !regionManager.isHoneypotBlock(block)) return;

        Player player = event.getPlayer();

        // If any of the adapters state that this is a disallowed action, don't bother doing anything since it was already blocked
        if (!adapterManager.checkAllAdapters(player, block.getLocation())) return;

        logger.debug(Component.text("Player " + player.getName() + " tried to ignite Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ()));

        if (!new HoneypotPreTriggerEvent(player, block, TriggerType.GENERIC).callEvent()) {
            logger.debug(Component.text("HoneypotPreTriggerEvent was cancelled, allowing " + player.getName() + " to ignite Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
            return;
        }

        // Exempt players may light Honeypots, everyone else is stopped from doing so
        if (actionHandler.checkAndHandle(player, block, TriggerType.GENERIC) != ActionHandler.TriggerResult.EXEMPT)
            event.setCancelled(true);

        new HoneypotTriggerEvent(player, block, TriggerType.GENERIC).callEvent();
    }
}
