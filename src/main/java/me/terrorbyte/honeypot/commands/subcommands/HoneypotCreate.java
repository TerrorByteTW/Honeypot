package me.terrorbyte.honeypot.commands.subcommands;

import me.terrorbyte.honeypot.storagemanager.HoneypotFileManager;
import me.terrorbyte.honeypot.commands.CommandFeedback;
import me.terrorbyte.honeypot.commands.SubCommand;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HoneypotCreate extends SubCommand {
    //Abstract methods. Return name, description, and syntax

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Create a honeypot";
    }

    @Override
    public String getSyntax() {
        return "/honeypot create <action> <reason>";
    }

    @Override
    public void perform(Player p, String[] args) {

        //Get block the player is looking at
        Block block = p.getTargetBlock(null, 5);
        
        //If the blocks meta has a honeypot tag, let them know
        if (HoneypotFileManager.isHoneypotBlock(block)) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("alreadyexists"));

            //If it does not have a honeypot tag or the honeypot tag does not equal 1, create one
        } else {
            HoneypotFileManager.createBlock(block, "kick");
            p.sendMessage(CommandFeedback.sendCommandFeedback("success", true));
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {

        //We are already in argument 1 of the command, hence why this is a subcommand class. Argument 2 is the subcommand for the subcommand,
        //aka /honeypot create <THIS ONE>

        if(args.length == 2){
            //Return all action types for the /honeypot create command
            List<String> actions = new ArrayList<>();
            actions.add("warn");
            actions.add("kick");
            actions.add("ban");
            return actions;
        }

        return null;
    }
}
