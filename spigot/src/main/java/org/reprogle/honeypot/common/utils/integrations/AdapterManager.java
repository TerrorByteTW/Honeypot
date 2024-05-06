package org.reprogle.honeypot.common.utils.integrations;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Server;
import org.reprogle.honeypot.Honeypot;

@Singleton
public class AdapterManager {

    private final Honeypot plugin;

    /**
     * Private constructor to hide implicit one
     */
    @Inject
    public AdapterManager(Honeypot plugin) {
        this.plugin = plugin;
    }

    private static WorldGuardAdapter wga = null;

    private static GriefPreventionAdapter gpa = null;

    private static LandsAdapter la = null;

    public void onLoadAdapters(Server server) {
        if (server.getPluginManager().getPlugin("WorldGuard") != null) {
            wga = new WorldGuardAdapter();
        }
    }

    public void onEnableAdapters(Server server) {
        if (server.getPluginManager().getPlugin("GriefPrevention") != null)
            gpa = new GriefPreventionAdapter();

        if (server.getPluginManager().getPlugin("Lands") != null) {
            server.getLogger().info(plugin.toString());
            la = new LandsAdapter(plugin);
        }
    }

    /**
     * Retrieve the WorldGuard Adapter
     */
    public WorldGuardAdapter getWorldGuardAdapter() {
        return wga;
    }

    /**
     * Retrieve the GriefPrevention Adapter
     */
    public GriefPreventionAdapter getGriefPreventionAdapter() {
        return gpa;
    }

    /**
     * Retrieve the GriefPrevention Adapter
     */
    public LandsAdapter getLandsAdapter() {
        return la;
    }

}
