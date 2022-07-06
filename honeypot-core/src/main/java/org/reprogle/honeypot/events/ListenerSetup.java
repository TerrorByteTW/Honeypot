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
        plugin.getServer().getPluginManager().registerEvents(new PlayerBreakEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ExplosionEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityChangeEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerBlockInteractListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PistonMoveListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockPhysicsEventListener(), plugin);
    }

}
