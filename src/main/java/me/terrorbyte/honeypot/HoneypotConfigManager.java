package me.terrorbyte.honeypot;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class HoneypotConfigManager extends JavaPlugin {

    Plugin plugin = Honeypot.getPlugin();

    public static YamlDocument setupConfig(YamlDocument config) throws IOException {

        config.update();
        config.save();

        return config;
    }
}
