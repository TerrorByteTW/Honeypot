package me.terrorbyte.honeypot.commands.subcommands;

import me.terrorbyte.honeypot.commands.CommandFeedback;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import me.terrorbyte.honeypot.commands.HoneypotSubCommand;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HoneypotRemove extends HoneypotSubCommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public void perform(Player p, String[] args) {

        //Check if they have permission
        if(!(p.hasPermission("honeypot.remove"))) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        //Get the block data for the block the player is looking at
        Block block = p.getTargetBlockExact(5);

        //If it is a pot
        assert block != null;
        if (HoneypotBlockStorageManager.isHoneypotBlock(block)) {
            HoneypotBlockStorageManager.deleteBlock(block);
            p.sendMessage(CommandFeedback.sendCommandFeedback("success", false));

            //If it is not a pot
        } else {
            p.sendMessage(CommandFeedback.sendCommandFeedback("notapot"));
        }
    }

    //We don't have any subcommands here, but we cannot return null otherwise the tab completer in the CommandManager will throw an exception since CopyPartialMatches doesn't allow null values
    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return new ArrayList<>();
    }
}
