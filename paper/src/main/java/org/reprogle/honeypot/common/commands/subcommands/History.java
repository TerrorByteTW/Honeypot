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

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.commands.CommandFactory;
import org.reprogle.bytelib.commands.dsl.*;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.store.HoneypotPlayerHistoryManager;
import org.reprogle.honeypot.common.storageproviders.HoneypotPlayerHistoryObject;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"java:S1192", "java:S3776"})
public class History implements CommandCallback {

    // Rows of history entries shown per page. The GUI adds a header row and a navigation row around them
    private static final int HISTORY_ROWS = 4;
    private static final int PAGE_SIZE = HISTORY_ROWS * 9;

    private final JavaPlugin plugin;
    private final CommandFeedback commandFeedback;
    private final BytePluginConfig config;
    private final HoneypotPlayerHistoryManager playerHistoryManager;
    private final HoneypotLogger logger;

    @Inject
    public History(JavaPlugin plugin, CommandFeedback commandFeedback, BytePluginConfig config, HoneypotPlayerHistoryManager playerHistoryManager, HoneypotLogger logger) {
        this.plugin = plugin;
        this.commandFeedback = commandFeedback;
        this.config = config;
        this.playerHistoryManager = playerHistoryManager;
        this.logger = logger;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
        HoneypotHistoryArgs args = parseArgumentsFromContext(ctx);
        if (!args.isValid()) {
            ctx.getSource().getSender().sendMessage(commandFeedback.sendCommandFeedback("usage"));
            return Command.SINGLE_SUCCESS;
        }

        // This is safe because args.isValid() will only be true if both of these variables are not null
        assert args.action != null;
        assert args.player != null;

        var sender = ctx.getSource().getSender();
        var id = args.player.getId();

        if (id == null) {
            sender.sendMessage(commandFeedback.sendCommandFeedback("player-not-found"));
            return Command.SINGLE_SUCCESS;
        }

        Player player = Bukkit.getPlayer(id);
        if (player == null || !player.isOnline()) {
            player = Bukkit.getOfflinePlayer(id).getPlayer();
            if (player == null) {
                sender.sendMessage(commandFeedback.sendCommandFeedback("player-not-found"));
                return Command.SINGLE_SUCCESS;
            }
        }

        switch (args.action) {
            case "delete":
                if (args.count >= 1) { // Since primitives are not nullable, the argument has a minimum of 1 but defaults to 0 if not provided. So, we know that args.count == 0 means not provided, and anything <= 0 is not possible thanks to Brigadier
                    playerHistoryManager.deletePlayerHistory(player, args.count);
                } else {
                    playerHistoryManager.deletePlayerHistory(player);
                }

                sender.sendMessage(commandFeedback.sendCommandFeedback("success"));

                break;
            case "query":
                if (!(sender instanceof Player viewer)) {
                    sender.sendMessage(Component.text("You must be a player to view history", NamedTextColor.RED));
                    break;
                }

                sender.sendMessage(commandFeedback.sendCommandFeedback("searching"));
                openHistoryGui(viewer, player);
                break;
            case "purge":
                playerHistoryManager.deleteAllHistory();
                sender.sendMessage(commandFeedback.sendCommandFeedback("success"));
                break;
            default:
                sender.sendMessage(commandFeedback.sendCommandFeedback("usage"));
                break;
        }

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Opens a paginated history GUI for the target player. Only the entry count, break count, and first page are
     * fetched up front; every other page is loaded from the store the first time it's navigated to.
     */
    private void openHistoryGui(Player viewer, Player target) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int total;
            List<HoneypotPlayerHistoryObject> firstPage;

            try {
                total = playerHistoryManager.getPlayerHistoryCount(target);
                firstPage = total > 0 ? playerHistoryManager.getPlayerHistory(target, 0, PAGE_SIZE) : List.of();
            } catch (Exception e) {
                logger.warning(Component.text("Failed to load history for player " + target.getName() + ": " + e.getMessage()));
                Bukkit.getScheduler().runTask(plugin, () -> viewer.sendMessage(commandFeedback.sendCommandFeedback("unknown-error")));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!viewer.isOnline()) return;

                if (total == 0) {
                    viewer.sendMessage(commandFeedback.sendCommandFeedback("no-history"));
                    return;
                }

                new HistoryView(viewer, target, total, firstPage).show();
            });
        });
    }

    private final class HistoryView {
        private final Player viewer;
        private final Player target;
        private final int lastPage;
        private final Map<Integer, List<HoneypotPlayerHistoryObject>> loadedPages = new HashMap<>();

        private final ChestGui gui;
        private final StaticPane entries = new StaticPane(9, HISTORY_ROWS);
        private final StaticPane navigation = new StaticPane(9, 1);

        private int page = 0;
        private boolean loading = false;

        private HistoryView(Player viewer, Player target, int total, List<HoneypotPlayerHistoryObject> firstPage) {
            this.viewer = viewer;
            this.target = target;
            this.lastPage = (total - 1) / PAGE_SIZE;
            this.loadedPages.put(0, firstPage);

            gui = new ChestGui(HISTORY_ROWS + 2, target.getName() + "'s History");
            gui.setOnGlobalClick(e -> e.setCancelled(true));

            gui.addPane(Slot.fromXY(0, 0), background());
            gui.addPane(Slot.fromXY(0, HISTORY_ROWS + 1), background());

            StaticPane header = new StaticPane(9, 1);
            header.addItem(new GuiItem(playerHead(total)), Slot.fromXY(4, 0));
            gui.addPane(Slot.fromXY(0, 0), header);

            gui.addPane(Slot.fromXY(0, 1), entries);
            gui.addPane(Slot.fromXY(0, HISTORY_ROWS + 1), navigation);
        }

        private void show() {
            render();
            gui.show(viewer);
        }

        private void goTo(int newPage) {
            if (loading || newPage < 0 || newPage > lastPage) return;

            if (loadedPages.containsKey(newPage)) {
                page = newPage;
                render();
                return;
            }

            // Lock navigation until the page arrives so spam-clicking doesn't queue up a pile of queries
            loading = true;
            renderNavigation();
            gui.update();

            int offset = newPage * PAGE_SIZE;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                List<HoneypotPlayerHistoryObject> result;
                try {
                    result = playerHistoryManager.getPlayerHistory(target, offset, PAGE_SIZE);
                } catch (Exception e) {
                    logger.warning(Component.text("Failed to load history page " + (newPage + 1) + " for player " + target.getName() + ": " + e.getMessage()));
                    result = null;
                }

                List<HoneypotPlayerHistoryObject> loaded = result;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    loading = false;
                    if (loaded != null) {
                        loadedPages.put(newPage, loaded);
                        page = newPage;
                    }

                    // Don't redraw an inventory the viewer has already closed
                    if (gui.getViewers().contains(viewer)) render();
                });
            });
        }

        private void render() {
            entries.clear();
            List<HoneypotPlayerHistoryObject> history = loadedPages.getOrDefault(page, List.of());
            for (int i = 0; i < history.size() && i < PAGE_SIZE; i++) {
                entries.addItem(historyItem(history.get(i)), Slot.fromXY(i % 9, i / 9));
            }

            renderNavigation();
            gui.update();
        }

        private void renderNavigation() {
            navigation.clear();

            if (page > 0) {
                navigation.addItem(new GuiItem(namedItem(guiMaterial("previous-page-item", Material.ARROW), Component.text("Previous Page", NamedTextColor.GOLD), List.of()),
                    _ -> goTo(page - 1)), Slot.fromXY(0, 0));
            }

            if (page < lastPage) {
                navigation.addItem(new GuiItem(namedItem(guiMaterial("next-page-item", Material.ARROW), Component.text("Next Page", NamedTextColor.GOLD), List.of()),
                    _ -> goTo(page + 1)), Slot.fromXY(8, 0));
            }

            Component pageName = loading
                ? Component.text("Loading...", NamedTextColor.GRAY)
                : Component.text("Page " + (page + 1) + " of " + (lastPage + 1), NamedTextColor.GOLD);
            navigation.addItem(new GuiItem(namedItem(guiMaterial("current-page-item", Material.NAME_TAG), pageName, List.of())), Slot.fromXY(4, 0));
        }

        private ItemStack playerHead(int total) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            if (head.getItemMeta() instanceof SkullMeta skullMeta) {
                skullMeta.setOwningPlayer(target);
                head.setItemMeta(skullMeta);
            }

            return named(head, Component.text(target.getName(), NamedTextColor.GOLD),
                List.of(Component.text(total + " history " + (total == 1 ? "entry" : "entries"), NamedTextColor.GRAY)));
        }

        private GuiItem historyItem(HoneypotPlayerHistoryObject entry) {
            // Entries recorded before the block was tracked have no material, so they get a placeholder
            Material material = entry.getBlock() == null ? null : GUI.safeGetMaterial(entry.getBlock(), null);
            Component name = material == null
                ? Component.text("Unknown Block", NamedTextColor.RED)
                : Component.translatable(material.translationKey(), NamedTextColor.GOLD);

            Location location = entry.getLocation();
            World world = location.getWorld();
            String coordinates = location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();

            List<Component> lore = new ArrayList<>(List.of(
                loreLine("Date", entry.getDateTime()),
                loreLine("Location", (world == null ? "Unknown world" : world.getName()) + " @ " + coordinates),
                loreLine("Action", entry.getAction()),
                loreLine("Break type", entry.getType())
            ));

            if (world != null) {
                lore.add(Component.empty());
                lore.add(Component.text("Click to teleport", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
            }

            ItemStack item = namedItem(material == null ? Material.BARRIER : material, name, lore);
            return new GuiItem(item, _ -> {
                if (world == null) return;
                viewer.closeInventory();
                viewer.teleportAsync(location.clone().add(0.5, 1, 0.5), PlayerTeleportEvent.TeleportCause.PLUGIN);
            });
        }

        private Component loreLine(String label, String value) {
            return Component.text(label + ": ", NamedTextColor.GRAY)
                .append(Component.text(value, NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false);
        }

        private Material guiMaterial(String key, Material fallback) {
            return GUI.safeGetMaterial(config.require("gui").getString(key), fallback);
        }

        private OutlinePane background() {
            OutlinePane background = new OutlinePane(9, 1);
            background.addItem(new GuiItem(namedItem(Material.YELLOW_STAINED_GLASS_PANE, Component.empty(), List.of())));
            background.setRepeat(true);
            background.setPriority(Pane.Priority.LOWEST);
            return background;
        }

        private ItemStack namedItem(Material material, Component name, List<Component> lore) {
            return named(new ItemStack(material), name, lore);
        }

        private ItemStack named(ItemStack item, Component name, List<Component> lore) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(name.decoration(TextDecoration.ITALIC, false));
                if (!lore.isEmpty()) meta.lore(lore);
                item.setItemMeta(meta);
            }
            return item;
        }
    }

    private static HoneypotHistoryArgs parseArgumentsFromContext(CommandContext<CommandSourceStack> ctx) {
        @Nullable String actionArg = null;
        @Nullable PlayerProfile playerArg = null;
        int countArg = 0;

        try {
            actionArg = StringArgumentType.getString(ctx, "action");

            final Collection<PlayerProfile> profiles = ctx.getArgument("player", PlayerProfileListResolver.class).resolve(ctx.getSource());
            if (profiles.isEmpty()) throw new IllegalArgumentException("Provided player is not a real player!");
            if (profiles.size() > 1)
                throw new IllegalArgumentException("Multiple players provided, please specify a single player!");
            playerArg = profiles.iterator().next();

            countArg = IntegerArgumentType.getInteger(ctx, "count");
        } catch (IllegalArgumentException | CommandSyntaxException ignored) {
            return new HoneypotHistoryArgs(actionArg, playerArg, countArg);
        }

        return new HoneypotHistoryArgs(actionArg, playerArg, countArg);
    }

    private record HoneypotHistoryArgs(@Nullable String action, @Nullable PlayerProfile player, int count) {
        public boolean isValid() {
            return action != null && player != null;
        } // primitives can't be null, but we don't care if `count` is null or not
    }

    public static LiteralNode commandTree(CommandFactory factory) {
        return CommandDsl.literal("history")
            .requires(
                PermissionChecks.anyOf(
                    PermissionChecks.permission("honeypot.history"),
                    PermissionChecks.permission("honeypot.*"),
                    PermissionChecks.isOp()
                )
            )
            .then(
                CommandDsl.argument("action", StringArgumentType.string())
                    .suggests(
                        Suggest.fixedWithTooltip(java.util.List.of(
                                Suggest.suggestion("delete", Component.text("Delete history record for a player")),
                                Suggest.suggestion("query", Component.text("Query history for a player")),
                                Suggest.suggestion("purge", Component.text("Purges all history for all players"))
                            )
                        )
                    )
                    .then(
                        CommandDsl.argument("player", ArgumentTypes.playerProfiles())
                            .suggests(
                                Suggest.dynamic(
                                    (ctx, remaining) ->
                                        Bukkit.getOnlinePlayers()
                                            .stream()
                                            .filter(player -> player.getName().startsWith(remaining) || player.getName().equalsIgnoreCase(remaining))
                                            .map(player -> Suggest.suggestion(player.getName()))
                                            .toList()
                                )
                            )
                            .then(
                                CommandDsl.argument("count", IntegerArgumentType.integer(1, 100000))
                            )
                    )
                    .executes(History.class, factory));
    }
}
