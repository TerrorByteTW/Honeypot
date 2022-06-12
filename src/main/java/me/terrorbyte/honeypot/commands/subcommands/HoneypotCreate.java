package me.terrorbyte.honeypot.commands.subcommands;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.HoneypotConfigManager;
import me.terrorbyte.honeypot.commands.CommandFeedback;
import me.terrorbyte.honeypot.events.PlayerConversationListener;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import me.terrorbyte.honeypot.commands.HoneypotSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HoneypotCreate extends HoneypotSubCommand {

    @Override
    public String getName() {
        return "create";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(Player p, String[] args) {
        Block block;

        //If player doesn't have the create permission, don't let them do this
        if (!(p.hasPermission("honeypot.create"))) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        //Get block the player is looking at
        if(p.getTargetBlockExact(5) != null){
             block = p.getTargetBlockExact(5);
        } else {
            p.sendMessage(CommandFeedback.sendCommandFeedback("notlookingatblock"));
            return;
        }

        if(HoneypotConfigManager.getPluginConfig().getBoolean("filters.blocks") || HoneypotConfigManager.getPluginConfig().getBoolean("filters.inventories")) {
            List<String> allowedBlocks = (List<String>) HoneypotConfigManager.getPluginConfig().getList("allowed-blocks");
            List<String> allowedInventories = (List<String>) HoneypotConfigManager.getPluginConfig().getList("allowed-inventories");
            boolean allowed = false;

            if (HoneypotConfigManager.getPluginConfig().getBoolean("filters.blocks")){
                for (String blockType : allowedBlocks) {
                    assert block != null;
                    if (block.getType().name().equals(blockType)){
                        allowed = true;
                        break;
                    }
                }
            }

            if (HoneypotConfigManager.getPluginConfig().getBoolean("filters.inventories")){
                for (String blockType : allowedInventories) {
                    if (block.getType().name().equals(blockType)){
                        allowed = true;
                        break;
                    }
                }
            }

            if (!allowed){
                p.sendMessage(CommandFeedback.sendCommandFeedback("againstfilter"));
                return;
            }
        }

        //If the blocks meta has a honeypot tag, let them know
        if (HoneypotBlockStorageManager.isHoneypotBlock(block)) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("alreadyexists"));

            //If it does not have a honeypot tag or the honeypot tag does not equal 1, create one
        } else {
            if (args.length >= 2 && (args[1].equalsIgnoreCase("kick") ||
                    args[1].equalsIgnoreCase("ban") ||
                    args[1].equalsIgnoreCase("warn") ||
                    args[1].equalsIgnoreCase("notify") ||
                    args[1].equalsIgnoreCase("nothing") ||
                    args[1].equalsIgnoreCase("custom"))) 
            {
                switch (args[1].toLowerCase()){

                    case "custom" -> {
                        if (HoneypotConfigManager.getPluginConfig().getBoolean("enable-custom-actions")){
                            if (!p.hasPermission("honeypot.custom")){
                                p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
                            } else {
                                p.sendTitle(ChatColor.AQUA + "Enter action", "Enter your custom action command (WITHOUT THE /) in chat. Type cancel to exit", 10, 60, 10);
                                ConversationFactory cf = new ConversationFactory(Honeypot.getPlugin());
                                Conversation conv = cf.withFirstPrompt(new PlayerConversationListener(block)).withLocalEcho(false).withEscapeSequence("cancel").addConversationAbandonedListener(new PlayerConversationListener(block)).withTimeout(10).buildConversation(p);
                                conv.begin();
                            }
                        } else {
                            p.sendMessage(CommandFeedback.sendCommandFeedback("customactionsdisabled"));
                        }
                    }

                    default -> {
                        HoneypotBlockStorageManager.createBlock(block, args[1]);
                        p.sendMessage(CommandFeedback.sendCommandFeedback("success", true));
                    }
                }

            } else {
                p.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
            }
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        List<String> subcommands = new ArrayList<>();

        //We are already in argument 1 of the command, hence why this is a subcommand class. Argument 2 is the subcommand for the subcommand,
        //aka /honeypot create <THIS ONE>

        if(args.length == 2){
            //Return all action types for the /honeypot create command
            subcommands.add("warn");
            subcommands.add("kick");
            subcommands.add("ban");
            subcommands.add("notify");
            subcommands.add("nothing");
            if (HoneypotConfigManager.getPluginConfig().getBoolean("enable-custom-actions")) { subcommands.add("custom"); }
        }

        return subcommands;
    }
}
