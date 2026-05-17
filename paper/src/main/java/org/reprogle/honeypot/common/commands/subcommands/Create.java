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

package org.reprogle.honeypot.common.commands.subcommands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.reprogle.bytelib.ByteLibPlugin;
import org.reprogle.bytelib.commands.CommandFactory;
import org.reprogle.bytelib.commands.dsl.*;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.api.events.HoneypotCreateEvent;
import org.reprogle.honeypot.api.events.HoneypotPreCreateEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.utils.HoneypotCreatingPlayer;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;
import org.reprogle.honeypot.common.utils.integrations.GriefPreventionAdapter;
import org.reprogle.honeypot.common.utils.integrations.LandsAdapter;
import org.reprogle.honeypot.common.utils.integrations.WorldGuardAdapter;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Create implements CommandCallback {

    public static HashMap<UUID, HoneypotCreatingPlayer> playersCreatingRegions = new HashMap<>();

    private final ByteLibPlugin plugin;
    private final CommandFeedback commandFeedback;
    private final BytePluginConfig config;
    private final HoneypotRegionManager regionManager;
    private final AdapterManager adapterManager;

    @Inject
    Create(ByteLibPlugin plugin, CommandFeedback commandFeedback, BytePluginConfig config, HoneypotRegionManager regionManager, AdapterManager adapterManager) {
        this.plugin = plugin;
        this.commandFeedback = commandFeedback;
        this.config = config;
        this.regionManager = regionManager;
        this.adapterManager = adapterManager;
    }

    private boolean isAllowedPerFilters(Block block) {
        List<String> allowedBlocks = config.config().getStringList("allowed-blocks");
        List<String> allowedInventories = config.config().getStringList("allowed-inventories");
        boolean allowed = false;

        if (config.config().getBoolean("filters.blocks")) {
            for (String blockType : allowedBlocks) {
                assert block != null;
                if (block.getType().name().equals(blockType)) {
                    allowed = true;
                    break;
                }
            }
        }

        if (config.config().getBoolean("filters.inventories")) {
            for (String blockType : allowedInventories) {
                if (block.getType().name().equals(blockType)) {
                    allowed = true;
                    break;
                }
            }
        }

        return allowed;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
        String kindArg;
        String typeArg;

        try {
            kindArg = StringArgumentType.getString(ctx, "kind");
            if (!(kindArg.equalsIgnoreCase("block") || kindArg.equalsIgnoreCase("region"))) {
                ctx.getSource().getSender().sendMessage(commandFeedback.sendCommandFeedback("not-valid-kind"));
                return Command.SINGLE_SUCCESS;
            }
        } catch (IllegalArgumentException ignored) {
            ctx.getSource().getSender().sendMessage(commandFeedback.sendCommandFeedback("usage"));
            return Command.SINGLE_SUCCESS;
        }

        try {
            typeArg = StringArgumentType.getString(ctx, "type");
            if (!config.require("honeypots").contains(typeArg) && Registry.getBehaviorRegistry().getBehaviorProvider(typeArg) == null) {
                ctx.getSource().getSender().sendMessage(commandFeedback.sendCommandFeedback("no-exist"));
                return Command.SINGLE_SUCCESS;
            }
        } catch (IllegalArgumentException ignored) {
            ctx.getSource().getSender().sendMessage(commandFeedback.sendCommandFeedback("usage"));
            return Command.SINGLE_SUCCESS;
        }

        int returnCode = 1;

        Player p = (Player) ctx.getSource().getSender(); // This is safe because this command has a requirement that only allows players to execute it

        if (kindArg.equalsIgnoreCase("block"))
            returnCode = createBlock(p, typeArg);
        else
            returnCode = createRegion(p, typeArg);

        return returnCode;
    }

    private int createBlock(Player p, String typeArg) {
        Block block;
        WorldGuardAdapter wga = adapterManager.getWorldGuardAdapter();
        GriefPreventionAdapter gpa = adapterManager.getGriefPreventionAdapter();
        LandsAdapter la = adapterManager.getLandsAdapter();

        // Get the block the player is looking at
        if (p.getTargetBlockExact(5) != null) {
            block = p.getTargetBlockExact(5);
        } else {
            p.sendMessage(commandFeedback.sendCommandFeedback("not-looking-at-block"));
            return Command.SINGLE_SUCCESS;
        }

        if (block == null) return Command.SINGLE_SUCCESS;

        // Check if in a WorldGuard region and the flag is set to deny. If it is, don't
        // bother continuing
        if (wga != null && !wga.isAllowed(p, block.getLocation())) {
            p.sendMessage(commandFeedback.sendCommandFeedback("worldguard"));
            return Command.SINGLE_SUCCESS;
        }

        // Check if in a GriefPrevention region
        if (gpa != null && !gpa.isAllowed(p, block.getLocation())) {
            p.sendMessage(commandFeedback.sendCommandFeedback("griefprevention"));
            return Command.SINGLE_SUCCESS;
        }

        // Check if in a Lands region
        if (la != null && !la.isAllowed(block.getLocation())) {
            p.sendMessage(commandFeedback.sendCommandFeedback("lands"));
            return Command.SINGLE_SUCCESS;
        }

        // Check if the filter is enabled, and if so, if it's allowed
        if ((config.config().getBoolean("filters.blocks")
            || config.config().getBoolean("filters.inventories"))
            && (!isAllowedPerFilters(block))) {
            p.sendMessage(commandFeedback.sendCommandFeedback("against-filter"));
            return Command.SINGLE_SUCCESS;

        }

        // If the block already exists in the DB
        if (regionManager.isHoneypotBlock(block)) {
            p.sendMessage(commandFeedback.sendCommandFeedback("already-exists"));

            // If the block doesn't exist
        } else {

            // Fire HoneypotPreCreateEvent and cancel the command execution if any other plugin cancels the event itself
            HoneypotPreCreateEvent hpce = new HoneypotPreCreateEvent(p, block);
            Bukkit.getPluginManager().callEvent(hpce);

            if (hpce.isCancelled())
                return Command.SINGLE_SUCCESS;


            regionManager.createBlock(block, typeArg);
            p.sendMessage(commandFeedback.sendCommandFeedback("success.created"));

            // Fire HoneypotCreateEvent
            HoneypotCreateEvent hce = new HoneypotCreateEvent(p, block);
            Bukkit.getPluginManager().callEvent(hce);
        }

        return Command.SINGLE_SUCCESS;
    }

    private int createRegion(Player p, String typeArg) {
        if (Create.playersCreatingRegions.containsKey(p.getUniqueId())) {
            p.sendMessage(commandFeedback.sendCommandFeedback("creating-region.already-creating"));
            return Command.SINGLE_SUCCESS;
        }

        Create.playersCreatingRegions.put(p.getUniqueId(), new HoneypotCreatingPlayer(p, typeArg));

        Material mat = safeGetMaterial(config.config().getString("region-wand"));
        ItemStack item = new ItemStack(mat, 1);
        item.editMeta(meta -> meta.displayName(Component.text("Honeypot Wand")));
        item.editPersistentDataContainer(pdc -> pdc.set(new NamespacedKey(plugin, "region_wand"), PersistentDataType.BOOLEAN, true));
        item.editMeta(meta -> meta.lore(List.of(Component.text("Right click two blocks to create a region, drop the wand to cancel creation"))));
        p.give(item);

        // Schedule region creation to automatically end after 10 minutes if the player hasn't created a region or canceled it themselves.
        Bukkit.getAsyncScheduler().runDelayed(plugin, _ -> {
            if (!p.isOnline()) return;
            if (!Create.playersCreatingRegions.containsKey(p.getUniqueId())) return;

            Create.playersCreatingRegions.remove(p.getUniqueId());
            p.getInventory().remove(item);
            p.sendMessage(commandFeedback.sendCommandFeedback("creating-region.cancel"));
        }, 10L, TimeUnit.MINUTES);

        p.sendMessage(commandFeedback.sendCommandFeedback("creating-region.start"));

        return Command.SINGLE_SUCCESS;
    }

    private Material safeGetMaterial(String materialName) {
        Material material = Material.getMaterial(materialName);
        return material != null && material.asItemType() != null ? material : Material.HONEY_BLOCK;
    }

    public static LiteralNode commandTree(BytePluginConfig config, CommandFactory factory) {
        return CommandDsl.literal("create")
            .requires(
                PermissionChecks.allOf( // Requires that at least one of the permissions listed is owned, and that the sender is a player
                    PermissionChecks.anyOf(
                        PermissionChecks.permission("honeypot.create"),
                        PermissionChecks.permission("honeypot.*"),
                        PermissionChecks.isOp()
                    ),
                    PermissionChecks.playerOnly()
                )
            )
            .then(
                CommandDsl.argument("kind", StringArgumentType.string())
                    .suggests(
                        Suggest.dynamic((ctx, remaining) -> {
                            Set<String> options = new HashSet<>(Set.of("block"));

                            Player player = (Player) ctx.getSource().getSender();
                            if (player.hasPermission("honeypot.create.region")) options.add("region");

                            return options.stream().filter(
                                    opt -> opt.startsWith(remaining) || opt.equalsIgnoreCase(remaining))
                                .map(Suggest::suggestion)
                                .toList();
                        })
                    )
                    .then(
                        CommandDsl.argument("type", StringArgumentType.string())
                            .suggests(
                                Suggest.dynamic(
                                    (ctx, remaining) -> {
                                        // Get all registered behavior providers
                                        Collection<BrigadierSuggestion> suggestions = new ArrayList<>();
                                        ConcurrentMap<String, BehaviorProvider> map = Registry.getBehaviorRegistry().getBehaviorProviders();
                                        map.forEach((providerName, provider) -> suggestions.add(Suggest.suggestion(providerName)));

                                        // Get all custom honeypots in honeypots.yml
                                        Set<Object> keys = config.require("honeypots").getKeys();
                                        keys.forEach(key -> suggestions.add(Suggest.suggestion(key.toString())));

                                        return suggestions.stream()
                                            .filter(s -> s.value().startsWith(remaining) || s.value().equalsIgnoreCase(remaining))
                                            .collect(Collectors.toList());
                                    }
                                )
                            )
                            .executes(Create.class, factory)
                    )
            )
            .executes(Help.class, factory);
    }
}
