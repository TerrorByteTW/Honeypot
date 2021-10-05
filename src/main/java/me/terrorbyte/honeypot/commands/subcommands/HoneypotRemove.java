package me.terrorbyte.honeypot.commands.subcommands;

import me.terrorbyte.honeypot.storagemanager.HoneypotFileManager;
import me.terrorbyte.honeypot.commands.CommandFeedback;
import me.terrorbyte.honeypot.commands.SubCommand;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public class HoneypotRemove extends SubCommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Removes a honeypot";
    }

    @Override
    public String getSyntax() {
        return "/honeypot remove";
    }

    @Override
    public void perform(Player p, String[] args) {

        //Get the block data for the block the player is looking at
        Block block = p.getTargetBlock(null, 5);

        //If it is a pot
        if(HoneypotFileManager.isHoneypotBlock(block)){
            HoneypotFileManager.deleteBlock(block);
            p.sendMessage(CommandFeedback.sendCommandFeedback("success", false));

            //If it is not a pot
        } else {
            p.sendMessage(CommandFeedback.sendCommandFeedback("notapot"));
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return null;
    }
}
