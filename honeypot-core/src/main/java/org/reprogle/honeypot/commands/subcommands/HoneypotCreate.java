package org.reprogle.honeypot.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.HoneypotConfigManager;
import org.reprogle.honeypot.api.events.HoneypotCreateEvent;
import org.reprogle.honeypot.api.events.HoneypotPreCreateEvent;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.commands.HoneypotSubCommand;
import org.reprogle.honeypot.events.PlayerConversationListener;
import org.reprogle.honeypot.storagemanager.HoneypotBlockStorageManager;

import java.util.ArrayList;
import java.util.List;

public class HoneypotCreate implements HoneypotSubCommand {

    @Override
    public String getName() {
        return "create";
    }

    @Override
    @SuppressWarnings({"unchecked", "java:S3776", "java:S1192"})
    public void perform(Player p, String[] args) {
        Block block;

        // If player doesn't have the create permission, don't let them do this
        if (!(p.hasPermission("honeypot.create"))) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        // Get block the player is looking at
        if (p.getTargetBlockExact(5) != null) {
            block = p.getTargetBlockExact(5);
        }
        else {
            p.sendMessage(CommandFeedback.sendCommandFeedback("notlookingatblock"));
            return;
        }

        if (HoneypotConfigManager.getPluginConfig().getBoolean("filters.blocks")
                || HoneypotConfigManager.getPluginConfig().getBoolean("filters.inventories")) {
            List<String> allowedBlocks = (List<String>) HoneypotConfigManager.getPluginConfig()
                    .getList("allowed-blocks");
            List<String> allowedInventories = (List<String>) HoneypotConfigManager.getPluginConfig()
                    .getList("allowed-inventories");
            boolean allowed = false;

            if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("filters.blocks"))) {
                for (String blockType : allowedBlocks) {
                    assert block != null;
                    if (block.getType().name().equals(blockType)) {
                        allowed = true;
                        break;
                    }
                }
            }

            if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("filters.inventories"))) {
                for (String blockType : allowedInventories) {
                    if (block.getType().name().equals(blockType)) {
                        allowed = true;
                        break;
                    }
                }
            }

            if (!allowed) {
                p.sendMessage(CommandFeedback.sendCommandFeedback("againstfilter"));
                return;
            }
        }

        // If the blocks meta has a honeypot tag, let them know
        if (Boolean.TRUE.equals(HoneypotBlockStorageManager.isHoneypotBlock(block))) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("alreadyexists"));

            // If it does not have a honeypot tag or the honeypot tag does not equal 1, create one
        }
        else {
            if (args.length >= 2 && (args[1].equalsIgnoreCase("kick") || args[1].equalsIgnoreCase("ban")
                    || args[1].equalsIgnoreCase("warn") || args[1].equalsIgnoreCase("notify")
                    || args[1].equalsIgnoreCase("nothing") || args[1].equalsIgnoreCase("custom"))) {

                // Fire HoneypotPreNonPlayerBreakEvent
                HoneypotPreCreateEvent hpce = new HoneypotPreCreateEvent(p, block);
                Bukkit.getPluginManager().callEvent(hpce);

                // Don't do anything if the event is cancelled
                if (hpce.isCancelled())
                    return;

                if (args[1].equalsIgnoreCase("custom")) {
                    if (Boolean.TRUE
                            .equals(HoneypotConfigManager.getPluginConfig().getBoolean("enable-custom-actions"))) {
                        if (!p.hasPermission("honeypot.custom")) {
                            p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
                        }
                        else {
                            p.sendTitle(ChatColor.AQUA + "Enter action",
                                    "Enter your custom action command (WITHOUT THE /) in chat. Type cancel to exit", 10,
                                    60, 10);
                            ConversationFactory cf = new ConversationFactory(Honeypot.getPlugin());
                            Conversation conv = cf.withFirstPrompt(new PlayerConversationListener(block))
                                    .withLocalEcho(false).withEscapeSequence("cancel")
                                    .addConversationAbandonedListener(new PlayerConversationListener(block))
                                    .withTimeout(10).buildConversation(p);
                            conv.begin();
                        }
                    }
                    else {
                        p.sendMessage(CommandFeedback.sendCommandFeedback("customactionsdisabled"));
                    }
                }
                else {
                    HoneypotBlockStorageManager.createBlock(block, args[1]);
                    p.sendMessage(CommandFeedback.sendCommandFeedback("success", true));
                }

                // Fire HoneypotPreNonPlayerBreakEvent
                HoneypotCreateEvent hce = new HoneypotCreateEvent(p, block);
                Bukkit.getPluginManager().callEvent(hce);

            }
            else {
                p.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
            }
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        List<String> subcommands = new ArrayList<>();

        // We are already in argument 1 of the command, hence why this is a subcommand class. Argument 2 is the
        // subcommand for the subcommand,
        // aka /honeypot create <THIS ONE>

        if (args.length == 2) {
            // Return all action types for the /honeypot create command
            subcommands.add("warn");
            subcommands.add("kick");
            subcommands.add("ban");
            subcommands.add("notify");
            subcommands.add("nothing");
            if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("enable-custom-actions"))) {
                subcommands.add("custom");
            }
        }

        return subcommands;
    }
}
