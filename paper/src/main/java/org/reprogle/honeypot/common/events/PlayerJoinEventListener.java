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

import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.utils.ChangelogGUI;
import org.reprogle.honeypot.common.utils.HoneypotUpdateChecker;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class PlayerJoinEventListener implements Listener, IHoneypotEvent {

    private final CommandFeedback commandFeedback;
    private final HoneypotUpdateChecker updateChecker;
    private final ChangelogGUI changelogGUI;

    @Inject
    PlayerJoinEventListener(CommandFeedback commandFeedback, HoneypotUpdateChecker updateChecker, ChangelogGUI changelogGUI) {
        this.commandFeedback = commandFeedback;
        this.updateChecker = updateChecker;
        this.changelogGUI = changelogGUI;
    }

    // Player join event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        if (p.hasPermission("honeypot.update") || p.hasPermission("honeypot.*") || p.isOp()) {
            String currentVersion = updateChecker.currentVersion();
            updateChecker.fetchManifest(manifest -> {
                if (!manifest.hasUpdate(currentVersion)) return;

                Component updateMessage = commandFeedback.sendCommandFeedback("update-available")
                    .clickEvent(ClickEvent.openUrl(manifest.downloadUrl()))
                    .hoverEvent(HoverEvent.showText(commandFeedback.sendCommandFeedback("updater.download-hover")));

                Component whatsNew = commandFeedback.sendCommandFeedback("updater.whats-new")
                    .hoverEvent(HoverEvent.showText(commandFeedback.sendCommandFeedback("updater.whats-new-hover",
                        Placeholder.unparsed("version", currentVersion))))
                    .clickEvent(ClickEvent.callback(audience -> {
                        if (audience instanceof Player player) {
                            changelogGUI.open(player, manifest, currentVersion);
                        }
                    }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build()));

                p.sendMessage(Component.text()
                    .append(updateMessage)
                    .append(Component.space())
                    .append(whatsNew));
            });
        }
    }

}
