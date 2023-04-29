package org.reprogle.honeypot.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
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

/**
 * A small utility class for helping connect with WorldGuard
 */
public class WorldGuardUtil {
	private StateFlag honeypotFlag;

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

		return honeypotFlag;
	}

	/**
	 * Returns the WorldGuard flag
	 * 
	 * @return {@link StateFlag}
	 */
	public StateFlag getWorldGuardFlag() {
		return honeypotFlag;
	}

	/**
	 * Check if the allow-honeypots flag is on
	 * 
	 * @param player   The player initiating the action
	 * @param location The location of the block being placed (It may be different
	 *                 from the player location)
	 * @return True if the action is allowed, false if the action isn't allowed OR
	 *         if WorldGuard support isn't enabled.
	 */
	public boolean isAllowed(Player player, Location location) {
		LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));

		return set.testState(localPlayer, honeypotFlag);
	}

}
