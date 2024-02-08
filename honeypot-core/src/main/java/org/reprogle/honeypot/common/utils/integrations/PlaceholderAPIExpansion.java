package org.reprogle.honeypot.common.utils.integrations;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

@SuppressWarnings({ "deprecation", "unused" })
public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    // Currently not needed, but here in case :)
    private final Honeypot plugin;

    public PlaceholderAPIExpansion(Honeypot plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", Honeypot.plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getIdentifier() {
        return "honeypot";
    }

    @Override
    public @NotNull String getVersion() {
        return Honeypot.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull
    String params) {
        Honeypot.getHoneypotLogger().debug("Param received was: " + params);
        if (params.equalsIgnoreCase("current_count_broken")) {
            if (player == null)
                return null;
            int count = HoneypotPlayerManager.getInstance().getCount(player);
            return count < 0 ? "0" : String.valueOf(count);
        }

        if (params.startsWith("current_count_broken_")) {
            String playerName = params.split("current_count_broken_")[1];
            OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
            int count = HoneypotPlayerManager.getInstance().getCount(p);
            return count < 0 ? "0" : String.valueOf(count);
        }

        if (params.equalsIgnoreCase("breaks_before_action")) {
            return String.valueOf(HoneypotConfigManager.getPluginConfig().getInt("blocks-broken-before-action-taken"));
        }

        return null;
    }

}
