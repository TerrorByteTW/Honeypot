package org.reprogle.honeypot.common.utils.integrations;

import org.bukkit.Server;

public class AdapterManager {

    private static WorldGuardAdapter wga = null;

    private static GriefPreventionAdapter gpa = null;

    private static LandsAdapter la = null;

    public static void onLoadAdapters(Server server) {
        if (server.getPluginManager().getPlugin("WorldGuard") != null) {
            wga = new WorldGuardAdapter();
        }
    }

    public static void onEnableAdapters(Server server) {
        if (server.getPluginManager().getPlugin("GriefPrevention") != null)
            gpa = new GriefPreventionAdapter();

        if (server.getPluginManager().getPlugin("Lands") != null) {
            la = new LandsAdapter();
        }
    }

    /**
     * Retrieve the WorldGuard Adapter
     */
    public static WorldGuardAdapter getWorldGuardAdapter() {
        return wga;
    }

    /**
     * Retrieve the GriefPrevention Adapter
     */
    public static GriefPreventionAdapter getGriefPreventionAdapter() {
        return gpa;
    }

    /**
     * Retrieve the GriefPrevention Adapter
     */
    public static LandsAdapter getLandsAdapter() {
        return la;
    }

}
