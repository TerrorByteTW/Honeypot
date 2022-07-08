package org.reprogle.honeypot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Consumer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public record HoneypotUpdateChecker(Plugin plugin, String link) {

    /**
     * Grabs the version number from the link provided
     * 
     * @param consumer The consumer function
     */
    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            Honeypot.getHoneypotLogger().log("Checking for updates");
            try (InputStream inputStream = new URL(this.link).openStream();
                    Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            }
            catch (IOException exception) {
                plugin.getLogger().info("Unable to check for updates: " + exception.getMessage());
                Honeypot.getHoneypotLogger().log("Unable to check for updates" + exception.getMessage());
            }
        });
    }
}
