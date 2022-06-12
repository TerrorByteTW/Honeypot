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
	private final JavaPlugin OWNER;
    private final GUI GUI_MANAGER;

	public GUIMenuListener(JavaPlugin owner, GUI guiManager) {
        this.OWNER = owner;
        this.GUI_MANAGER = guiManager;
    }

	@EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (event.getInventory().getHolder() != null
            && event.getInventory().getHolder() instanceof GUIMenu) {

            GUIMenu clickedGui = (GUIMenu) event.getInventory().getHolder();

            if (!clickedGui.getOwner().equals(OWNER)) return;

            if (clickedGui.areDefaultInteractionsBlocked() != null) {
                event.setCancelled(clickedGui.areDefaultInteractionsBlocked());
            } else {
                if (GUI_MANAGER.areDefaultInteractionsBlocked())
                    event.setCancelled(true);
            }

            if (event.getSlot() > clickedGui.getPageSize()) {
                int offset = event.getSlot() - clickedGui.getPageSize();
                GUIPageButtonBuilder paginationButtonBuilder = GUI_MANAGER.getDefaultPaginationButtonBuilder();

                if (clickedGui.getPaginationButtonBuilder() != null) {
                    paginationButtonBuilder = clickedGui.getPaginationButtonBuilder();
                }

                GUIPageButtonType buttonType = GUIPageButtonType.forSlot(offset);
                GUIButton paginationButton = paginationButtonBuilder.buildPaginationButton(buttonType, clickedGui);
                if (paginationButton != null) paginationButton.getListener().onClick(event);
                return;
            }
            
            if (clickedGui.isStickiedSlot(event.getSlot())) {
                GUIButton button = clickedGui.getButton(0, event.getSlot());
                if (button != null && button.getListener() != null) button.getListener().onClick(event);
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

        if (event.getInventory().getHolder() != null
                && event.getInventory().getHolder() instanceof GUIMenu) {

            GUIMenu clickedGui = (GUIMenu) event.getInventory().getHolder();
            if (!clickedGui.getOwner().equals(OWNER)) return;

            if (clickedGui.getOnClose() != null)
                clickedGui.getOnClose().accept(clickedGui);

        }

    }


}
