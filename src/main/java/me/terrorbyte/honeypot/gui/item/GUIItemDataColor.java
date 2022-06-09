package me.terrorbyte.honeypot.gui.item;

public enum GUIItemDataColor {

	WHITE((short) 0),
    ORANGE((short) 1),
    MAGENTA((short) 2),
    LIGHT_BLUE((short) 3),
    YELLOW((short) 4),
    LIME((short) 5),
    PINK((short) 6),
    GRAY((short) 7),
    LIGHT_GRAY((short) 8),
    CYAN((short) 9),
    PURPLE((short) 10),
    BLUE((short) 11),
    BROWN((short) 12),
    GREEN((short) 13),
    RED((short) 14),
    BLACK((short) 15);

    /**
     * The durability value of the color.
     */
    private final short value;

    GUIItemDataColor(short value) {
        this.value = value;
    }

    /**
     * Returns the durability value that the named color represents.
     *
     * @return The durability value as a 'short'.
     */
    public short getValue() {
        return value;
    }

    /**
     * Returns an {@link ItemDataColor} as found by its damage value or
     * null if there isn't one.
     *
     * @param value The corresponding damage value of the color.
     * @return The {@link ItemDataColor} associated with <code>value</code> or null if there isn't one.
     */
    public static GUIItemDataColor getByValue(short value) {
        for (GUIItemDataColor color : GUIItemDataColor.values()) {
            if (value == color.value) return color;
        }

        return null;
    }

}
