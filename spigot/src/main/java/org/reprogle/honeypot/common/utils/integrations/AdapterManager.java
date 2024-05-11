package org.reprogle.honeypot.common.utils.integrations;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Server;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

@Singleton
public class AdapterManager {

    private final Honeypot plugin;
    private final HoneypotConfigManager configManager;

    /**
     * Private constructor to hide implicit one
     */
    @Inject
    public AdapterManager(Honeypot plugin, HoneypotConfigManager configManager) {
        this.configManager = configManager;
        this.plugin = plugin;
    }

    private WorldGuardAdapter wga = null;

    private GriefPreventionAdapter gpa = null;

    private LandsAdapter la = null;

    public void onLoadAdapters(Server server) {
        if (server.getPluginManager().getPlugin("WorldGuard") != null) {
            wga = new WorldGuardAdapter();
        }
    }

    public void onEnableAdapters(Server server) {
        if (server.getPluginManager().getPlugin("GriefPrevention") != null)
            gpa = new GriefPreventionAdapter(configManager);

        if (server.getPluginManager().getPlugin("Lands") != null) {
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
