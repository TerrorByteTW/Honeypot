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

package org.reprogle.honeypot.common.providers.included;

import com.google.inject.Inject;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.providers.Behavior;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.providers.BehaviorType;

import net.kyori.adventure.text.Component;

@Behavior(type = BehaviorType.NOTIFY, name = "notify", icon = Material.BEACON)
@SuppressWarnings("deprecation")
public class Notify extends BehaviorProvider {

    @Inject
    private CommandFeedback commandFeedback;

    @Inject
    private Honeypot plugin;

    @Override
    public boolean process(Player p, Block block) {

        Component chatPrefix = commandFeedback.getChatPrefix();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("honeypot.notify") || player.hasPermission("honeypot.*")
                    || player.isOp()) {
                player.sendMessage(chatPrefix.append(Component.text(" "))
                        .append(Component.text(p.getName(), NamedTextColor.RED))
                        .append(Component.text(" was caught breaking a Honeypot block at x=" + block.getX()))
                        .append(Component.text(", y=" + block.getY()))
                        .append(Component.text(", z=" + block.getZ()))
                        .append(Component.text(" in world " + block.getWorld().getName()))
                );
            }
        }

        plugin.getServer().getConsoleSender().sendMessage(chatPrefix.append(Component.text(" ")
                .append(Component.text(p.getName(), NamedTextColor.RED))
                .append(Component.text(" was caught breaking a Honeypot block")))
        );

        return true;
    }
}
