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

import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.utils.HoneypotLogger;
import org.reprogle.honeypot.common.utils.HoneypotUpdateChecker;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class PlayerJoinEventListener implements Listener {

    private final CommandFeedback commandFeedback;
    private final HoneypotLogger logger;
    private final Honeypot plugin;

    /**
     * Create a private constructor to hide the implicit one
     */
    @Inject
    PlayerJoinEventListener(Honeypot plugin, CommandFeedback commandFeedback, HoneypotLogger logger) {
        this.plugin = plugin;
        this.commandFeedback = commandFeedback;
        this.logger = logger;
    }

    // Player join event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        if (p.hasPermission("honeypot.update") || p.hasPermission("honeypot.*") || p.isOp()) {
            new HoneypotUpdateChecker(plugin,
                    "https://raw.githubusercontent.com/TerrorByteTW/Honeypot/master/version.txt").getVersion(latest -> {
                if (Integer.parseInt(latest.replace(".", "")) > Integer
                        .parseInt(plugin.getPluginMeta().getVersion().replace(".", ""))) {
                    Component message = commandFeedback.sendCommandFeedback("update-available")
                            .clickEvent(ClickEvent.openUrl("https://github.com/TerrorByteTW/Honeypot"))
                            .hoverEvent(HoverEvent.showText(Component.text("Click me to download the latest update!")));
                    
                    p.sendMessage(message);
                }
            }, logger);
        }
    }

}
