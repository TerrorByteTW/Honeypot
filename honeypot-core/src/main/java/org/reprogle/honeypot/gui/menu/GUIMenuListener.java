/*
 * Honeypot is a tool for griefing auto-moderation
 * Copyright TerrorByte (c) 2022-2023
 * Copyright Honeypot Contributors (c) 2022-2023
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

package org.reprogle.honeypot.gui.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.honeypot.gui.GUI;
import org.reprogle.honeypot.gui.GUIMenu;
import org.reprogle.honeypot.gui.button.GUIButton;
import org.reprogle.honeypot.gui.pagination.GUIPageButtonBuilder;
import org.reprogle.honeypot.gui.pagination.GUIPageButtonType;

public class GUIMenuListener implements Listener {
    private final JavaPlugin owner;

    private final GUI guiManager;

    public GUIMenuListener(JavaPlugin owner, GUI guiManager) {
        this.owner = owner;
        this.guiManager = guiManager;
    }

    // Supressed warnings here because I don't have the brainpower currently to
    // rework this method to be compliant with cognitive complexity rules
    @EventHandler
    @SuppressWarnings("java:S3776")
    public void onInventoryClick(InventoryClickEvent event) {

        if (event.getClickedInventory() != null
                && event.getClickedInventory().getHolder() != null
                && event.getInventory().getHolder() instanceof GUIMenu) {

            GUIMenu clickedGui = (GUIMenu) event.getClickedInventory().getHolder();

            if (!clickedGui.getOwner().equals(owner))
                return;

            if (clickedGui.areDefaultInteractionsBlocked() != null) {
                event.setCancelled(clickedGui.areDefaultInteractionsBlocked());
            } else {
                if (guiManager.areDefaultInteractionsBlocked())
                    event.setCancelled(true);
            }

            if (event.getSlot() > clickedGui.getPageSize()) {
                int offset = event.getSlot() - clickedGui.getPageSize();
                GUIPageButtonBuilder paginationButtonBuilder = guiManager.getDefaultPaginationButtonBuilder();

                if (clickedGui.getPaginationButtonBuilder() != null) {
                    paginationButtonBuilder = clickedGui.getPaginationButtonBuilder();
                }

                GUIPageButtonType buttonType = GUIPageButtonType.forSlot(offset);
                GUIButton paginationButton = paginationButtonBuilder.buildPaginationButton(buttonType, clickedGui);
                if (paginationButton != null)
                    paginationButton.getListener().onClick(event);
                return;
            }

            if (clickedGui.isStickiedSlot(event.getSlot())) {
                GUIButton button = clickedGui.getButton(0, event.getSlot());
                if (button != null && button.getListener() != null)
                    button.getListener().onClick(event);
                return;
            }

            GUIButton button = clickedGui.getButton(clickedGui.getCurrentPage(), event.getSlot());
            if (button != null && button.getListener() != null) {
                button.getListener().onClick(event);
            }

        }

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (event.getInventory().getHolder() instanceof GUIMenu) {

            GUIMenu clickedGui = (GUIMenu) event.getInventory().getHolder();

            if (!clickedGui.getOwner().equals(owner))
                return;

            if (clickedGui.getOnClose() != null)
                clickedGui.getOnClose().accept(clickedGui);

        }

    }

}
