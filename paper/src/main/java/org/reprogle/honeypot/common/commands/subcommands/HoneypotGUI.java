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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.api.events.HoneypotPreCreateEvent;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;
import org.reprogle.honeypot.common.utils.integrations.GriefPreventionAdapter;
import org.reprogle.honeypot.common.utils.integrations.LandsAdapter;
import org.reprogle.honeypot.common.utils.integrations.WorldGuardAdapter;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

// Some Paper methods are marked with the Obsolete annotation instead of Deprecated, and Sonarlint treats that as deprecated. 
// So, SuppressWarnings("deprecation") works, but my IDE considers it "unnecessary". Instead, we disable the SonarLint rule
@SuppressWarnings("java:S1874")
public class HoneypotGUI implements HoneypotSubCommand {

    private final Honeypot plugin;
    private final HoneypotConfigManager configManager;
    private final HoneypotBlockManager blockManager;
    private final CommandFeedback commandFeedback;
    private final AdapterManager adapterManager;

    @Inject
    HoneypotGUI(Honeypot plugin, HoneypotConfigManager configManager, HoneypotBlockManager blockManager, CommandFeedback commandFeedback, AdapterManager adapterManager) {
        this.plugin = plugin;
        this.configManager = configManager;
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
        p.openInventory(mainMenu(p).getInventory());
    }

    @SuppressWarnings({"java:S1192", "java:S1121"})
    private void customHoneypotsInventory(Player p) {
        SGMenu customHoneypotsGUI = plugin.getGUI().create("Custom Honeypot", 3);
        List<String> types = new ArrayList<>();

        Set<Object> keys = configManager.getHoneypotsConfig().getKeys();
        for (Object key : keys) {
            types.add(key.toString());
        }

        ConcurrentMap<String, BehaviorProvider> map = Registry.getBehaviorRegistry().getBehaviorProviders();
        map.forEach((providerName, provider) -> types.add(providerName));

        for (String type : types) {

            ItemBuilder item;

            if (Registry.getBehaviorRegistry().getBehaviorProvider(type) == null) {
                String action = configManager.getHoneypotsConfig().getString(type + ".icon");

                if (action != null && !action.isEmpty()) {
                    item = new ItemBuilder(safeGetMaterial(action));
                } else {
                    item = new ItemBuilder(Material.PAPER);
                }
            } else {
                item = new ItemBuilder(Registry.getBehaviorRegistry().getBehaviorProvider(type).getIcon());
            }

            item.name(type);
            item.lore("Click to create a Honeypot of this type");
            SGButton button = new SGButton(item.build())
                    .withListener((InventoryClickEvent event) -> createHoneypotFromGUI(event, type));

            customHoneypotsGUI.addButton(button);
        }

        p.openInventory(customHoneypotsGUI.getInventory());
    }

