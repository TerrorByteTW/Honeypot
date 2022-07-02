package org.reprogle.honeypot.commands.subcommands;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.HoneypotConfigManager;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.commands.HoneypotSubCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HoneypotReload implements HoneypotSubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void perform(Player p, String[] args) {

        // Check if they have permission
        if (!(p.hasPermission("honeypot.reload"))) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        try {
            HoneypotConfigManager.getPluginConfig().reload();
            HoneypotConfigManager.getPluginConfig().save();

            HoneypotConfigManager.getGuiConfig().reload();
            HoneypotConfigManager.getGuiConfig().save();

            HoneypotConfigManager.getHoneypotsConfig().reload();
            HoneypotConfigManager.getHoneypotsConfig().save();

            p.sendMessage(CommandFeedback.sendCommandFeedback("reload"));
        }
        catch (IOException e) {
            // Nothing
        }
    }

    // We don't have any subcommands here, but we cannot return null otherwise the tab completer in the CommandManager
    // will throw an exception since CopyPartialMatches doesn't allow null values
    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return new ArrayList<>();
    }
}
