package org.reprogle.honeypot.utils;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.flags.StateFlag;

/**
 * A class for accessing the Honeypot WorldGuard. This will allow retrieval of the Honeypot WorldGuard flag
 * as well as checking if actions are allowed, without having to integrate WorldGuard into your own plugin
 */
public abstract class WorldGuardUtil {

	/**
     * Sets up the hook for WorldGuard
	 * 
	 * @return The StateFlag that was setup, if necessary.
     */
    @SuppressWarnings("java:S2696")
    public abstract StateFlag setupWorldGuard();

	/**
	 * Returns the WorldGuard flag
	 * @return {@link StateFlag}
	 */
	public abstract StateFlag getWorldGuardFlag();

	/**
	 * Checks if WorldGuard support is enabled
	 * @return True if enabled, false if not
	 */
	public abstract boolean isEnabled();

	/**
	 * Check if the allow-honeypots flag is on
	 * 
	 * @param player The player initiating the action
	 * @return True if the action is allowed, false if the action isn't allowed OR if WorldGuard support isn't enabled.
	 * @see #isEnabled() {@link #isEnabled()}
	 */
	public abstract boolean isAllowed(Player player);
}
