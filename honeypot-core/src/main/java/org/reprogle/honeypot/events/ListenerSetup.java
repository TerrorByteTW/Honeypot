package org.reprogle.honeypot.events;

import org.bukkit.plugin.Plugin;

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
    }

}
