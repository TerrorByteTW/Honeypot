package org.reprogle.honeypot.utils;

import org.bukkit.entity.Player;

/**
 * A utility class for better managing permissions across the plugin, instead of
 * having to write spaghetti-logic
 */
public class PermissionManager {

    // Private constructor to hide implicit one
    private PermissionManager() {

    }

    /**
     * Checks the permission level of the player executing the action/command
     * 
     * @param p The {@link Player} to check
     * @return An int representing the permission level. 0 means no permissions, 99
     *         means Op
     */
    public static int checkPermissions(Player p) {

        if (p.hasPermission("honeypot.*") || p.isOp())
            return 99;

        return 0;
    }
}
