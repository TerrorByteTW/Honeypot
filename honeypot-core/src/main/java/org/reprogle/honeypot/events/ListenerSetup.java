package org.reprogle.honeypot.events;

import org.bukkit.plugin.Plugin;
import org.reprogle.honeypot.HoneypotConfigManager;

public class ListenerSetup {

    /**
     * Create package listener to hide implicit one
     */
    ListenerSetup(){

    }

    /**
     * Set's up all the listeners in the entire plugin
     * @param plugin The Honeypot plugin instance
     */
    public static void setupListeners(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new BlockBreakEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockFromToEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockBurnEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityChangeBlockEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityExplodeEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PistonExtendRetractListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerInteractEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new StructureGrowEventListener(), plugin);

        // We only want to enable this event if enabled in config
        // In order to enable this event after the config has been changed, a server restart is required
        if(Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("allow-physics-checks"))) {
            plugin.getServer().getPluginManager().registerEvents(new BlockPhysicsEventListener(), plugin);
        }
    }

}
