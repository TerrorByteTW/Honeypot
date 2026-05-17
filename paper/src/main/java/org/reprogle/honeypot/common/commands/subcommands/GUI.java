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

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.component.PagingButtons;
import com.github.stefvanschie.inventoryframework.pane.component.ToggleButton;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.commands.CommandFactory;
import org.reprogle.bytelib.commands.dsl.CommandCallback;
import org.reprogle.bytelib.commands.dsl.CommandDsl;
import org.reprogle.bytelib.commands.dsl.LiteralNode;
import org.reprogle.bytelib.commands.dsl.PermissionChecks;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.bytelib.config.Translator;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.api.events.HoneypotCreateEvent;
import org.reprogle.honeypot.api.events.HoneypotPreCreateEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.storageproviders.HoneypotRegionObject;
import org.reprogle.honeypot.common.utils.HoneypotCreatingPlayer;
import org.reprogle.honeypot.common.utils.HoneypotLogger;
import org.reprogle.honeypot.common.utils.RegionOutliner;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;
import org.reprogle.honeypot.common.utils.integrations.GriefPreventionAdapter;
import org.reprogle.honeypot.common.utils.integrations.LandsAdapter;
import org.reprogle.honeypot.common.utils.integrations.WorldGuardAdapter;

import javax.annotation.Nullable;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// Some Paper methods are marked with the Obsolete annotation instead of Deprecated, and Sonarlint treats that as deprecated. 
// So, SuppressWarnings("deprecation") works, but my IDE considers it "unnecessary". Instead, we disable the SonarLint rule
@SuppressWarnings("java:S1874")
public class GUI implements CommandCallback {

    private final JavaPlugin plugin;
    private final BytePluginConfig config;
    private final HoneypotRegionManager regionManager;
    private final CommandFeedback commandFeedback;
    private final AdapterManager adapterManager;
    private final Translator translator;
    private final HoneypotLogger logger;

    @Inject
    GUI(JavaPlugin plugin, BytePluginConfig config, HoneypotRegionManager regionManager, CommandFeedback commandFeedback, AdapterManager adapterManager, Translator translator, HoneypotLogger logger) {
        this.plugin = plugin;
        this.config = config;
        this.regionManager = regionManager;
        this.commandFeedback = commandFeedback;
        this.adapterManager = adapterManager;
        this.translator = translator;
        this.logger = logger;
    }

    @SuppressWarnings({"java:S1192", "java:S1121"})
    private void customHoneypotsInventory(Player p) {
        PaginatedPane pages = new PaginatedPane(9, 2);
        AtomicReference<String> type = new AtomicReference<>("block");

        ToggleButton typeToggle = new ToggleButton(1, 1, Pane.Priority.HIGH);
        typeToggle.setDisabledItem(button("SPYGLASS", "As Block", "Create the Honeypot as a single block"));
        typeToggle.setEnabledItem(button("FILLED_MAP", "As Region", "Create the Honeypot as a region"));
        typeToggle.setOnClick(_ -> type.set(typeToggle.isEnabled() ? "region" : "block"));

        String defaultMode = config.require("gui").getString("default-creation-mode", "block");
        if (defaultMode.equalsIgnoreCase("region")) {
            type.set("region");
            typeToggle.toggle();
        }

        if (!p.hasPermission("honeypot.create.region")) {
            type.set("block");
            if (typeToggle.isEnabled()) typeToggle.toggle();
            typeToggle.allowToggle(false);
        }

        // Collect + dedupe types (config keys + behavior providers)
        Set<String> typeSet = new HashSet<>();
        for (Object key : config.require("honeypots").getKeys()) {
            typeSet.add(String.valueOf(key));
        }

        Registry.getBehaviorRegistry()
            .getBehaviorProviders()
            .forEach((providerName, provider) -> typeSet.add(providerName));

        List<String> types = new ArrayList<>(typeSet);
        types.sort(String.CASE_INSENSITIVE_ORDER);

        fillPages(pages, types, 18, action -> {
            BehaviorProvider provider = Registry.getBehaviorRegistry().getBehaviorProvider(action);

            // Provider not present => type came from config; use config icon
            String iconMaterial = (provider == null)
                ? config.require("honeypots").getString(action + ".icon")
                : provider.getIcon().name();

            return button(
                iconMaterial,
                action,
                "Click to create a Honeypot of this type",
                event -> createHoneypotFromGUI(event, action, type.get())
            );
        });

        ChestGui gui = createPagedGui("Custom Honeypot", 3, pages);

        gui.addPane(Slot.fromXY(4, 2), typeToggle);

        gui.show(p);
    }

