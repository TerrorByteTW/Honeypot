/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022-2026 TerrorByte and Honeypot Contributors.
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

package org.reprogle.honeypot.common.utils;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.component.PagingButtons;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.utils.updater.ChangelogEntry;
import org.reprogle.honeypot.common.utils.updater.ReleaseNotes;
import org.reprogle.honeypot.common.utils.updater.VersionManifest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Displays every feature and bug fix from every version newer than the one running on the server.
 * <p>
 * Layout (6 rows): the top row is a honey bottle surrounded by yellow glass, the middle four rows hold the
 * "FEATURES" and "BUG FIXES" sections (each starting with a header row), and the bottom row holds the paging buttons.
 */
public class ChangelogGUI {

    private static final int ROWS = 6;
    private static final int COLUMNS = 9;
    private static final int CONTENT_ROWS = ROWS - 2;
    private static final int WRAP_WIDTH = 40;

    private final JavaPlugin plugin;
    private final CommandFeedback commandFeedback;

    @Inject
    ChangelogGUI(JavaPlugin plugin, CommandFeedback commandFeedback) {
        this.plugin = plugin;
        this.commandFeedback = commandFeedback;
    }

    /**
     * Opens the changelog for the player. Safe to call from any thread, as the GUI is shown on the player's scheduler.
     *
     * @param player         The player to show the changelog to
     * @param manifest       The parsed version manifest
     * @param currentVersion The version of Honeypot running on the server
     */
    public void open(Player player, VersionManifest manifest, String currentVersion) {
        player.getScheduler().run(plugin, _ -> build(manifest, currentVersion).show(player), null);
    }

    private ChestGui build(VersionManifest manifest, String currentVersion) {
        List<ReleaseNotes> releases = manifest.releasesNewerThan(currentVersion);

        ChestGui gui = new ChestGui(ROWS, ComponentHolder.of(tr("changelog-gui.title")));
        gui.setOnGlobalClick(e -> e.setCancelled(true));
        gui.setOnGlobalDrag(e -> e.setCancelled(true));

        StaticPane header = new StaticPane(COLUMNS, 1);
        header.addItem(honeyBottle(manifest, currentVersion, releases), COLUMNS / 2, 0);
        header.fillWith(namedItem(Material.YELLOW_STAINED_GLASS_PANE, Component.empty()));
        gui.addPane(Slot.fromXY(0, 0), header);

        PaginatedPane pages = new PaginatedPane(COLUMNS, CONTENT_ROWS);
        List<List<GuiItem[]>> layout = paginate(List.of(
            buildSection(Section.FEATURES, releases, ReleaseNotes::features),
            buildSection(Section.BUG_FIXES, releases, ReleaseNotes::bugFixes)
        ));

        if (layout.isEmpty()) {
            StaticPane empty = new StaticPane(COLUMNS, CONTENT_ROWS);
            empty.addItem(item(Material.BARRIER, tr("changelog-gui.empty.name"),
                List.of(tr("changelog-gui.empty.lore"))), COLUMNS / 2, 1);
            pages.addPage(Slot.fromXY(0, 0), empty);
        }

        for (List<GuiItem[]> page : layout) {
            StaticPane pane = new StaticPane(COLUMNS, CONTENT_ROWS);
            for (int y = 0; y < page.size(); y++) {
                GuiItem[] row = page.get(y);
                for (int x = 0; x < row.length; x++) {
                    if (row[x] != null) pane.addItem(row[x], x, y);
                }
            }
            pages.addPage(Slot.fromXY(0, 0), pane);
        }

        gui.addPane(Slot.fromXY(0, 1), pages);

        OutlinePane footer = new OutlinePane(COLUMNS, 1);
        footer.addItem(filler(Material.YELLOW_STAINED_GLASS_PANE));
        footer.setRepeat(true);
        footer.setPriority(Pane.Priority.LOWEST);
        gui.addPane(Slot.fromXY(0, ROWS - 1), footer);

        PagingButtons paging = new PagingButtons(COLUMNS, pages);
        paging.setBackwardButton(new GuiItem(namedItem(Material.ARROW, tr("changelog-gui.previous-page"))));
        paging.setForwardButton(new GuiItem(namedItem(Material.ARROW, tr("changelog-gui.next-page"))));
        gui.addPane(Slot.fromXY(0, ROWS - 1), paging);

        return gui;
    }

    /**
     * Converts a section into rows. The first row is always the section header, followed by rows of up to 9 entries.
     * Returns an empty list if the section has no entries, so that it's hidden from the GUI entirely.
     */
    private List<GuiItem[]> buildSection(Section section, List<ReleaseNotes> releases, Function<ReleaseNotes, List<ChangelogEntry>> getter) {
        List<GuiItem> items = new ArrayList<>();
        for (ReleaseNotes release : releases) {
            for (ChangelogEntry entry : getter.apply(release)) {
                items.add(entryItem(section, entry, release.version()));
            }
        }

        List<GuiItem[]> rows = new ArrayList<>();
        if (items.isEmpty()) return rows;

        rows.add(headerRow(section, items.size()));
        for (int i = 0; i < items.size(); i += COLUMNS) {
            List<GuiItem> chunk = items.subList(i, Math.min(i + COLUMNS, items.size()));
            GuiItem[] row = new GuiItem[COLUMNS];
            // Center partially filled rows
            int offset = (COLUMNS - chunk.size()) / 2;
            for (int j = 0; j < chunk.size(); j++) {
                row[offset + j] = chunk.get(j);
            }
            rows.add(row);
        }

        return rows;
    }

