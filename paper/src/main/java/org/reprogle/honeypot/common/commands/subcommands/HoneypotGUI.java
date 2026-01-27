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
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.api.events.HoneypotPreCreateEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.utils.HoneypotPermission;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;
import org.reprogle.honeypot.common.utils.integrations.GriefPreventionAdapter;
import org.reprogle.honeypot.common.utils.integrations.LandsAdapter;
import org.reprogle.honeypot.common.utils.integrations.WorldGuardAdapter;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

// Some Paper methods are marked with the Obsolete annotation instead of Deprecated, and Sonarlint treats that as deprecated. 
// So, SuppressWarnings("deprecation") works, but my IDE considers it "unnecessary". Instead, we disable the SonarLint rule
@SuppressWarnings("java:S1874")
public class HoneypotGUI implements HoneypotSubCommand {

    private final JavaPlugin plugin;
    private final BytePluginConfig config;
    private final HoneypotBlockManager blockManager;
    private final CommandFeedback commandFeedback;
    private final AdapterManager adapterManager;

    @Inject
    HoneypotGUI(JavaPlugin plugin, BytePluginConfig config, HoneypotBlockManager blockManager, CommandFeedback commandFeedback, AdapterManager adapterManager) {
        this.plugin = plugin;
        this.config = config;
        this.blockManager = blockManager;
        this.commandFeedback = commandFeedback;
        this.adapterManager = adapterManager;
    }

    @Override
    public String getName() {
        return "gui";
    }

    @Override
    @SuppressWarnings("java:S1192")
    public void perform(Player p, String[] args) {
        this.mainMenu(p);
    }

    @SuppressWarnings({"java:S1192", "java:S1121"})
    private void customHoneypotsInventory(Player p) {
        ChestGui gui = new ChestGui(3, "Custom Honeypot");
        OutlinePane background = new OutlinePane(0, 2, 9, 1);

        background.addItem(new GuiItem(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);

        gui.addPane(background);

        PaginatedPane pages = new PaginatedPane(0, 0, 9, 2);

        List<String> types = new ArrayList<>();

        Set<Object> keys = config.require("honeypots").getKeys();
        for (Object key : keys) {
            types.add(key.toString());
        }

        ConcurrentMap<String, BehaviorProvider> map = Registry.getBehaviorRegistry().getBehaviorProviders();
        map.forEach((providerName, provider) -> types.add(providerName));

        // 18 is 2*9, with 2 being the number of rows that will be used for populating Custom Honeypots
        for (int x = 0; x < types.size(); x += 18) {
            List<String> chunk = types.subList(x, Math.min(x + 18, types.size()));
            OutlinePane oPane = new OutlinePane(0, 0, 9, 2);

            for (String type : chunk) {
                GuiItem item;

                if (Registry.getBehaviorRegistry().getBehaviorProvider(type) == null) {
                    // The behavior registry will not have behaviors defined in the Custom Honeypots config file, so fallback to this
                    String action = config.require("honeypots").getString(type + ".icon");

                    item = createInventoryButtonItem(action, type, "Click to create a Honeypot of this type", event -> createHoneypotFromGUI(event, type));
                } else {
                    item = createInventoryButtonItem(Registry.getBehaviorRegistry().getBehaviorProvider(type).getIcon().name(), type, "Click to create a Honeypot of this type", event -> createHoneypotFromGUI(event, type));
                }

                oPane.addItem(item);
            }

            pages.addPage(oPane);
        }

        gui.addPane(pages);

        PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(0, 2), 9, pages);
        gui.addPane(pagingButtons);
        gui.show(p);
    }

