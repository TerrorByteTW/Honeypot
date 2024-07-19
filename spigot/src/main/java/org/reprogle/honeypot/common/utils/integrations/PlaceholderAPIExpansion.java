package org.reprogle.honeypot.common.utils.integrations;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

@SuppressWarnings({ "deprecation", "unused" })
public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final Honeypot plugin;
    private final HoneypotPlayerManager playerManager;
    private final HoneypotConfigManager configManager;
    private final HoneypotLogger logger;

    @Inject
    public PlaceholderAPIExpansion(Honeypot plugin, HoneypotLogger logger, HoneypotPlayerManager playerManager, HoneypotConfigManager configManager) {
        this.plugin = plugin;
        this.logger = logger;
        this.playerManager = playerManager;
        this.configManager = configManager;
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getIdentifier() {
        return "honeypot";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull
    String params) {
        logger.debug("Param received was: " + params);
        if (params.equalsIgnoreCase("current_count_broken")) {
            if (player == null)
                return null;
            int count = playerManager.getCount(player);
            return count < 0 ? "0" : String.valueOf(count);
        }

        if (params.startsWith("current_count_broken_")) {
            String playerName = params.split("current_count_broken_")[1];
            OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
            int count = playerManager.getCount(p);
            return count < 0 ? "0" : String.valueOf(count);
        }

        if (params.equalsIgnoreCase("breaks_before_action")) {
            return String.valueOf(configManager.getPluginConfig().getInt("blocks-broken-before-action-taken"));
        }

        return null;
    }

}
