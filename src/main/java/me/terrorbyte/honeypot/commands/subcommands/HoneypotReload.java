package me.terrorbyte.honeypot.commands.subcommands;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.commands.HoneypotCommandFeedback;
import me.terrorbyte.honeypot.commands.HoneypotSubCommand;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public class HoneypotReload extends HoneypotSubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload's honeypot config";
    }

    @Override
    public String getSyntax() {
        return "/honeypot reload";
    }

    @Override
    public void perform(Player p, String[] args) {

        //Check if they have permission
        if(p.hasPermission("honeypot.reload") || p.hasPermission("honeypot.*") || p.isOp() ){

            p.sendMessage(HoneypotCommandFeedback.sendCommandFeedback("reload"));
            Honeypot.getPlugin().reloadConfig();

        } else {
            //If they don't have permission disregard the command and let them know
            p.sendMessage(HoneypotCommandFeedback.sendCommandFeedback("nopermission"));
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return null;
    }
}
