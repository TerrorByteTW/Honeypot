package me.terrorbyte.honeypot.gui.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.terrorbyte.honeypot.gui.GUI;
import me.terrorbyte.honeypot.gui.GUIMenu;
import me.terrorbyte.honeypot.gui.button.GUIButton;
import me.terrorbyte.honeypot.gui.pagination.GUIPageButtonBuilder;
import me.terrorbyte.honeypot.gui.pagination.GUIPageButtonType;

public class GUIMenuListener implements Listener {
    private final JavaPlugin owner;

    private final GUI guiManager;

    public GUIMenuListener(JavaPlugin owner, GUI guiManager) {
        this.owner = owner;
        this.guiManager = guiManager;
    }

    // Supressed warnings here because I don't have the brainpower currently to rework this method to be compliant with cognitive complexity rules
    @EventHandler
    @SuppressWarnings("java:S3776")
    public void onInventoryClick(InventoryClickEvent event) {

        if (event.getInventory().getHolder() instanceof GUIMenu) {

            GUIMenu clickedGui = (GUIMenu) event.getInventory().getHolder();

            if (!clickedGui.getOwner().equals(owner))
                return;

            if (clickedGui.areDefaultInteractionsBlocked() != null) {
                event.setCancelled(clickedGui.areDefaultInteractionsBlocked());
            }
            else {
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
