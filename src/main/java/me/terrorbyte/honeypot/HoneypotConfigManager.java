package me.terrorbyte.honeypot;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class HoneypotConfigManager extends JavaPlugin {

    Plugin plugin = Honeypot.getPlugin();

    public static YamlDocument setupConfig(YamlDocument config) throws IOException {

        config.update();
        config.save();

        return config;
    }
}
