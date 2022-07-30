package org.reprogle.honeypot.utils;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;

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
	private static StateFlag honeypotFlag;
	private static HoneypotLogger logger = Honeypot.getHoneypotLogger();
	private static boolean enabled = true;

	//Create private constructor to hide implicit one
	private WorldGuardUtil() {

	}

	/**
     * Sets up the hook for WorldGuard
     */
    @SuppressWarnings("java:S2696")
    public static StateFlag setupWorldGuard() {
		Honeypot.getPlugin().getLogger().info("WorldGuard found, hooking...");
		logger.log("WorldGuard is loaded on server, registering Honeypot WorldGuard flags");

		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
		try {
			StateFlag flag = new StateFlag("allow-honeypots", true);
			registry.register(flag);
			
			Honeypot.getPlugin().getLogger().info("Successfully registered flags");
			logger.log("Successfully registered WorldGuard flags");

			honeypotFlag = flag;
			return honeypotFlag;
		} catch (FlagConflictException e) {
			Honeypot.getPlugin().getLogger().info("Error registering flag, trying to resolve...");
			logger.log("A flag with the name 'allow-honeypots' already exists. Trying to use that one...");
			
			Flag<?> existing = registry.get("allow-honeypots");
			if (existing instanceof StateFlag) {
				Honeypot.getPlugin().getLogger().info("A flag already exists with the name that Honeypot was trying to register. Honeypot will use that flag, but this may cause unexpected results!");
				logger.log("A flag already exists with the name that Honeypot was trying to register. Honeypot will use that flag, but this may cause unexpected results!");

				honeypotFlag = (StateFlag) existing;
				return honeypotFlag;
			} else {
				Honeypot.getPlugin().getLogger().info("Honeypot was unable to register its WorldGuard flags. WorldGuard support is not enabled");
				logger.log("Honeypot was unable to register its WorldGuard flags. WorldGuard support is being disabled");
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
	public static StateFlag getWorldGuardFlag() {
        return honeypotFlag;
    }

	/**
	 * Checks if WorldGuard support is enabled
	 * @return True if enabled, false if not
	 */
	public static boolean isEnabled() {
		return enabled;
	}

	/**
	 * Check if the allow-honeypots flag is on
	 * 
	 * @param player The player initiating the action
	 * @return True if the action is allowed, false if the action isn't allowed OR if WorldGuard support isn't enabled.
	 * @see #isEnabled() {@link #isEnabled()}
	 */
	public static boolean isAllowed(Player player) {
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
