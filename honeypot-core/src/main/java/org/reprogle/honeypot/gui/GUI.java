package org.reprogle.honeypot.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.honeypot.gui.button.GUIButton;
import org.reprogle.honeypot.gui.item.GUIItemBuilder;
import org.reprogle.honeypot.gui.menu.GUIMenuListener;
import org.reprogle.honeypot.gui.menu.GUIOpenMenu;
import org.reprogle.honeypot.gui.pagination.GUIPageButtonBuilder;
import org.reprogle.honeypot.utils.HoneypotConfigManager;

public class GUI {

    private final JavaPlugin plugin;

    private boolean blockDefaultInteractions = true;

    private boolean enableAutomaticPagination = true;

    private GUIPageButtonBuilder defaultPaginationButtonBuilder = (type, inventory) -> {
        switch (type) {
        case PREV_BUTTON:
            if (inventory.getCurrentPage() > 0)
                return new GUIButton(new GUIItemBuilder(
                        Material.getMaterial(HoneypotConfigManager.getGuiConfig().getString("previous-page-item")))
                                .name("&a&l\u2190 Previous Page")
                                .lore("&aClick to move back to", "&apage " + inventory.getCurrentPage() + ".").build())
                                        .withListener(event -> {
                                            event.setCancelled(true);
                                            inventory.previousPage(event.getWhoClicked());
                                        });
            else
                return null;

        case CURRENT_BUTTON:
            return new GUIButton(new GUIItemBuilder(
                    Material.getMaterial(HoneypotConfigManager.getGuiConfig().getString("current-page-item")))
                            .name("&7&lPage " + (inventory.getCurrentPage() + 1) + " of " + inventory.getMaxPage())
                            .lore("&7You are currently viewing", "&7page " + (inventory.getCurrentPage() + 1) + ".")
                            .build()).withListener(event -> event.setCancelled(true));

        case NEXT_BUTTON:
            if (inventory.getCurrentPage() < inventory.getMaxPage() - 1)
                return new GUIButton(new GUIItemBuilder(
                        Material.getMaterial(HoneypotConfigManager.getGuiConfig().getString("next-page-item")))
                                .name("&a&lNext Page \u2192")
                                .lore("&aClick to move forward to", "&apage " + (inventory.getCurrentPage() + 2) + ".")
                                .build()).withListener(event -> {
                                    event.setCancelled(true);
                                    inventory.nextPage(event.getWhoClicked());
                                });
            else
                return null;

        case UNASSIGNED:
        default:
            return null;
        }
    };

    public GUI(JavaPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(new GUIMenuListener(plugin, this), plugin);
    }

    public GUIMenu create(String name, int rows) {
        return create(name, rows, null);
    }

    public GUIMenu create(String name, int rows, String tag) {
        return new GUIMenu(plugin, this, name, rows, tag);
    }

    public void setBlockDefaultInteractions(boolean blockDefaultInteractions) {
        this.blockDefaultInteractions = blockDefaultInteractions;
    }

    public boolean areDefaultInteractionsBlocked() {
        return blockDefaultInteractions;
    }

    public void setEnableAutomaticPagination(boolean enableAutomaticPagination) {
        this.enableAutomaticPagination = enableAutomaticPagination;
    }

    public boolean isAutomaticPaginationEnabled() {
        return enableAutomaticPagination;
    }

    public void setDefaultPaginationButtonBuilder(GUIPageButtonBuilder defaultPaginationButtonBuilder) {
        this.defaultPaginationButtonBuilder = defaultPaginationButtonBuilder;
    }

    public GUIPageButtonBuilder getDefaultPaginationButtonBuilder() {
        return defaultPaginationButtonBuilder;
    }

    public List<GUIOpenMenu> findOpenWithTag(String tag) {

        List<GUIOpenMenu> foundInventories = new ArrayList<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Inventory topInventory = player.getOpenInventory().getTopInventory();

            if (topInventory.getHolder() != null) {
                GUIMenu inventory = (GUIMenu) topInventory.getHolder();
                if (topInventory.getHolder() instanceof GUIMenu && inventory.getTag().equals(tag)) {
                    foundInventories.add(new GUIOpenMenu(inventory, player));
                }
            }
        }

        return foundInventories;

    }

}
