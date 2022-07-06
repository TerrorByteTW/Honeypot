package org.reprogle.honeypot.commands.subcommands;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.HoneypotConfigManager;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.commands.HoneypotSubCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HoneypotRemove implements HoneypotSubCommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    @SuppressWarnings("java:S3776")
    public void perform(Player p, String[] args) throws IOException {

        if (!p.hasPermission("honeypot.remove") && !p.hasPermission("honeypot.removecommand")) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        Block block = p.getTargetBlockExact(5);

        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
            case "all" -> {
                Honeypot.getHBM().deleteAllHoneypotBlocks();
                p.sendMessage(CommandFeedback.sendCommandFeedback("deletedall"));
            }

            case "near" -> {
                final double radius = HoneypotConfigManager.getPluginConfig().getDouble("search-range");
                final double xCoord = p.getLocation().getX();
                final double yCoord = p.getLocation().getY();
                final double zCoord = p.getLocation().getZ();

                // For every x value within radius
                for (double x = xCoord - radius; x < xCoord + radius; x++) {
                    // For every y value within radius
                    for (double y = yCoord - radius; y < yCoord + radius; y++) {
                        // For every z value within radius
                        for (double z = zCoord - radius; z < zCoord + radius; z++) {

                            // Check the block at coords x,y,z to see if it's a Honeypot
                            final Block b = new Location(p.getWorld(), x, y, z).getBlock();

                            // If it is a honeypot do this
                            if (Boolean.TRUE.equals(Honeypot.getHBM().isHoneypotBlock(b))) {
                                Honeypot.getHBM().deleteBlock(b);

                            }
                        }
                    }
                }

                p.sendMessage(CommandFeedback.sendCommandFeedback("deletednear"));
            }

            default -> {
                potRemovalCheck(block, p);
            }
            }
        }
        else {
            potRemovalCheck(block, p);
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        List<String> subcommands = new ArrayList<>();

        if (args.length == 2) {
            // Return all action types for the /honeypot create command
            subcommands.add("all");
            subcommands.add("near");
        }

        return subcommands;
    }

    private void potRemovalCheck(Block block, Player p) {
        assert block != null;
        if (Boolean.TRUE.equals(Honeypot.getHBM().isHoneypotBlock(block))) {
            Honeypot.getHBM().deleteBlock(block);
            p.sendMessage(CommandFeedback.sendCommandFeedback("success", false));
        }
        else {
            p.sendMessage(CommandFeedback.sendCommandFeedback("notapot"));
        }
    }
}
