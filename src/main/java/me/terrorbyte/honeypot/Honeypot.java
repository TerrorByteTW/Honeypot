package me.terrorbyte.honeypot;

import me.terrorbyte.honeypot.commands.HoneypotCommandManager;
import me.terrorbyte.honeypot.events.HoneypotEntityChangeEventListener;
import me.terrorbyte.honeypot.events.HoneypotExplosionEventListener;
import me.terrorbyte.honeypot.events.HoneypotPlayerBreakEventListener;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import me.terrorbyte.honeypot.storagemanager.HoneypotPlayerStorageManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class Honeypot extends JavaPlugin {

    //On enable, register the block break event listener, register the command manager, and log to the console
    @Override
    public void onEnable() {
        plugin = this;

        getServer().getPluginManager().registerEvents(new HoneypotPlayerBreakEventListener(), this);
        getServer().getPluginManager().registerEvents(new HoneypotExplosionEventListener(), this);
        getServer().getPluginManager().registerEvents(new HoneypotEntityChangeEventListener(), this);
        getCommand("honeypot").setExecutor(new HoneypotCommandManager());
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Enabled " + ChatColor.GOLD + "Honeypot" + ChatColor.AQUA + " anti-cheat honeypot plugin");

        try {
            //For whatever reason, if we don't explicitly pass the plugin variable instead of letting HoneypotFileManager
            //use Honeypot.getPlugin(), it crashes the plugin. Idk, it worked fine in the YouTube tutorial I watched lol
            HoneypotBlockStorageManager.loadHoneypotBlocks(plugin);
            HoneypotPlayerStorageManager.loadHoneypotPlayers(plugin);
        } catch (IOException e) {
            e.printStackTrace();
        }

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        new HoneypotUpdateChecker(this, 96665).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getLogger().info(ChatColor.GREEN + "You are on the latest version of Honeypot!");
            } else {
                getLogger().info(ChatColor.RED + "There is a new update available: " + version + ". Please download for the latest features and security updates!");
            }
        });

    }

    //On disable, do nothing, really
    @Override
    public void onDisable() {
        // Save any unsaved HoneypotBlocks for whatever reason

        try {
            HoneypotBlockStorageManager.saveHoneypotBlocks();
            HoneypotPlayerStorageManager.saveHoneypotPlayers();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Disabled " + ChatColor.GOLD + "Honeypot" + ChatColor.AQUA + " anti-cheat honeypot plugin");
    }

    //Return the plugin instance
    public static Honeypot getPlugin() {
        return plugin;
    }

    //Static plugin variable, private to Honeypot to prevent changes
    private static Honeypot plugin;
}
