package org.reprogle.honeypot.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.honeypot.gui.button.GUIButton;
import org.reprogle.honeypot.gui.pagination.GUIPageButtonBuilder;
import org.reprogle.honeypot.gui.pagination.GUIPageButtonType;

import net.md_5.bungee.api.ChatColor;

public class GUIMenu implements InventoryHolder {
    private final JavaPlugin owner;

    private final GUI guiManager;

    private String name;

    private String tag;

    private int rowsPerPage;

    private final Map<Integer, GUIButton> items;

    private final HashSet<Integer> stickiedSlots;

    private int currentPage;

    private Boolean blockDefaultInteractions;

    private Boolean enableAutomaticPagination;

    private GUIPageButtonBuilder paginationButtonBuilder;

    private Consumer<GUIMenu> onClose;

    private Consumer<GUIMenu> onPageChange;

    GUIMenu(JavaPlugin owner, GUI guiManager, String name, int rowsPerPage, String tag) {
        this.owner = owner;
        this.guiManager = guiManager;
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.rowsPerPage = rowsPerPage;
        this.tag = tag;

        this.items = new HashMap<>();
        this.stickiedSlots = new HashSet<>();

        this.currentPage = 0;
    }

    public void setBlockDefaultInteractions(boolean blockDefaultInteractions) {
        this.blockDefaultInteractions = blockDefaultInteractions;
    }

    public Boolean areDefaultInteractionsBlocked() {
        return blockDefaultInteractions;
    }

    public void setAutomaticPaginationEnabled(boolean enableAutomaticPagination) {
        this.enableAutomaticPagination = enableAutomaticPagination;
    }

    public Boolean isAutomaticPaginationEnabled() {
        return enableAutomaticPagination;
    }

    public void setPaginationButtonBuilder(GUIPageButtonBuilder paginationButtonBuilder) {
        this.paginationButtonBuilder = paginationButtonBuilder;
    }

    public GUIPageButtonBuilder getPaginationButtonBuilder() {
        return this.paginationButtonBuilder;
    }