    private void allHoneypotsInventory(Player p) {
        if (!p.hasPermission("honeypot.locate")) {
            p.sendMessage(commandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        PaginatedPane pages = new PaginatedPane(9, 2);

        boolean displayAsPot = config.require("gui").getBoolean("display-button-as-honeypot");

        List<HoneypotRegionObject> blocks = new ArrayList<>(regionManager.getAllHoneypots());
        // Sort based on distance to player
        blocks.sort(Comparator.comparingDouble(b -> b.getPos1().distanceSquared(p.getLocation())));

        fillPages(pages, blocks, 18, block -> {
            BehaviorProvider provider = Registry.getBehaviorRegistry().getBehaviorProvider(block.getAction());

            // Provider not present => type came from config; use config icon
            String iconMaterial = (provider == null)
                ? config.require("honeypots").getString(block.getAction() + ".icon")
                : provider.getIcon().name();

            String mat = displayAsPot && block.isSingleBlockRegion() ? block.getPos1().getBlock().getType().name() : iconMaterial;

            return button(mat,
                "Region: " + block.getPos1().getBlockX() + ", " + block.getPos1().getBlockY() + ", " + block.getPos1().getBlockZ(),
                "Click to teleport to Honeypot",
                e -> {
                    Player clicker = (Player) e.getWhoClicked();
                    clicker.sendMessage(Component.text("Whoosh!", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
                    clicker.teleportAsync(block.getPos1().add(0.5, 1, 0.5));
                    clicker.closeInventory();
                }
            );
        });

        ChestGui gui = createPagedGui("All Honeypots", 3, pages);
        gui.show(p);
    }

    private void historyQueryInventory(Player p) {
        if (!p.hasPermission("honeypot.history")) {
            p.sendMessage(commandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        PaginatedPane pages = new PaginatedPane(9, 2);

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));

        fillPages(pages, players, 18, player -> {
            ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
            if (!(skullItem.getItemMeta() instanceof SkullMeta skullMeta)) {
                // Fallback: should be extremely rare
                return button(
                    Material.PAPER.name(),
                    player.getName(),
                    "Click to query history",
                    event -> {
                        event.getWhoClicked().closeInventory();
                        Bukkit.dispatchCommand(event.getWhoClicked(), "honeypot history query " + player.getName());
                    }
                );
            }

            skullMeta.setOwningPlayer(player);
            skullMeta.displayName(Component.text(player.getName()));
            skullItem.setItemMeta(skullMeta);

            return new GuiItem(skullItem, event -> {
                event.getWhoClicked().closeInventory();
                Bukkit.dispatchCommand(event.getWhoClicked(), "honeypot history query " + player.getName());
            });
        });

        ChestGui gui = createPagedGui("Query Player History", 3, pages);
        gui.show(p);
    }

    @SuppressWarnings({"java:S3776", "java:S1192"})
    private void removeHoneypotInventory(Player p) {
        if (!(p.hasPermission("honeypot.remove"))) {
            p.sendMessage(commandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        ChestGui gui = new ChestGui(1, "Remove Honeypots");

        StaticPane options = new StaticPane(9, 1);
        options.addItem(button(
            config.require("gui").getString("remove-buttons.remove-all-button"),
            "Remove all Honeypots",
            null,
            event -> {
                event.getWhoClicked().closeInventory();
                regionManager.deleteAllHoneypotBlocks();
                p.sendMessage(commandFeedback.sendCommandFeedback("deleted.all"));
            }
        ), 3, 0);

        options.addItem(button(
            config.require("gui").getString("remove-buttons.remove-near-button"),
            "Remove nearby Honeypots",
            null,
            event -> {
                event.getWhoClicked().closeInventory();
                final int radius = config.config().getInt("search-range");
                List<HoneypotRegionObject> honeypots = Registry.getRegionStore().getNearbyHoneypotRegions(p.getLocation(), radius);

                if (honeypots.isEmpty()) {
                    p.sendMessage(commandFeedback.sendCommandFeedback("no-pots-found"));
                    return;
                }

                // We want to delete the region, so we can just pass in one of the corners and delete it that way
                for (HoneypotRegionObject honeypot : honeypots) {
                    regionManager.deleteRegionContaining(honeypot.getPos1().getBlock());
                }

                p.sendMessage(commandFeedback.sendCommandFeedback("deleted.near"));
            }
        ), 4, 0);

        options.addItem(button(
            config.require("gui").getString("remove-buttons.remove-target-button"),
            "Remove the Honeypot you're targeting",
            null,
            event -> {
                Block block;
                event.getWhoClicked().closeInventory();

                if (event.getWhoClicked().getTargetBlockExact(5) != null) {
                    block = event.getWhoClicked().getTargetBlockExact(5);
                } else {
                    event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("not-looking-at-block"));
                    return;
                }

                if (block == null) {
                    event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("not-looking-at-block"));
                    return;
                }

                if (regionManager.isHoneypotBlock(block)) {
                    regionManager.deleteRegionContaining(block);
                    p.sendMessage(commandFeedback.sendCommandFeedback("success.removed"));
                } else {
                    event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("not-a-honeypot"));
                }
            }
        ), 5, 0);

        gui.addPane(Slot.fromXY(0, 0), options);
        gui.show(p);
    }

    @SuppressWarnings({"unchecked", "java:S3776", "java:S6541"})
    private void createHoneypotFromGUI(InventoryClickEvent event, String action, String type) {
        event.getWhoClicked().closeInventory();

        if (type.equalsIgnoreCase("region")) {
            if (!(event.getWhoClicked() instanceof Player p)) return;

            if (Create.playersCreatingRegions.containsKey(p.getUniqueId())) {
                event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("creating-region.already-creating"));
                return;
            }

            Create.playersCreatingRegions.put(p.getUniqueId(), new HoneypotCreatingPlayer(p, action));

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
            return;
        }

        Block block;
        WorldGuardAdapter wga = adapterManager.getWorldGuardAdapter();
        GriefPreventionAdapter gpa = adapterManager.getGriefPreventionAdapter();
        LandsAdapter la = adapterManager.getLandsAdapter();

        // Get block the player is looking at
        if (event.getWhoClicked().getTargetBlockExact(5) != null) {
            block = event.getWhoClicked().getTargetBlockExact(5);
        } else {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("not-looking-at-block"));
            return;
        }

        // Check if in a WorldGuard region and the flag is set to deny. If it is, don't
        // bother continuing
        if (wga != null && !wga.isAllowed((Player) event.getWhoClicked(), block.getLocation())) {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("worldguard"));
            return;
        }

        // Check if in a GriefPrevention region.
        if (gpa != null && !gpa.isAllowed((Player) event.getWhoClicked(), block.getLocation())) {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("griefprevention"));
            return;
        }

