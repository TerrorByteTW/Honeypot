package me.terrorbyte.honeypot.commands.subcommands;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.commands.CommandFeedback;
import me.terrorbyte.honeypot.commands.HoneypotSubCommand;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import me.terrorbyte.honeypot.storagemanager.HoneypotPlayerStorageManager;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

public class HoneypotReload extends HoneypotSubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void perform(Player p, String[] args) {

        //Check if they have permission
        if(!(p.hasPermission("honeypot.reload"))){
            p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        p.sendMessage(CommandFeedback.sendCommandFeedback("reload"));
        Honeypot.getPlugin().reloadConfig();
        try {
            HoneypotBlockStorageManager.loadHoneypotBlocks(Honeypot.getPlugin());
            HoneypotPlayerStorageManager.loadHoneypotPlayers(Honeypot.getPlugin());
        } catch (IOException e) {
            //Nothing
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return null;
    }
}
