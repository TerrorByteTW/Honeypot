package org.reprogle.honeypot.common.utils.integrations;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

import javax.annotation.Nullable;

@Singleton
public class AdapterManager {

    private final Honeypot plugin;
    private final HoneypotConfigManager configManager;
    private final CommandFeedback commandFeedback;

    /**
     * Private constructor to hide implicit one
     * @param plugin Pluign instance
     * @param configManager ConfigManager instance
     * @param commandFeedback CommandFeedback instance
     */
    @Inject
    public AdapterManager(Honeypot plugin, HoneypotConfigManager configManager, CommandFeedback commandFeedback) {
        this.configManager = configManager;
        this.plugin = plugin;
        this.commandFeedback = commandFeedback;
    }

    private WorldGuardAdapter wga = null;

    private GriefPreventionAdapter gpa = null;

    private LandsAdapter la = null;

    private PermissionAdapter pa = null;

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

        if (server.getPluginManager().getPlugin("Vault") != null) {
            pa = new PermissionAdapter(plugin);
        } else {
            plugin.getHoneypotLogger().info(commandFeedback.getChatPrefix()
                    .append(Component.text("Vault is not installed, some features won't work. Please download Vault here: https://www.spigotmc.org/resources/vault.34315/", NamedTextColor.RED)));
        }

        if (server.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			plugin.getHoneypotLogger().debug(Component.text("PlaceholderAPI is installed on this server, hooking into it"));
            plugin.getInjector().getInstance(PlaceholderAPIExpansion.class).register();
		}
    }

    /**
     * Retrieve the WorldGuard Adapter
     * @return WorldGuardAdapter
     */
    public WorldGuardAdapter getWorldGuardAdapter() {
        return wga;
    }

    /**
     * Retrieve the GriefPrevention Adapter
     * @return GriefPreventionAdapter
     */
    public GriefPreventionAdapter getGriefPreventionAdapter() {
        return gpa;
    }

    /**
     * Retrieve the GriefPrevention Adapter
     * @return LandsAdapter
     */
    public LandsAdapter getLandsAdapter() {
        return la;
    }

    /**
     * Retrieve the permission service provider object
     * @return {@link Permission}
     */
    public PermissionAdapter getPermissions() {
        return pa;
    }

}
