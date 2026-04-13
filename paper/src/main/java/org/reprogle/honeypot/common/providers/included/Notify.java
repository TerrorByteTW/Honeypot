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

package org.reprogle.honeypot.common.providers.included;

import com.google.inject.Inject;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.providers.Behavior;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.providers.BehaviorType;

import net.kyori.adventure.text.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Behavior(type = BehaviorType.NOTIFY, name = "notify", icon = Material.BEACON, configurable = true)
public class Notify extends BehaviorProvider {

    @Inject
    CommandFeedback commandFeedback;

    @Inject
    JavaPlugin plugin;

    @Override
    public boolean process(Player p, Block block, @Nullable YamlDocument config) {

        Component chatPrefix = commandFeedback.getChatPrefix();

        List<String> permissions = new ArrayList<>(List.of("honeypot.notify", "honeypot.*"));
        if (config != null && config.getBoolean("use-additional-permissions", false)) {
            permissions.addAll(config.getStringList("additional-permissions"));
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isPermissible(player, permissions)) {
                player.sendMessage(chatPrefix.append(Component.text(" ", NamedTextColor.WHITE))
                    .append(Component.text(p.getName(), NamedTextColor.RED))
                    .append(Component.text(" was caught breaking a Honeypot block at x=" + block.getX()))
                    .append(Component.text(", y=" + block.getY()))
                    .append(Component.text(", z=" + block.getZ()))
                    .append(Component.text(" in world " + block.getWorld().getName()))
                );
            }
        }

        plugin.getServer().getConsoleSender().sendMessage(chatPrefix.append(Component.text(" ", NamedTextColor.WHITE))
            .append(Component.text(p.getName(), NamedTextColor.RED))
            .append(Component.text(" was caught breaking a Honeypot block"))
        );

        return true;
    }

    private boolean isPermissible(Player player, List<String> permissions) {
        return player.isOp() || permissions.stream().anyMatch(player::hasPermission);
    }
}
