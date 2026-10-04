package org.reprogle.honeypot.api.events;

public enum TriggerType {
    /**
     * Triggers involving the player breaking a block
     */
    BREAK,
    /**
     * Triggers involving the player opening an inventory. Only applicable if the server has `use-inventory-click` set to false
     */
    INVENTORY_OPEN,
    /**
     * Triggers involving the player interacting with an inventory. Only applicable if the server has `use-inventory-click` set to true
     */
    INVENTORY_INTERACT,
    /**
     * Triggers involving the player performing an action that is not covered by the other trigger types, such as placing TNT
     */
    GENERIC,
    /**
     * Triggers involving non-player entities, such as enderman or pistons. Does not actually track in the database, and is only used for events
     */
    NON_PLAYER
}
