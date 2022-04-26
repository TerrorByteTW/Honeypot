package me.terrorbyte.honeypot.events;

import org.bukkit.plugin.Plugin;

public class ListenerSetup {
    
    public static void SetupListeners(Plugin plugin){
        plugin.getServer().getPluginManager().registerEvents(new PlayerBreakEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ExplosionEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityChangeEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerContainerOpenListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PistonMoveListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
    }
    
}
