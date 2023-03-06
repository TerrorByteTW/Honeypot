package org.reprogle.honeypot.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

/**
 * A small helper class for utilizing GriefPrevention
 */
public class GriefPreventionUtil {

	/**
	 * Check if the player has permission
	 * 
	 * @param player   The player placing the block
	 * @param location The location of the block being placed (It may be different
	 *                 than the players location)
	 * @return True if allowed, false if not
	 */
	public boolean isAllowed(Player player, Location location) {
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);

		if (claim != null) {
			if (Boolean.FALSE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("respect-griefprevention")))
				return false;

			return claim.checkPermission(player, ClaimPermission.Build, null) == null;
		}

		return true;
	}
}
