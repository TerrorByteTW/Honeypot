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

import java.util.Set;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

public class Listeners {

    private final JavaPlugin plugin;
    private final BytePluginConfig config;
    private final HoneypotLogger logger;
    private final Set<IHoneypotEvent> events;

    /**
     * Create the listener configurator
     */
    @Inject
    Listeners(JavaPlugin plugin, BytePluginConfig config, HoneypotLogger logger, Set<IHoneypotEvent> events) {
        this.plugin = plugin;
        this.config = config;
        this.logger = logger;
        this.events = events;
    }

    /**
     * Set up all the listeners in the entire plugin
     */
    public void setupListeners() {
        PluginManager manager = plugin.getServer().getPluginManager();
        var cfg = config.config();

        boolean enableExtraEvents = cfg.getBoolean("enable-extra-events");
        boolean enableContainerActions = cfg.getBoolean("container-actions.enable-container-actions");
        boolean useInventoryClick = cfg.getBoolean("container-actions.use-inventory-click");

        events.forEach(event -> {
            if (!(event instanceof Listener listener)) {
                return;
            }

            if (event.isOptional() && !enableExtraEvents) {
                logger.debug(Component.text(
                    "Skipping registration of optional event: " + event.getClass().getSimpleName()), true);
                return;
            }

            if (event.isOptional()) {
                logger.debug(Component.text(
                    "Registering optional event: " + event.getClass().getSimpleName()), true);
            }

            if (listener instanceof InventoryClickDragEventListener) {
                if (enableContainerActions && useInventoryClick) {
                    logger.info(Component.text("Using inventory click for containers"));
                    manager.registerEvents(listener, plugin);
                }
                return;
            }

            if (listener instanceof PlayerInteractEventListener) {
                if (enableContainerActions && !useInventoryClick) {
                    logger.info(Component.text("Using player interact for containers"));
                    manager.registerEvents(listener, plugin);
                }
                return;
            }

            manager.registerEvents(listener, plugin);
        });
    }

}