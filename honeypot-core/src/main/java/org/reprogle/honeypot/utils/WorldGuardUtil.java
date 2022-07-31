package org.reprogle.honeypot.utils;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

public class WorldGuardUtil {
	private StateFlag honeypotFlag;
	private boolean enabled = true;

	/**
     * Sets up the hook for WorldGuard
     */
    @SuppressWarnings("java:S2696")
    public StateFlag setupWorldGuard() {
		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

		try {
			StateFlag flag = new StateFlag("allow-honeypots", true);
			registry.register(flag);

			honeypotFlag = flag;
			return honeypotFlag;
		} catch (FlagConflictException e) {

			Flag<?> existing = registry.get("allow-honeypots");
			if (existing instanceof StateFlag) {
				honeypotFlag = (StateFlag) existing;
				return honeypotFlag;
			}
		}

		enabled = false;
		honeypotFlag = null;
        return honeypotFlag;
    }

	/**
	 * Returns the WorldGuard flag
	 * @return {@link StateFlag}
	 */
	public StateFlag getWorldGuardFlag() {
        return honeypotFlag;
    }

	/**
	 * Checks if WorldGuard support is enabled
	 * @return True if enabled, false if not
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Check if the allow-honeypots flag is on
	 * 
	 * @param player The player initiating the action
	 * @return True if the action is allowed, false if the action isn't allowed OR if WorldGuard support isn't enabled.
	 * @see #isEnabled() {@link #isEnabled()}
	 */
	public boolean isAllowed(Player player) {
		LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
		Location loc = localPlayer.getLocation();
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(loc);

		if (!isEnabled()) {
			return false;
		}

		return set.testState(localPlayer, honeypotFlag);
	}

}
