package org.reprogle.honeypot.common.utils.integrations;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.HoneypotPlayerManager;

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
        if (params.equalsIgnoreCase("current_count_broken")) {
            Honeypot.getHoneypotLogger().debug("Returning placeholder for player " + player.getName());
            return String.valueOf(HoneypotPlayerManager.getInstance().getCount(player));
        }

        return null;
    }

}