    public JavaPlugin getOwner() {
        return owner;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public int getPageSize() {
        return rowsPerPage * 9;
    }

    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setName(String name) {
        this.name = ChatColor.translateAlternateColorCodes('&', name);
    }

    public void setRawName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addButton(GUIButton button) {
        if (getHighestFilledSlot() == 0 && getButton(0) == null) {
            setButton(0, button);
            return;
        }

        setButton(getHighestFilledSlot() + 1, button);
    }

    public void addButtons(GUIButton... buttons) {
        for (GUIButton button : buttons)
            addButton(button);
    }

    public void setButton(int slot, GUIButton button) {
        items.put(slot, button);
    }

    public void setButton(int page, int slot, GUIButton button) {
        if (slot < 0 || slot > getPageSize())
            return;

        setButton((page * getPageSize()) + slot, button);
    }

    public void removeButton(int slot) {
        items.remove(slot);
    }

    public void removeButton(int page, int slot) {
        if (slot < 0 || slot > getPageSize())
            return;

        removeButton((page * getPageSize()) + slot);
    }

    public GUIButton getButton(int slot) {
        if (slot < 0 || slot > getHighestFilledSlot())
            return null;

        return items.get(slot);
    }

    public GUIButton getButton(int page, int slot) {
        if (slot < 0 || slot > getPageSize())
            return null;

        return getButton((page * getPageSize()) + slot);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
        if (this.onPageChange != null)
            this.onPageChange.accept(this);
    }

    public int getMaxPage() {
        return (int) Math.ceil(((double) getHighestFilledSlot() + 1) / (getPageSize()));
    }

    public int getHighestFilledSlot() {
        int slot = 0;

        for (Map.Entry<Integer, GUIButton> entry : items.entrySet()) {
            int nextSlot = entry.getKey();
            if (items.get(nextSlot) != null && nextSlot > slot)
                slot = nextSlot;
        }

        return slot;
    }

    public boolean nextPage(HumanEntity viewer) {
        if (currentPage < getMaxPage() - 1) {
            currentPage++;
            refreshInventory(viewer);
            if (this.onPageChange != null)
                this.onPageChange.accept(this);
            return true;
        }
        else {
            return false;
        }
    }

    public boolean previousPage(HumanEntity viewer) {
        if (currentPage > 0) {
            currentPage--;
            refreshInventory(viewer);
            if (this.onPageChange != null)
                this.onPageChange.accept(this);
            return true;
        }
        else {
            return false;
        }
    }

    public void stickSlot(int slot) {
        if (slot < 0 || slot >= getPageSize())
            return;

        this.stickiedSlots.add(slot);
    }

    public void unstickSlot(int slot) {
        this.stickiedSlots.remove(Integer.valueOf(slot));
    }

    public void clearStickiedSlots() {
        this.stickiedSlots.clear();
    }

    public boolean isStickiedSlot(int slot) {
        if (slot < 0 || slot >= getPageSize())
            return false;

        return this.stickiedSlots.contains(slot);
    }

    public void clearAllButStickiedSlots() {
        this.currentPage = 0;
        items.entrySet().removeIf(item -> !isStickiedSlot(item.getKey()));
    }

    public Consumer<GUIMenu> getOnClose() {
        return this.onClose;
    }

    public void setOnClose(Consumer<GUIMenu> onClose) {
        this.onClose = onClose;
    }

    public Consumer<GUIMenu> getOnPageChange() {
        return this.onPageChange;
    }

    public void setOnPageChange(Consumer<GUIMenu> onPageChange) {
        this.onPageChange = onPageChange;
    }

    public void refreshInventory(HumanEntity viewer) {
        if (!(viewer.getOpenInventory().getTopInventory().getHolder() instanceof GUIMenu)
                || viewer.getOpenInventory().getTopInventory().getHolder() != this)
            return;

        if (viewer.getOpenInventory().getTopInventory().getSize() != getPageSize() + (getMaxPage() > 0 ? 9 : 0)) {
            viewer.openInventory(getInventory());
            return;
        }

        String newName = name.replace("{currentPage}", String.valueOf(currentPage + 1)).replace("{maxPage}",
                String.valueOf(getMaxPage()));
        if (!viewer.getOpenInventory().getTitle().equals(newName)) {
            viewer.openInventory(getInventory());
            return;
        }

        viewer.getOpenInventory().getTopInventory().setContents(getInventory().getContents());
    }

    @Override
    public Inventory getInventory() {
        boolean isAutomaticPaginationEnabled = guiManager.isAutomaticPaginationEnabled();
        if (isAutomaticPaginationEnabled() != null) {
            isAutomaticPaginationEnabled = isAutomaticPaginationEnabled();
        }

        boolean needsPagination = getMaxPage() > 0 && isAutomaticPaginationEnabled;

        Inventory inventory = Bukkit.createInventory(this, ((needsPagination)
                // Pagination enabled: add the bottom toolbar row.
                ? getPageSize() + 9
                // Pagination not required or disabled.
                : getPageSize()),
                name.replace("{currentPage}", String.valueOf(currentPage + 1)).replace("{maxPage}",
                        String.valueOf(getMaxPage())));

        // Add the main inventory items.
        for (int key = currentPage * getPageSize(); key < (currentPage + 1) * getPageSize(); key++) {
            // If we've already reached the maximum assigned slot, stop assigning
            // slots.
            if (key > getHighestFilledSlot())
                break;

            if (items.containsKey(key)) {
                inventory.setItem(key - (currentPage * getPageSize()), items.get(key).getIcon());
            }
        }

        // Update the stickied slots.
        for (int stickiedSlot : stickiedSlots) {
            inventory.setItem(stickiedSlot, items.get(stickiedSlot).getIcon());
        }

        checkIfPaginationNeeded(needsPagination, inventory);

        return inventory;
    }

    public void checkIfPaginationNeeded(boolean needsPagination, Inventory inventory){
        // Render the pagination items.
        if (needsPagination) {
            paginationButtonBuilder = guiManager.getDefaultPaginationButtonBuilder();
            if (getPaginationButtonBuilder() != null) {
                paginationButtonBuilder = getPaginationButtonBuilder();
            }

            int pageSize = getPageSize();
            for (int i = pageSize; i < pageSize + 9; i++) {
                int offset = i - pageSize;

                GUIButton paginationButton = paginationButtonBuilder
                        .buildPaginationButton(GUIPageButtonType.forSlot(offset), this);
                inventory.setItem(i, paginationButton != null ? paginationButton.getIcon() : null);
            }
        }
    }

}
