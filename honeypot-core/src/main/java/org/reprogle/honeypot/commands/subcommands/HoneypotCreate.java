package org.reprogle.honeypot.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotCreateEvent;
import org.reprogle.honeypot.api.events.HoneypotPreCreateEvent;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.commands.HoneypotSubCommand;
import org.reprogle.honeypot.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.utils.GriefPreventionUtil;
import org.reprogle.honeypot.utils.HoneypotConfigManager;
import org.reprogle.honeypot.utils.HoneypotPermission;
import org.reprogle.honeypot.utils.WorldGuardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HoneypotCreate implements HoneypotSubCommand {

    @Override
    public String getName() {
        return "create";
    }

    @Override
    @SuppressWarnings({ "unchecked", "java:S3776", "java:S1192" })
    public void perform(Player p, String[] args) {
        Block block;
        WorldGuardUtil wgu = Honeypot.getWorldGuardUtil();
        GriefPreventionUtil gpu = Honeypot.getGriefPreventionUtil();

        // Get block the player is looking at
        if (p.getTargetBlockExact(5) != null) {
            block = p.getTargetBlockExact(5);
        } else {
            p.sendMessage(CommandFeedback.sendCommandFeedback("notlookingatblock"));
            return;
        }

        // Check if in a WorldGuard region and the flag is set to deny. If it is, don't
        // bother continuing
        if (wgu != null && !wgu.isAllowed(p, block.getLocation())) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("worldguard"));
            return;
        }

        // Check if in a GriefPrevention region
        if (gpu != null && !gpu.isAllowed(p, block.getLocation())) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("griefprevention"));
            return;
        }

        // Check if the filter is enabled, and if so, if it's allowed
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

        // If the block already exists in the DB
        if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(block))) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("alreadyexists"));

            // If the block doesn't exist
        } else {
            if (args.length >= 2 && (args[1].equalsIgnoreCase("kick") || args[1].equalsIgnoreCase("ban")
                    || args[1].equalsIgnoreCase("warn") || args[1].equalsIgnoreCase("notify")
                    || args[1].equalsIgnoreCase("nothing") || args[1].equalsIgnoreCase("custom"))) {

                // Fire HoneypotPreCreateEvent
                HoneypotPreCreateEvent hpce = new HoneypotPreCreateEvent(p, block);
                Bukkit.getPluginManager().callEvent(hpce);

                // Don't do anything if the event is cancelled
                if (hpce.isCancelled())
                    return;

                if (args[1].equalsIgnoreCase("custom")) {
                    if (!args[2].isEmpty() && HoneypotConfigManager.getHoneypotsConfig().contains(args[2])) {
                        HoneypotBlockManager.getInstance().createBlock(block, args[2]);
                        p.sendMessage(CommandFeedback.sendCommandFeedback("success", true));
                    } else {
                        p.sendMessage(CommandFeedback.sendCommandFeedback("noexist"));
                    }
                } else {
                    HoneypotBlockManager.getInstance().createBlock(block, args[1]);
                    p.sendMessage(CommandFeedback.sendCommandFeedback("success", true));
                }

                // Fire HoneypotCreateEvent
                HoneypotCreateEvent hce = new HoneypotCreateEvent(p, block);
                Bukkit.getPluginManager().callEvent(hce);

            } else {
                p.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
            }
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        List<String> subcommands = new ArrayList<>();

        // We are already in argument 1 of the command, hence why this is a subcommand
        // class. Argument 2 is the
        // subcommand for the subcommand,
        // aka /honeypot create <THIS ONE>

        if (args.length == 2) {
            // Return all action types for the /honeypot create command
            subcommands.add("warn");
            subcommands.add("kick");
            subcommands.add("ban");
            subcommands.add("notify");
            subcommands.add("nothing");
            subcommands.add("custom");
            // If the argument length is 3, return all the root keys for the subcommands
        } else if (args.length == 3 && args[1].equalsIgnoreCase("custom")) {
            Set<Object> keys = HoneypotConfigManager.getHoneypotsConfig().getKeys();
            for (Object key : keys) {
                subcommands.add(key.toString());
            }
        }

        return subcommands;
    }

    @Override
    public List<HoneypotPermission> getRequiredPermissions() {
        List<HoneypotPermission> permissions = new ArrayList<>();
        permissions.add(new HoneypotPermission("honeypot.create"));
        return permissions;
    }
}