    @SuppressWarnings("java:S1192")
    private void allHoneypotsInventory(Player p) {
        if (!(p.hasPermission("honeypot.locate"))) {
            p.sendMessage(commandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        SGMenu allBlocksGUI = plugin.getGUI().create("Honeypots {currentPage}/{maxPage}", 3);

        for (HoneypotBlockObject honeypotBlock : blockManager.getAllHoneypots(p.getWorld())) {
            ItemBuilder item;

            if (configManager.getGuiConfig().getBoolean("display-button-as-honeypot")) {
                item = new ItemBuilder(honeypotBlock.getBlock().getType());
            } else {
                item = new ItemBuilder(
                        safeGetMaterial(configManager.getGuiConfig().getString("default-gui-button")));
            }
            item.lore("Click to teleport to Honeypot");
            item.name("Honeypot: " + honeypotBlock.getCoordinates());

            SGButton button = new SGButton(item.build()).withListener((InventoryClickEvent event) -> {
                event.getWhoClicked().sendMessage(Component.text("Whoosh!", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));

                // In the future, we're going to make this nice and pretty. Until then, ew.
                event.getWhoClicked().teleportAsync(honeypotBlock.getLocation().add(0.5, 1, 0.5));
                event.getWhoClicked().closeInventory();
            });

            allBlocksGUI.addButton(button);

        }

        p.openInventory(allBlocksGUI.getInventory());
    }

    private void historyQueryInventory(Player p) {
        if (!(p.hasPermission("honeypot.history"))) {
            p.sendMessage(commandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        SGMenu historyQueryGUI = plugin.getGUI().create("Query Player History", 3);

        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemBuilder item;

            ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
            assert skullMeta != null;
            skullMeta.setOwningPlayer(player);
            skullItem.setItemMeta(skullMeta);

            item = new ItemBuilder(skullItem);
            item.name(player.getName());

            SGButton button = new SGButton(item.build()).withListener((InventoryClickEvent event) -> {
                event.getWhoClicked().closeInventory();
                Bukkit.dispatchCommand(event.getWhoClicked(), "honeypot history query " + item.getName());
            });

            historyQueryGUI.addButton(button);

        }

        p.openInventory(historyQueryGUI.getInventory());
    }

    @SuppressWarnings({"java:S3776", "java:S1192"})
    private void removeHoneypotInventory(Player p) {
        if (!(p.hasPermission("honeypot.remove"))) {
            p.sendMessage(commandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        SGMenu removeGUI = plugin.getGUI().create("Remove Honeypots", 1);

        ItemBuilder removeAllItem = new ItemBuilder(Material
                .getMaterial(configManager.getGuiConfig().getString("remove-buttons.remove-all-button")));
        removeAllItem.name("Remove all Honeypots");

        ItemBuilder removeNearItem = new ItemBuilder(Material
                .getMaterial(configManager.getGuiConfig().getString("remove-buttons.remove-near-button")));
        removeNearItem.name("Remove nearby Honeypots");

        ItemBuilder removeTargetItem = new ItemBuilder(Material
                .getMaterial(configManager.getGuiConfig().getString("remove-buttons.remove-target-button")));
        removeTargetItem.name("Remove the Honeypot you're targeting");

        SGButton removeAllButton = new SGButton(removeAllItem.build()).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().closeInventory();
            blockManager.deleteAllHoneypotBlocks(p.getWorld());
            p.sendMessage(commandFeedback.sendCommandFeedback("deleted", true));
        });

        SGButton removeNearButton = new SGButton(removeNearItem.build()).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().closeInventory();
            final int radius = configManager.getPluginConfig().getInt("search-range");
            List<HoneypotBlockObject> honeypots = Registry.getStorageProvider().getNearbyHoneypots(p.getLocation(), radius);

            if (honeypots.isEmpty()) {
                p.sendMessage(commandFeedback.sendCommandFeedback("no-pots-found"));
                return;
            }

            for (HoneypotBlockObject honeypot : honeypots) {
                blockManager.deleteBlock(honeypot.getBlock());
            }

            p.sendMessage(commandFeedback.sendCommandFeedback("deleted", false));
        });

        SGButton removeTargetButton = new SGButton(removeTargetItem.build())
                .withListener((InventoryClickEvent event) -> {
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
                });

        removeGUI.setButton(3, removeTargetButton);
        removeGUI.setButton(4, removeNearButton);
        removeGUI.setButton(5, removeAllButton);

        p.openInventory(removeGUI.getInventory());

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

        if (configManager.getPluginConfig().getBoolean("filters.blocks")
                || configManager.getPluginConfig().getBoolean("filters.inventories")) {
            List<String> allowedBlocks = (List<String>) configManager.getPluginConfig()
                    .getList("allowed-blocks");
            List<String> allowedInventories = (List<String>) configManager.getPluginConfig()
                    .getList("allowed-inventories");
            boolean allowed = false;

            if (configManager.getPluginConfig().getBoolean("filters.blocks")) {
                for (String blockType : allowedBlocks) {
                    if (block.getType().name().equals(blockType)) {
                        allowed = true;
                        break;
                    }
                }
            }

            if (configManager.getPluginConfig().getBoolean("filters.inventories")) {
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
    public SGMenu mainMenu(Player p) {
        SGMenu mainMenu = plugin.getGUI().create("Honeypot Main Menu", 1);
        ItemBuilder createItem;
        ItemBuilder removeItem;
        ItemBuilder listItem;
        ItemBuilder locateItem;
        ItemBuilder historyItem;

        createItem = new ItemBuilder(
                safeGetMaterial(configManager.getGuiConfig().getString("main-buttons.create-button")));
        createItem.name("Create a Honeypot");

        removeItem = new ItemBuilder(
                safeGetMaterial(configManager.getGuiConfig().getString("main-buttons.remove-button")));
        removeItem.name("Remove a Honeypot");

        listItem = new ItemBuilder(
                safeGetMaterial(configManager.getGuiConfig().getString("main-buttons.list-button")));
        listItem.name("List all Honeypots");

        locateItem = new ItemBuilder(
                safeGetMaterial(configManager.getGuiConfig().getString("main-buttons.locate-button")));
        locateItem.name("Locate nearby Honeypots");

        historyItem = new ItemBuilder(
                safeGetMaterial(configManager.getGuiConfig().getString("main-buttons.history-button")));
        historyItem.name("Query player history");

        SGButton createButton = new SGButton(createItem.build())
                .withListener((InventoryClickEvent event) -> customHoneypotsInventory(p));

        SGButton removeButton = new SGButton(removeItem.build())
                .withListener((InventoryClickEvent event) -> removeHoneypotInventory(p));

        SGButton listButton = new SGButton(listItem.build())
                .withListener((InventoryClickEvent event) -> allHoneypotsInventory(p));

        SGButton locateButton = new SGButton(locateItem.build()).withListener((InventoryClickEvent event) -> {
            event.getWhoClicked().closeInventory();
            if (!(p.hasPermission("honeypot.locate"))) {
                p.sendMessage(commandFeedback.sendCommandFeedback("nopermission"));
                return;
            }

            final int radius = configManager.getPluginConfig().getInt("search-range");

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
                slime.setHealth(slime.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
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
        });

        SGButton historyButton = new SGButton(historyItem.build())
                .withListener((InventoryClickEvent event) -> historyQueryInventory(p));

        mainMenu.setButton(2, createButton);
        mainMenu.setButton(3, removeButton);
        mainMenu.setButton(4, listButton);
        mainMenu.setButton(5, locateButton);
        mainMenu.setButton(6, historyButton);

        return mainMenu;
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
}
