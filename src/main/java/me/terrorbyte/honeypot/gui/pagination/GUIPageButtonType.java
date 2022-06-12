package me.terrorbyte.honeypot.gui.pagination;

public enum GUIPageButtonType {
	
	PREV_BUTTON(3),
    CURRENT_BUTTON(4),
    NEXT_BUTTON(5),
    UNASSIGNED(0);

    private final int SLOT;

    GUIPageButtonType(int slot) {
        this.SLOT = slot;
    }

    public int getSlot() {
        return SLOT;
    }

    public static GUIPageButtonType forSlot(int slot) {
        for (GUIPageButtonType buttonType : GUIPageButtonType.values()) {
            if (buttonType.SLOT == slot) return buttonType;
        }

        return GUIPageButtonType.UNASSIGNED;
    }

}
