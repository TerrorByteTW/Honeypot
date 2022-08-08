package org.reprogle.honeypot.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.commands.HoneypotSubCommand;

public class HoneypotInfo implements HoneypotSubCommand {

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public void perform(Player p, String[] args) throws IOException {
        p.sendMessage(ChatColor.GOLD + "\n" + 
        " _____                         _\n"
      + "|  |  |___ ___ ___ _ _ ___ ___| |_\n" 
      + "|     | . |   | -_| | | . | . |  _|    by" + ChatColor.RED + " TerrorByte\n" + ChatColor.GOLD + 
        "|__|__|___|_|_|___|_  |  _|___|_|      version " + ChatColor.RED + Honeypot.getPlugin().getDescription().getVersion() + "\n" + ChatColor.GOLD + 
        "                  |___|_|");
        p.sendMessage(CommandFeedback.getChatPrefix() + "Honeypot running on Spigot version " + Bukkit.getVersion());
        if (!Honeypot.versionCheck()) {
            p.sendMessage(CommandFeedback.getChatPrefix() + "This version of Honeypot is not guaranteed to work on this version of Spigot. Some newer blocks (If any) may exhibit unusual behavior!");
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return new ArrayList<>();
    }
    
}