    /**
     * Lays out section rows across pages. A header is never left alone at the bottom of a page, and if a section
     * spills onto a new page, its header is repeated at the top of that page.
     */
    private static List<List<GuiItem[]>> paginate(List<List<GuiItem[]>> sections) {
        List<List<GuiItem[]>> pages = new ArrayList<>();
        List<GuiItem[]> current = new ArrayList<>();

        for (List<GuiItem[]> section : sections) {
            if (section.isEmpty()) continue;

            GuiItem[] header = section.getFirst();
            for (int i = 1; i < section.size(); i++) {
                boolean needsHeader = i == 1 || current.isEmpty();
                int needed = needsHeader ? 2 : 1;

                if (current.size() + needed > CONTENT_ROWS) {
                    pages.add(current);
                    current = new ArrayList<>();
                    needsHeader = true;
                }

                if (needsHeader) current.add(copyRow(header));
                current.add(section.get(i));
            }
        }

        if (!current.isEmpty()) pages.add(current);
        return pages;
    }

    private static GuiItem[] copyRow(GuiItem[] row) {
        GuiItem[] copy = new GuiItem[row.length];
        for (int i = 0; i < row.length; i++) {
            copy[i] = row[i] == null ? null : row[i].copy();
        }
        return copy;
    }

    private GuiItem[] headerRow(Section section, int count) {
        GuiItem[] row = new GuiItem[COLUMNS];
        for (int x = 0; x < COLUMNS; x++) {
            row[x] = filler(section.glass);
        }

        row[COLUMNS / 2] = item(section.icon,
            tr(section.langKey + ".title"),
            List.of(tr(section.langKey + ".count", Placeholder.unparsed("count", String.valueOf(count)))));
        return row;
    }

    private GuiItem entryItem(Section section, ChangelogEntry entry, String version) {
        List<Component> lore = new ArrayList<>();
        List<String> lines = entry.description().size() == 1 ? wrap(entry.description().getFirst()) : entry.description();
        for (String line : lines) {
            lore.add(tr(section.langKey + ".entry-description", Placeholder.unparsed("line", line)));
        }

        if (!lore.isEmpty()) lore.add(Component.empty());
        lore.add(tr(section.langKey + ".entry-version", Placeholder.unparsed("version", version)));

        return item(resolveMaterial(entry.material(), section.defaultMaterial),
            tr(section.langKey + ".entry-name", Placeholder.unparsed("name", entry.name())), lore);
    }

    private GuiItem honeyBottle(VersionManifest manifest, String currentVersion, List<ReleaseNotes> releases) {
        TagResolver placeholders = TagResolver.resolver(
            Placeholder.unparsed("version", currentVersion),
            Placeholder.unparsed("latest", String.valueOf(manifest.latestVersion())),
            Placeholder.unparsed("count", String.valueOf(releases.size())),
            Placeholder.unparsed("url", manifest.downloadUrl())
        );

        List<Component> lore = new ArrayList<>();
        lore.add(tr("changelog-gui.honey-bottle.installed-version", placeholders));
        lore.add(tr("changelog-gui.honey-bottle.latest-version", placeholders));
        lore.add(tr("changelog-gui.honey-bottle.versions-behind", placeholders));
        lore.add(Component.empty());
        lore.add(tr("changelog-gui.honey-bottle.click", placeholders));

        GuiItem bottle = item(Material.HONEY_BOTTLE, tr("changelog-gui.honey-bottle.name", placeholders), lore);
        bottle.setAction(event -> {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().sendMessage(tr("changelog-gui.download-link", placeholders)
                .clickEvent(ClickEvent.openUrl(manifest.downloadUrl()))
                .hoverEvent(HoverEvent.showText(tr("changelog-gui.download-link-hover", placeholders))));
        });
        return bottle;
    }

    private Component tr(String key, TagResolver... resolvers) {
        return commandFeedback.sendCommandFeedback(key, resolvers);
    }

    private static GuiItem filler(Material material) {
        return new GuiItem(namedItem(material, Component.empty()));
    }

    private static GuiItem item(Material material, Component name, List<Component> lore) {
        ItemStack stack = namedItem(material, name);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.lore(lore.stream().map(line -> line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList());
            stack.setItemMeta(meta);
        }
        return new GuiItem(stack);
    }

    private static ItemStack namedItem(Material material, Component name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(name.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static Material resolveMaterial(@Nullable String name, Material fallback) {
        if (name == null) return fallback;
        Material material = Material.matchMaterial(name);
        return material != null && !material.isAir() && material.asItemType() != null ? material : fallback;
    }

    private static List<String> wrap(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            if (!line.isEmpty() && line.length() + 1 + word.length() > WRAP_WIDTH) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (!line.isEmpty()) line.append(' ');
            line.append(word);
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }

    private enum Section {
        FEATURES("changelog-gui.features", Material.LIME_STAINED_GLASS_PANE, Material.NETHER_STAR, Material.PAPER),
        BUG_FIXES("changelog-gui.bug-fixes", Material.RED_STAINED_GLASS_PANE, Material.SPIDER_EYE, Material.SPIDER_EYE);

        private final String langKey;
        private final Material glass;
        private final Material icon;
        private final Material defaultMaterial;

        Section(String langKey, Material glass, Material icon, Material defaultMaterial) {
            this.langKey = langKey;
            this.glass = glass;
            this.icon = icon;
            this.defaultMaterial = defaultMaterial;
        }
    }
}
