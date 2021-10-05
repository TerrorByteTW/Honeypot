package me.terrorbyte.honeypot;

import me.terrorbyte.honeypot.commands.CommandManager;
import me.terrorbyte.honeypot.events.HoneypotBreakEventListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class Honeypot extends JavaPlugin {

    //On enable, register the block break event listener, register the command manager, and log to the console
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new HoneypotBreakEventListener(), this);
        getCommand("honeypot").setExecutor(new CommandManager());
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Enabled TSS anti-cheat honeypot plugin!");

        plugin = this;
    }

    //On disable, do nothing, really
    @Override
    public void onDisable() {
        // Plugin shutdown logic

        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Disabled TSS anti-cheat honeypot plugin!");
    }

    //Return the plugin instance
    public static Honeypot getPlugin() {
        return plugin;
    }

    //Static plugin variable, private to Honeypot to prevent changes
    private static Honeypot plugin;
}
