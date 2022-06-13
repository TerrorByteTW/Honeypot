package org.reprogle.honeypot.gui.pagination;

public enum GUIPageButtonType {

    PREV_BUTTON(3), 
    CURRENT_BUTTON(4), 
    NEXT_BUTTON(5), 
    UNASSIGNED(0);

    private final int slot;

    GUIPageButtonType(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public static GUIPageButtonType forSlot(int slot) {
        for (GUIPageButtonType buttonType : GUIPageButtonType.values()) {
            if (buttonType.slot == slot)
                return buttonType;
        }

        return GUIPageButtonType.UNASSIGNED;
    }

}
