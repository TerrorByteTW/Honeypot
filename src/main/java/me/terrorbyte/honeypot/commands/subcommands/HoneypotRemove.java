package me.terrorbyte.honeypot.commands.subcommands;

import me.terrorbyte.honeypot.storagemanager.HoneypotManager;
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

        //Check if they have permission
        if(p.hasPermission("honeypot.remove") || p.hasPermission("honeypot.*") || p.isOp() ){
            //Get the block data for the block the player is looking at
            Block block = p.getTargetBlock(null, 5);

            //If it is a pot
            if(HoneypotManager.isHoneypotBlock(block)){
                HoneypotManager.deleteBlock(block);
                p.sendMessage(CommandFeedback.sendCommandFeedback("success", false));

                //If it is not a pot
            } else {
                p.sendMessage(CommandFeedback.sendCommandFeedback("notapot"));
            }
        } else {
            //If they don't have permission disregard the command and let them know
            p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return null;
    }
}