        // Check if in a Lands land
        if (la != null && !la.isAllowed(block.getLocation())) {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("lands"));
            return;
        }

        if (config.config().getBoolean("filters.blocks")
            || config.config().getBoolean("filters.inventories")) {
            List<String> allowedBlocks = (List<String>) config.config()
                .getList("allowed-blocks");
            List<String> allowedInventories = (List<String>) config.config()
                .getList("allowed-inventories");
            boolean allowed = false;

            if (config.config().getBoolean("filters.blocks")) {
                for (String blockType : allowedBlocks) {
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

            if (!allowed) {
                event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("against-filter"));
                return;
            }
        }

        if (regionManager.isHoneypotBlock(block)) {
            event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("already-exists"));

            // If it does not have a honeypot tag or the honeypot tag does not equal 1,
            // create one
        } else {

            // Fire HoneypotPreCreateEvent
            HoneypotPreCreateEvent hpce = new HoneypotPreCreateEvent((Player) event.getWhoClicked(), block);
            Bukkit.getPluginManager().callEvent(hpce);

            if (hpce.isCancelled())
                return;

            regionManager.createBlock(block, action);
            event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("success.created"));

            // Fire HoneypotCreateEvent
            HoneypotCreateEvent hce = new HoneypotCreateEvent((Player) event.getWhoClicked(), block);
            Bukkit.getPluginManager().callEvent(hce);
        }
    }

    @SuppressWarnings("java:S3776")
    public void mainMenu(Player p) {
        ChestGui gui = new ChestGui(1, "Honeypot");

        StaticPane navigation = new StaticPane(9, 1);

        if (p.hasPermission("honeypot.create"))
            navigation.addItem(button(
                config.require("gui").getString("main-buttons.create-button"),
                "Create a Honeypot",
                null,
                event -> this.customHoneypotsInventory(p)
            ), 2, 0);
        else
            navigation.addItem(button(
                "BARRIER",
                "Create a Honeypot (No Permission)",
                null,
                event -> p.sendMessage(translator.tr("no-permission"))
            ), 2, 0);

        if (p.hasPermission("honeypot.remove"))
            navigation.addItem(button(
                config.require("gui").getString("main-buttons.remove-button"),
                "Remove a Honeypot",
                null,
                event -> this.removeHoneypotInventory(p)
            ), 3, 0);
        else
            navigation.addItem(button(
                "BARRIER",
                "Remove a Honeypot (No Permission)",
                null,
                event -> p.sendMessage(translator.tr("no-permission"))
            ), 3, 0);


        navigation.addItem(button(
            config.require("gui").getString("main-buttons.list-button"),
            "List all Honeypots",
            null,
            event -> this.allHoneypotsInventory(p)
        ), 4, 0);

        if (p.hasPermission("honeypot.locate"))
            navigation.addItem(button(
                config.require("gui").getString("main-buttons.locate-button"),
                "Locate nearby Honeypots",
                null,
                event -> {
                    event.getWhoClicked().closeInventory();
                    if (!(p.hasPermission("honeypot.locate"))) {
                        p.sendMessage(commandFeedback.sendCommandFeedback("nopermission"));
                        return;
                    }

                    final int radius = config.config().getInt("search-range");

                    boolean potFound = false;

                    List<HoneypotRegionObject> honeypots = Registry.getRegionStore().getNearbyHoneypotRegions(p.getLocation(), radius);
                    if (!honeypots.isEmpty()) potFound = true;

                    for (HoneypotRegionObject honeypot : honeypots) {
                        if (honeypot.isSingleBlockRegion()) {
                            RegionOutliner.spawnSlime(plugin, p.getWorld(), honeypot.getPos1().getX(), honeypot.getPos1().getY(), honeypot.getPos1().getZ());
                        } else {
                            RegionOutliner.outlineBoundingBox(plugin, p, honeypot.getPos1(), honeypot.getPos2());
                        }
                    }

                    // Let the player know if a pot was found or not
                    if (potFound) {
                        p.sendMessage(commandFeedback.sendCommandFeedback("found-pots"));
                    } else {
                        p.sendMessage(commandFeedback.sendCommandFeedback("no-pots-found"));
                    }
                }
            ), 5, 0);
        else
            navigation.addItem(button(
                "BARRIER",
                "Locate nearby Honeypots (No Permission)",
                null,
                event -> p.sendMessage(translator.tr("no-permission"))
            ), 5, 0);

        if (p.hasPermission("honeypot.history"))
            navigation.addItem(button(
                config.require("gui").getString("main-buttons.history-button"),
                "Query player history",
                null,
                event -> this.historyQueryInventory(p)
            ), 6, 0);
        else
            navigation.addItem(button(
                "BARRIER",
                "Query player history (No Permission)",
                null,
                event -> p.sendMessage(translator.tr("no-permission"))
            ), 6, 0);

        gui.addPane(Slot.fromXY(0, 0), navigation);

        gui.show(p);
    }

    public void callAllHoneypotsInventory(Player p) {
        allHoneypotsInventory(p);
    }

    private GuiItem button(String materialName, String name, @Nullable String lore, Consumer<InventoryClickEvent> onClick) {
        return button(
            materialName,
            name,
            lore,
            (event, _) -> onClick.accept(event)
        );
    }

    private GuiItem button(String materialName, String name, @Nullable String lore) {
        return button(
            materialName,
            name,
            lore,
            (_, _) -> {
            }
        );
    }

    private GuiItem button(String materialName, String name, @Nullable String lore, BiConsumer<InventoryClickEvent, ItemStack> onClick) {
        // This seems confusing, but basically it's a bunch of fallbacks. We use the material provided, and if that doeesn't work
        // then we use the default-gui-button material, and if that doesn't work then we use Material.PAPER
        Material defaultButton = safeGetMaterial(config.require("gui").getString("default-gui-button"));
        ItemStack item = new ItemStack(safeGetMaterial(materialName, defaultButton));

        if (item.getType().equals(Material.AIR)) {
            // The only time a Honeypot can be AIR is if it was broken somehow (Such as by flowing water if optional events are turned off). The Ghost Honeypot Monitor will clean it up eventually.
            item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("Broken Honeypot", NamedTextColor.RED));
            meta.lore(List.of(Component.text("This Honeypot has been broken and does not exist in the world anymore. It will be removed automatically soon")));
            item.setItemMeta(meta);
        } else {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(name));
                if (lore != null && !lore.isBlank()) meta.lore(List.of(Component.text(lore, NamedTextColor.GRAY)));

                item.setItemMeta(meta);
            }
        }

        return new GuiItem(item, e -> {
            e.setCancelled(true);
            onClick.accept(e, e.getCurrentItem());
        });
    }

    private ChestGui createPagedGui(String title, int rows, PaginatedPane pages) {
        ChestGui gui = new ChestGui(rows, title);

        // Cancel everything by default
        gui.setOnGlobalClick(e -> e.setCancelled(true));

        // Bottom row background
        OutlinePane background = new OutlinePane(9, 1);
        background.addItem(new GuiItem(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE), e -> e.setCancelled(true)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);

        gui.addPane(Slot.fromXY(0, rows - 1), background);
        gui.addPane(Slot.fromXY(0, 0), pages);

        PagingButtons paging = new PagingButtons(9, pages);
        paging.setBackwardButton(button("ARROW", "Previous Page", null, e -> {
        }));
        paging.setForwardButton(button("ARROW", "Next Page", null, e -> {
        }));
        gui.addPane(Slot.fromXY(0, rows - 1), paging);

        return gui;
    }

    private <T> void fillPages(PaginatedPane pages, List<T> items, int pageSize, java.util.function.Function<T, GuiItem> toGuiItem) {
        for (int i = 0; i < items.size(); i += pageSize) {
            List<T> chunk = items.subList(i, Math.min(i + pageSize, items.size()));
            OutlinePane pane = new OutlinePane(9, 2);

            for (T t : chunk) {
                pane.addItem(toGuiItem.apply(t));
            }

            pages.addPage(Slot.fromXY(0, 0), pane);
        }

        // Ensure at least one empty page so the GUI still renders nicely with paging buttons
        if (items.isEmpty()) {
            pages.addPage(Slot.fromXY(0, 0), new OutlinePane(9, 2));
        }
    }

    public static Material safeGetMaterial(String materialName) {
        return safeGetMaterial(materialName, Material.PAPER);
    }

    public static Material safeGetMaterial(String materialName, Material defaultMaterial) {
        Material material = Material.getMaterial(materialName);
        return material != null && material.asItemType() != null ? material : defaultMaterial;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) throws Exception {
        Player player = (Player) ctx.getSource().getSender(); // This is safe because we guarantee it's a player based on the requirement

        this.mainMenu(player);
        player.updateCommands();

        return Command.SINGLE_SUCCESS;
    }

    public static LiteralNode commandTree(BytePluginConfig config, CommandFactory factory) {
        return CommandDsl.literal("gui")
            .requires(
                PermissionChecks.allOf(
                    PermissionChecks.anyOf(
                        PermissionChecks.permission("honeypot.gui"),
                        PermissionChecks.permission("honeypot.*"),
                        PermissionChecks.isOp()
                    ),
                    PermissionChecks.playerOnly()
                )
            )
            .executes(GUI.class, factory);
    }
}
