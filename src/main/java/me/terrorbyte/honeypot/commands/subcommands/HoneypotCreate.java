package me.terrorbyte.honeypot.commands.subcommands;

import me.terrorbyte.honeypot.commands.HoneypotCommandFeedback;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import me.terrorbyte.honeypot.commands.HoneypotSubCommand;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HoneypotCreate extends HoneypotSubCommand {
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

        //If player has create permission, let them do this
        if(p.hasPermission("honeypot.create") || p.hasPermission("honeypot.*") || p.isOp()) {
            //Get block the player is looking at
            Block block = p.getTargetBlock(null, 5);

            //If the blocks meta has a honeypot tag, let them know
            if (HoneypotBlockStorageManager.isHoneypotBlock(block)) {
                p.sendMessage(HoneypotCommandFeedback.sendCommandFeedback("alreadyexists"));

                //If it does not have a honeypot tag or the honeypot tag does not equal 1, create one
            } else {
                if(args.length >= 2 && (args[1].equalsIgnoreCase("kick") ||
                        args[1].equalsIgnoreCase("ban") ||
                        args[1].equalsIgnoreCase("warn") ||
                        args[1].equalsIgnoreCase("notify") ||
                        args[1].equalsIgnoreCase("nothing")))
                {
                    HoneypotBlockStorageManager.createBlock(block, args[1]);
                    p.sendMessage(HoneypotCommandFeedback.sendCommandFeedback("success", true));
                } else {
                    p.sendMessage(HoneypotCommandFeedback.sendCommandFeedback("usage"));
                }
            }
        } else {
            //If no permissions, let the player know
            p.sendMessage(HoneypotCommandFeedback.sendCommandFeedback("nopermission"));
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
            actions.add("notify");
            actions.add("nothing");
            return actions;
        }

        return null;
    }
}
