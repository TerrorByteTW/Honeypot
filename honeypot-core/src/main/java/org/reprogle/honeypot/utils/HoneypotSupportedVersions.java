package org.reprogle.honeypot.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Consumer;
import org.reprogle.honeypot.Honeypot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public record HoneypotSupportedVersions(Plugin plugin, String version) {

    /**
     * Gets the supported server versions for the current version of the plugin
     *
     * @param consumer The consumer function
     */
    public void getSupportedVersions(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            Honeypot.getHoneypotLogger().log("Checking for updates");
            try (InputStream inputStream = new URL("https://raw.githubusercontent.com/TerrorByteTW/Honeypot/master/supported-versions/" + version).openStream();
                 Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            }
            catch (IOException exception) {
                plugin.getLogger().info("Unable to check supported versions: " + exception.getMessage());
                Honeypot.getHoneypotLogger().log("Unable to check supported versions: " + exception.getMessage());
            }
        });
    }
}
