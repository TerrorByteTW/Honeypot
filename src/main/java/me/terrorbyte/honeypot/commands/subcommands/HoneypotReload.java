package me.terrorbyte.honeypot.commands.subcommands;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.commands.CommandFeedback;
import me.terrorbyte.honeypot.commands.HoneypotSubCommand;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
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
        try {
            Honeypot.config.reload();
            Honeypot.config.save();
        } catch (IOException e) {
            //Nothing
        }
    }

    //We don't have any subcommands here, but we cannot return null otherwise the tab completer in the CommandManager will throw an exception since CopyPartialMatches doesn't allow null values
    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return new ArrayList<>();
    }
}
