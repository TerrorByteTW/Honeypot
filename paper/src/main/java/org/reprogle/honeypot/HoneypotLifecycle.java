package org.reprogle.honeypot;

import com.google.inject.Inject;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.boot.lifecycle.PluginLifecycle;
import org.reprogle.bytelib.config.BoostedYamlPluginConfig;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.subcommands.Create;
import org.reprogle.honeypot.common.events.Listeners;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.storageproviders.PlayerHistoryStore;
import org.reprogle.honeypot.common.storageproviders.PlayerStore;
import org.reprogle.honeypot.common.storageproviders.RegionStore;
import org.reprogle.honeypot.common.store.sqlite.HoneypotMigrations;
import org.reprogle.honeypot.common.utils.*;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class HoneypotLifecycle implements PluginLifecycle {
    private final JavaPlugin plugin;
    @Getter
    private final AdapterManager adapterManager;
    private final Listeners listeners;
    private final HoneypotLogger logger;
    private final GhostHoneypotMonitor ghf;
    private final CommandFeedback commandFeedback;
    private final Set<BehaviorProvider> behaviorProviders;
    private final Set<RegionStore> regionStores;
    private final Set<PlayerStore> playerStores;
    private final Set<PlayerHistoryStore> playerHistoryStores;
    private final BytePluginConfig config;
    private final HoneypotSupportedVersions supportedVersions;


    @Inject
    public HoneypotLifecycle(
        JavaPlugin plugin,
        AdapterManager adapterManager,
        Listeners listeners,
        HoneypotLogger logger,
        GhostHoneypotMonitor ghf,
        CommandFeedback commandFeedback,
        Set<BehaviorProvider> behaviorProviders,
        Set<RegionStore> regionStores,
        Set<PlayerStore> playerStores,
        Set<PlayerHistoryStore> playerHistoryStores,
        HoneypotMigrations migrations,
        BytePluginConfig config,
        HoneypotSupportedVersions supportedVersions
    ) {
        this.plugin = plugin;
        this.adapterManager = adapterManager;
        this.listeners = listeners;
        this.logger = logger;
        this.ghf = ghf;
        this.commandFeedback = commandFeedback;
        this.behaviorProviders = behaviorProviders;
        this.regionStores = regionStores;
        this.playerStores = playerStores;
        this.playerHistoryStores = playerHistoryStores;
        this.config = config;
        this.supportedVersions = supportedVersions;

        migrations.migrate();
    }

    @Override
    @SuppressWarnings("java:S2696")
    public void onLoad() {
        // Register adapters which must be registered on load
        adapterManager.onLoadAdapters(plugin.getServer());

        try {
            Path dir = plugin.getDataFolder().toPath().resolve("behaviors");
            if (Files.notExists(dir)) Files.createDirectories(dir);
        } catch (IOException e) {
            logger.severe(Component.text("Could not create the behaviors folder! Honeypot will function without it, but custom behaviors may not load properly."));
        }

        // Register all internal behavior providers
        for (BehaviorProvider behavior : behaviorProviders) {
            Registry.getBehaviorRegistry().register(behavior);
        }

        // Register all internal region storage providers
        // Currently only exists 1, but others may be implemented in the future
        for (RegionStore store : regionStores) {
            Registry.getStorageManagerRegistry().register(store);
        }

        // Register all internal player storage providers
        // Currently only exists 1, but others may be implemented in the future
        for (PlayerStore store : playerStores) {
            Registry.getStorageManagerRegistry().register(store);
        }

        // Register all internal player history storage providers
        // Currently only exists 1, but others may be implemented in the future
        for (PlayerHistoryStore store : playerHistoryStores) {
            Registry.getStorageManagerRegistry().register(store);
        }
    }

    /**
     * onEnable() method called by Paper. This is a little messy due to all the setup
     * it has to do
     */
    @lombok.SneakyThrows
    @Override
    public void onEnable() {

        config.register("honeypots", BoostedYamlPluginConfig.YamlSpec.of(plugin.getDataFolder().toPath().resolve("honeypots.yml"), "honeypots.yml", null, false));
        config.register("gui", BoostedYamlPluginConfig.YamlSpec.of(plugin.getDataFolder().toPath().resolve("gui.yml"), "gui.yml", "file-version"));
        registerBehaviorConfigs();
        config.reload();

        // Lock the registries and start the Ghost Honeypot Monitor task
        Registry.getBehaviorRegistry().setInitialized(true);
        Registry.getStorageManagerRegistry().setInitialized(true);
        ghf.startTask();

        String regionStoreName = config.config().getString("storage-method.regions");
        Optional<RegionStore> regStore = Registry.getStorageManagerRegistry().get(regionStoreName, RegionStore.class);
        String playerStoreName = config.config().getString("storage-method.players");
        Optional<PlayerStore> playerStore = Registry.getStorageManagerRegistry().get(playerStoreName, PlayerStore.class);
        String playerHistoryStoreName = config.config().getString("storage-method.player-history");
        Optional<PlayerHistoryStore> playerHistoryStore = Registry.getStorageManagerRegistry().get(playerHistoryStoreName, PlayerHistoryStore.class);

        if (regStore.isPresent()) {
            try {
                Registry.setRegionStore(regStore.get());
            } catch (Exception e) {
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                logger.severe(Component.text("THE PLUGIN WAS PURPOSELY SHUT DOWN, THIS IS NOT A BUG. The Region Store that is set in config is not properly defined, please report this to the developer of the plugin that created the storage provider!"));
                throw new ConfigurationException(config.lang().getString("invalid-storage-provider").replace("%s", regionStoreName));
            }
        } else {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            logger.severe(Component.text("THE PLUGIN WAS PURPOSELY SHUT DOWN, THIS IS NOT A BUG. The Region Store that is set in config could not be found, check with the developer of the provider!"));
            throw new ConfigurationException(config.lang().getString("invalid-storage-provider").replace("%s", regionStoreName));
        }

        if (playerStore.isPresent()) {
            try {
                Registry.setPlayerStore(playerStore.get());
            } catch (Exception e) {
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                logger.severe(Component.text("THE PLUGIN WAS PURPOSELY SHUT DOWN, THIS IS NOT A BUG. The Player Store that is set in config is not properly defined, please report this to the developer of the plugin that created the storage provider!"));
                throw new ConfigurationException(config.lang().getString("invalid-storage-provider").replace("%s", playerStoreName));
            }
        } else {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            logger.severe(Component.text("THE PLUGIN WAS PURPOSELY SHUT DOWN, THIS IS NOT A BUG. The Player Store that is set in config could not be found, check with the developer of the provider!"));
            throw new ConfigurationException(config.lang().getString("invalid-storage-provider").replace("%s", playerStoreName));
        }

        if (playerHistoryStore.isPresent()) {
            try {
                Registry.setPlayerHistoryStore(playerHistoryStore.get());
            } catch (Exception e) {
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                logger.severe(Component.text("THE PLUGIN WAS PURPOSELY SHUT DOWN, THIS IS NOT A BUG. The Player History Store that is set in config is not properly defined, please report this to the developer of the plugin that created the storage provider!"));
                throw new ConfigurationException(config.lang().getString("invalid-storage-provider").replace("%s", playerHistoryStoreName));
            }
        } else {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            logger.severe(Component.text("THE PLUGIN WAS PURPOSELY SHUT DOWN, THIS IS NOT A BUG. The Player History Store that is set in config could not be found, check with the developer of the provider!"));
            throw new ConfigurationException(config.lang().getString("invalid-storage-provider").replace("%s", playerHistoryStoreName));
        }

        logger.info(Component.text("Successfully registered " + Registry.getBehaviorRegistry().size()
            + " behavior providers. Further registrations are now locked."));

        logger.info(Component.text(Registry.getStorageManagerRegistry().size()
            + " storage providers have been registered. Further registrations are now locked, but the provider can be changed at any time by doing /honeypot reload."));

        logger.info(Component.text("Configured Region Store: " + regionStoreName));
        logger.info(Component.text("Configured Player Store: " + playerStoreName));
        logger.info(Component.text("Configured Player History Store: " + playerHistoryStoreName));

        // Start bstats and register event listeners
        new Metrics(plugin, 15425);
        listeners.setupListeners();

        // Register remaining adapters that can be registered on enable
        adapterManager.onEnableAdapters(plugin.getServer());

        plugin.getServer().getConsoleSender().sendMessage(commandFeedback.buildSplash(plugin));

        if (isFolia()) {
            logger.warning(
                Component.text("Welcome to Folia! It is assumed you know what you're doing, since Folia is not yet standard. While Honeypot can run on Folia, it is not yet officially endorsed by the developer, and is also not actively tested. Be wary when using it for now, and report any bugs in Honeypot caused by Folia to the developer!"));
        }

        // Check the supported MC versions against the MC versions supported by this version of Honeypot
        // That's a mouthful, isn't it?
        supportedVersions.checkIfServerSupported();

        // Check for any updates
        new HoneypotUpdateChecker(plugin, "https://raw.githubusercontent.com/TerrorByteTW/Honeypot/master/version.txt")
            .getVersion(latest -> {
                if (Integer.parseInt(latest.replace(".", "")) > Integer
                    .parseInt(plugin.getPluginMeta().getVersion().replace(".", ""))) {
                    plugin.getServer().getConsoleSender()
                        .sendMessage(commandFeedback.getChatPrefix().append(Component.text("There is a new update available: " + latest + ". Download for the latest features and performance improvements!", NamedTextColor.RED)));
                } else {
                    plugin.getServer().getConsoleSender().sendMessage(commandFeedback.getChatPrefix().append(Component.text(" You are on the latest version of Honeypot!", NamedTextColor.GREEN)));
                }
            }, logger);
    }

    /**
     * Disable method called by Bukkit
     */
    @Override
    public void onDisable() {
        logger.info(Component.text("Stopping the ghost checker task"));
        ghf.cancelTask();
        logger.info(Component.text("Cancelling any in-progress region creations"));
        Create.playersCreatingRegions.forEach((_, creating) ->
            Arrays.stream(creating.player.getInventory().getContents())
                .filter(item -> item != null && item.getPersistentDataContainer().has(new NamespacedKey(plugin, "region_wand")))
                .forEach(item -> creating.player.getInventory().remove(item)));
        Create.playersCreatingRegions.clear();
        logger.info(Component.text("Successfully shutdown Honeypot. Bye for now!"));
    }

    /*
     * All the functions below are getter functions
     *
     * These simply return objects to prevent static keyword abuse
     */


    private boolean isFolia() {
        return Bukkit.getServer().getName().startsWith("Folia");
    }

    private void registerBehaviorConfigs() {
        for (BehaviorProvider behavior : Registry.getBehaviorRegistry().getBehaviorProviders().values()) {
            if (!behavior.isConfigurable()) {
                continue;
            }

            config.register(
                behavior.getProviderName(),
                // By providing our own YamlSpec instead of using `of()` or `externalOnly()`, we can mark it as an internal YAML file without it being required
                // This allows us to write our own behavior provider configs to disk without requiring 3rd party ones to exist
                new BoostedYamlPluginConfig.YamlSpec(
                    plugin.getDataFolder().toPath().resolve("behaviors/" + behavior.getProviderName() + ".yml"),
                    "behaviors/" + behavior.getProviderName() + ".yml",
                    null,
                    false,
                    false
                ));
        }
    }
}