    @SuppressWarnings("java:S1192")
    private void allHoneypotsInventory(Player p) {
        if (!(p.hasPermission("honeypot.locate"))) {
            p.sendMessage(commandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        ChestGui gui = new ChestGui(3, "All Honeypots");
        OutlinePane background = new OutlinePane(0, 2, 9, 1);

        background.addItem(new GuiItem(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);

        gui.addPane(background);

        PaginatedPane pages = new PaginatedPane(0, 0, 9, 2);

        List<HoneypotBlockObject> blocks = blockManager.getAllHoneypots(p.getWorld());

        boolean displayAsPot = config.require("gui").getBoolean("display-button-as-honeypot");

        // 18 is 2*9, with 2 being the number of rows that will be used for populating Custom Honeypots
        for (int x = 0; x < blocks.size(); x += 18) {
            List<HoneypotBlockObject> chunk = blocks.subList(x, Math.min(x + 18, blocks.size()));
            OutlinePane oPane = new OutlinePane(0, 0, 9, 2);

            for (HoneypotBlockObject block : chunk) {
                GuiItem item;

                if (displayAsPot) {
                    item = createInventoryButtonItem(block.getBlock().getType().name(), "Honeypot: " + block.getCoordinates(), "Click to teleport to Honeypot", event -> {
                        event.getWhoClicked().sendMessage(Component.text("Whoosh!", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));

                        // In the future, we're going to make this nice and pretty. Until then, ew.
                        event.getWhoClicked().teleportAsync(block.getLocation().add(0.5, 1, 0.5));
                        event.getWhoClicked().closeInventory();
                    });
                } else {
                    item = createInventoryButtonItem(config.require("gui").getString("default-gui-button"), "Honeypot: " + block.getCoordinates(), "Click to teleport to Honeypot", event -> {
                        event.getWhoClicked().sendMessage(Component.text("Whoosh!", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));

                        // In the future, we're going to make this nice and pretty. Until then, ew.
                        event.getWhoClicked().teleportAsync(block.getLocation().add(0.5, 1, 0.5));
                        event.getWhoClicked().closeInventory();
                    });
                }

                oPane.addItem(item);
            }

            pages.addPage(oPane);
        }

        gui.addPane(pages);

        PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(0, 2), 9, pages);
        gui.addPane(pagingButtons);
        gui.show(p);
    }

    private void historyQueryInventory(Player p) {
        if (!(p.hasPermission("honeypot.history"))) {
            p.sendMessage(commandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        ChestGui gui = new ChestGui(3, "Query Player History");
        OutlinePane background = new OutlinePane(0, 2, 9, 1);

        background.addItem(new GuiItem(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);

        gui.addPane(background);

        PaginatedPane pages = new PaginatedPane(0, 0, 9, 2);

        List<Player> players = ImmutableList.copyOf(Bukkit.getOnlinePlayers());

        // 18 is 2*9, with 2 being the number of rows that will be used for populating Custom Honeypots
        for (int x = 0; x < players.size(); x += 18) {
            List<Player> chunk = players.subList(x, Math.min(x + 18, players.size()));
            OutlinePane oPane = new OutlinePane(0, 0, 9, 2);

            for (Player player : chunk) {
                GuiItem item;

                ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
                assert skullMeta != null;
                skullMeta.setOwningPlayer(player);
                skullMeta.displayName(Component.text(player.getName()));
                skullItem.setItemMeta(skullMeta);

                item = new GuiItem(skullItem, event -> {
                    event.getWhoClicked().closeInventory();
                    Bukkit.dispatchCommand(event.getWhoClicked(), "honeypot history query " + PlainTextComponentSerializer.plainText().serialize(skullItem.displayName()));
                });

                oPane.addItem(item);
            }

            pages.addPage(oPane);
        }

        gui.addPane(pages);

        PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(0, 2), 9, pages);
        gui.addPane(pagingButtons);
        gui.show(p);
    }

    @SuppressWarnings({"java:S3776", "java:S1192"})
    private void removeHoneypotInventory(Player p) {
        if (!(p.hasPermission("honeypot.remove"))) {
            p.sendMessage(commandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        ChestGui gui = new ChestGui(1, "Remove Honeypots");

        StaticPane options = new StaticPane(0, 0, 9, 1);
        options.addItem(createInventoryButtonItem(
                config.require("gui").getString("remove-buttons.remove-all-button"),
                "Remove all Honeypots",
                null,
                event -> {
                    event.getWhoClicked().closeInventory();
                    blockManager.deleteAllHoneypotBlocks(p.getWorld());
                    p.sendMessage(commandFeedback.sendCommandFeedback("deleted", true));
                }
        ), 3, 0);

        options.addItem(createInventoryButtonItem(
                config.require("gui").getString("remove-buttons.remove-near-button"),
                "Remove nearby Honeypots",
                null,
                event -> {
                    event.getWhoClicked().closeInventory();
                    final int radius = config.config().getInt("search-range");
                    List<HoneypotBlockObject> honeypots = Registry.getStorageProvider().getNearbyHoneypots(p.getLocation(), radius);

                    if (honeypots.isEmpty()) {
                        p.sendMessage(commandFeedback.sendCommandFeedback("no-pots-found"));
                        return;
                    }

                    for (HoneypotBlockObject honeypot : honeypots) {
                        blockManager.deleteBlock(honeypot.getBlock());
                    }

                    p.sendMessage(commandFeedback.sendCommandFeedback("deleted", false));
                }
        ), 4, 0);

        options.addItem(createInventoryButtonItem(
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

                    if (blockManager.isHoneypotBlock(block)) {
                        blockManager.deleteBlock(block);
                        p.sendMessage(commandFeedback.sendCommandFeedback("success", false));
                    } else {
                        event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("not-a-honeypot"));
                    }
                }
        ), 5, 0);

        gui.addPane(options);
        gui.show(p);
    }

    @SuppressWarnings({"unchecked", "java:S3776", "java:S6541"})
    private void createHoneypotFromGUI(InventoryClickEvent event, String action) {
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

        event.getWhoClicked().closeInventory();
        if (blockManager.isHoneypotBlock(block)) {
            event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("already-exists"));

            // If it does not have a honeypot tag or the honeypot tag does not equal 1,
            // create one
        } else {

            // Fire HoneypotPreCreateEvent
            HoneypotPreCreateEvent hpce = new HoneypotPreCreateEvent((Player) event.getWhoClicked(), block);
            Bukkit.getPluginManager().callEvent(hpce);

            if (hpce.isCancelled())
                return;

            blockManager.createBlock(block, action);
            event.getWhoClicked().sendMessage(commandFeedback.sendCommandFeedback("success", true));

            // Fire HoneypotCreateEvent
            HoneypotPreCreateEvent hce = new HoneypotPreCreateEvent((Player) event.getWhoClicked(), block);
            Bukkit.getPluginManager().callEvent(hce);
        }
    }

    @SuppressWarnings("java:S3776")
    public void mainMenu(Player p) {
        ChestGui gui = new ChestGui(1, "Honeypot");

        StaticPane navigation = new StaticPane(0, 0, 9, 1);

        navigation.addItem(createInventoryButtonItem(
                config.require("gui").getString("main-buttons.create-button"),
                "Create a Honeypot",
                null,
                event -> this.customHoneypotsInventory(p)
        ), 2, 0);
        navigation.addItem(createInventoryButtonItem(
                config.require("gui").getString("main-buttons.remove-button"),
                "Remove a Honeypot",
                null,
                event -> this.removeHoneypotInventory(p)
        ), 3, 0);
        navigation.addItem(createInventoryButtonItem(
                config.require("gui").getString("main-buttons.list-button"),
                "List all Honeypots",
                null,
                event -> this.allHoneypotsInventory(p)
        ), 4, 0);
        navigation.addItem(createInventoryButtonItem(
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

                    List<HoneypotBlockObject> honeypots = Registry.getStorageProvider().getNearbyHoneypots(p.getLocation(), radius);
                    if (!honeypots.isEmpty()) potFound = true;

                    for (HoneypotBlockObject honeypot : honeypots) {
                        Slime slime = (Slime) Objects.requireNonNull(Bukkit.getWorld(honeypot.getBlock().getWorld().getName()))
                                .spawnEntity(honeypot.getBlock().getLocation().add(0.5, 0, 0.5), EntityType.SLIME);
                        slime.setSize(2);
                        slime.setAI(false);
                        slime.setGlowing(true);
                        slime.setInvulnerable(true);
                        slime.setHealth(slime.getAttribute(Attribute.MAX_HEALTH).getValue());
                        slime.setInvisible(true);

                        // Remove the slime after 5 seconds
                        // If we kill it, a death animation plays and the slime splits and drops items
                        slime.getScheduler().runDelayed(plugin, scheduledTask -> slime.remove(), null, 20L * 5);
                    }

                    // Let the player know if a pot was found or not
                    if (potFound) {
                        p.sendMessage(commandFeedback.sendCommandFeedback("found-pots"));
                    } else {
                        p.sendMessage(commandFeedback.sendCommandFeedback("no-pots-found"));
                    }
                }
        ), 5, 0);
        navigation.addItem(createInventoryButtonItem(
                config.require("gui").getString("main-buttons.history-button"),
                "Query player history",
                null,
                event -> this.historyQueryInventory(p)
        ), 6, 0);

        gui.addPane(navigation);

        gui.show(p);
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return new ArrayList<>();
    }

    // We will let the individual GUIs handle their own permission checking, but the
    // overall permissions of the GUI will be handled here
    @Override
    public List<HoneypotPermission> getRequiredPermissions() {
        List<HoneypotPermission> permissions = new ArrayList<>();
        permissions.add(new HoneypotPermission("honeypot.gui"));
        return permissions;
    }

    private Material safeGetMaterial(String materialName) {
        Material material = Material.getMaterial(materialName);
        return material != null ? material : Material.PAPER;
    }

    public void callAllHoneypotsInventory(Player p) {
        allHoneypotsInventory(p);
    }

    private GuiItem createInventoryButtonItem(String materialName, String itemName, @Nullable String lore) {
        ItemStack item = new ItemStack(safeGetMaterial(materialName));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(itemName));
        item.setItemMeta(meta);
        if (lore != null && !lore.isEmpty()) item.lore(new ArrayList<>(List.of(Component.text(lore))));

        return new GuiItem(item);
    }

    private GuiItem createInventoryButtonItem(String materialName, String itemName, @Nullable String lore, Consumer<InventoryClickEvent> callback) {
        GuiItem item = createInventoryButtonItem(materialName, itemName, lore);
        item.setAction(callback);

        return item;
    }
}
