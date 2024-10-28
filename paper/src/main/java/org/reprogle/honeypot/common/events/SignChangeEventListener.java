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
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotLogger;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;

public class SignChangeEventListener implements Listener {

    private final HoneypotBlockManager blockManager;
    private final HoneypotLogger logger;
    private final AdapterManager adapterManager;

    /**
     * Create package listener to hide implicit one
     */
    @Inject
    SignChangeEventListener(HoneypotBlockManager blockManager, HoneypotLogger logger, AdapterManager adapterManager) {
        this.blockManager = blockManager;
        this.logger = logger;
        this.adapterManager = adapterManager;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSignChangeEvent(SignChangeEvent event) {
        Block block = event.getBlock();

        if (blockManager.isHoneypotBlock(block)) {
            // If any of the adapters state that this is a disallowed action, don't bother doing anything since it was already blocked
            if (!adapterManager.checkAllAdapters(event.getPlayer(), event.getBlock().getLocation())) {
                return;
            }

            logger.debug(Component.text("SignChangeEvent being called for Honeypot: " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
            event.setCancelled(true);
        }
    }

}
