package org.reprogle.honeypot.events;

import java.util.List;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.api.events.HoneypotPlayerBreakEvent;
import org.reprogle.honeypot.api.events.HoneypotPrePlayerBreakEvent;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.storagemanager.HoneypotPlayerHistoryManager;
import org.reprogle.honeypot.storagemanager.HoneypotPlayerManager;
import org.reprogle.honeypot.utils.HoneypotConfigManager;
import org.reprogle.honeypot.utils.PhysicsUtil;

import dev.dejvokep.boostedyaml.YamlDocument;

public class BlockBreakEventListener implements Listener {

    private static final String REMOVE_PERMISSION = "honeypot.break";

    private static final String WILDCARD_PERMISSION = "honeypot.*";

    private static final String EXEMPT_PERMISSION = "honeypot.exempt";

    /**
     * Create a private constructor to hide the implicit one
     */
    BlockBreakEventListener() {

    }

    // Player block break event
    @EventHandler(priority = EventPriority.LOW)
    @SuppressWarnings({ "java:S3776", "java:S1192" })
    public static void blockBreakEvent(BlockBreakEvent event) {
        if (Boolean.TRUE.equals(HoneypotBlockManager.getInstance().isHoneypotBlock(event.getBlock()))) {
            // Fire HoneypotPrePlayerBreakEvent
            HoneypotPrePlayerBreakEvent hppbe = new HoneypotPrePlayerBreakEvent(event.getPlayer(), event.getBlock());
            Bukkit.getPluginManager().callEvent(hppbe);

            // Check if the event was cancelled. If it is, delete the block.
            if (hppbe.isCancelled()) {
                HoneypotBlockManager.getInstance().deleteBlock(event.getBlock());
                return;
            }

            // Create a boolean for if we should remove the block from the DB or not
            boolean deleteBlock = false;

            // If Allow Player Destruction is true, the player has permissions or is Op,
            // flag the block for deletion
            // from the DB. Otherwise, set the BlockBreakEvent to cancelled
            if (Boolean.TRUE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("allow-player-destruction"))
                    || (event.getPlayer().hasPermission(REMOVE_PERMISSION)
                            || event.getPlayer().hasPermission(WILDCARD_PERMISSION) || event.getPlayer().isOp())) {
                deleteBlock = true;
            } else {
                event.setCancelled(true);
            }

            // If blocks broken before action is less than or equal to 1, or allow
            // destruction is enabled, just go to
            // the break action. Otherwise, count it
            if (HoneypotConfigManager.getPluginConfig().getInt("blocks-broken-before-action-taken") <= 1 || Boolean.TRUE
                    .equals(HoneypotConfigManager.getPluginConfig().getBoolean("allow-player-destruction"))) {
                breakAction(event);
            } else {
                // If the player is not exempt, not op, and does not have remove perms, count
                // the break. Otherwise, just
                // activate the break action.
                if (!event.getPlayer().hasPermission(EXEMPT_PERMISSION) && !event.getPlayer().isOp()
                        && !event.getPlayer().hasPermission(REMOVE_PERMISSION)) {
                    countBreak(event);
                } else {
                    breakAction(event);
                }
            }

            // Fire HoneypotPlayerBreakEvent
            HoneypotPlayerBreakEvent hpbe = new HoneypotPlayerBreakEvent(event.getPlayer(), event.getBlock());
            Bukkit.getPluginManager().callEvent(hpbe);

            // If we flagged the block for deletion, remove it from the DB. Do this after
            // other actions have been
            // completed, otherwise the other actions will fail with NPEs
            if (deleteBlock) {
                HoneypotBlockManager.getInstance().deleteBlock(event.getBlock());
            }
        }
    }

    // This is a separate event from the one above. We want to know if any Honeypots
    // were broken due to breaking a
    // "root" block, such as torches breaking due to
    // the block they're on being broken
    @EventHandler(priority = EventPriority.LOW)
    @SuppressWarnings("java:S1192")
    public static void checkBlockBreakSideEffects(BlockBreakEvent event) {
        if (Boolean.FALSE.equals(HoneypotConfigManager.getPluginConfig().getBoolean("allow-player-destruction"))
                && !(event.getPlayer().hasPermission(REMOVE_PERMISSION)
                        || event.getPlayer().hasPermission(WILDCARD_PERMISSION) || event.getPlayer().isOp()))
            return;

        Block block = event.getBlock();

        Block[] adjacentBlocks = new Block[] {
                block.getRelative(BlockFace.DOWN),
                block.getRelative(BlockFace.NORTH),
                block.getRelative(BlockFace.SOUTH),
                block.getRelative(BlockFace.EAST),
                block.getRelative(BlockFace.WEST)
        };

        Block blockUp = block.getRelative(BlockFace.UP);

        // Check all the blocks on the *side* first
        for (Block adjacentBlock : adjacentBlocks) {
            if (!PhysicsUtil.getSidePhysics().contains(adjacentBlock.getType()))
                continue;

            if (HoneypotBlockManager.getInstance().isHoneypotBlock(adjacentBlock)) {
                blockBreakEvent(new BlockBreakEvent(adjacentBlock, event.getPlayer()));
                HoneypotBlockManager.getInstance().deleteBlock(adjacentBlock);
                Honeypot.getPlugin().getLogger().warning(
                        "A Honeypot has been removed due to the block it's attached to being broken. It was located at "
                                + adjacentBlock.getX() + ", " + adjacentBlock.getY() + ", " + adjacentBlock.getZ()
                                + ". " + event.getPlayer().getName()
                                + " is the player that indirectly broke it, so the assigned action was ran against them. If needed, please recreate the Honeypot");
                Honeypot.getHoneypotLogger().log(
                        "A Honeypot has been removed due to the block it's attached to being broken. It was located at "
                                + adjacentBlock.getX() + ", " + adjacentBlock.getY() + ", " + adjacentBlock.getZ()
                                + ". " + event.getPlayer().getName()
                                + " is the player that indirectly broke it, so the assigned action was ran against them. If needed, please recreate the Honeypot");
            }
        }

        // Now check the block on the top (This is important because there are less
        // blocks that can be anchored on the sides of blocks than on the top of blocks)
        if (PhysicsUtil.getUpPhysics().contains(blockUp.getType())
                && HoneypotBlockManager.getInstance().isHoneypotBlock(blockUp)) {
            blockBreakEvent(new BlockBreakEvent(blockUp, event.getPlayer()));
            HoneypotBlockManager.getInstance().deleteBlock(blockUp);
            Honeypot.getPlugin().getLogger().warning(
                    "A Honeypot has been removed due to the block it's attached to being broken. It was located at "
                            + blockUp.getX() + ", " + blockUp.getY() + ", " + blockUp.getZ()
                            + ". " + event.getPlayer().getName()
                            + " is the player that indirectly broke it, so the assigned action was ran against them. If needed, please recreate the Honeypot");
            Honeypot.getHoneypotLogger().log(
                    "A Honeypot has been removed due to the block it's attached to being broken. It was located at "
                            + blockUp.getX() + ", " + blockUp.getY() + ", " + blockUp.getZ()
                            + ". " + event.getPlayer().getName()
                            + " is the player that indirectly broke it, so the assigned action was ran against them. If needed, please recreate the Honeypot");
        }
    }

    @SuppressWarnings({ "java:S3776", "java:S2629", "java:S1192" })
    private static void breakAction(BlockBreakEvent event) {

        // Get the block broken and the chat prefix for prettiness
        Block block = event.getBlock();
        String chatPrefix = CommandFeedback.getChatPrefix();

        // If the player isn't exempt, doesn't have permissions, and isn't Op
        if (!(event.getPlayer().hasPermission(EXEMPT_PERMISSION) || event.getPlayer().hasPermission(REMOVE_PERMISSION)
                || event.getPlayer().hasPermission(WILDCARD_PERMISSION) || event.getPlayer().isOp())) {

            // Log the event in the history table
            HoneypotPlayerHistoryManager.getInstance().addPlayerHistory(event.getPlayer(),
                    HoneypotBlockManager.getInstance().getHoneypotBlock(event.getBlock()));

            // Grab the action from the block via the storage manager
            String action = HoneypotBlockManager.getInstance().getAction(block);

            // Run certain actions based on the action of the Honeypot Block
            assert action != null;
            Honeypot.getHoneypotLogger().log("BlockBreakEvent being called for player: " + event.getPlayer().getName()
                    + ", UUID of " + event.getPlayer().getUniqueId() + ". Action is: " + action);
            switch (action) {
                case "kick" -> event.getPlayer().kickPlayer(CommandFeedback.sendCommandFeedback("kick"));

                case "ban" -> {
                    String banReason = CommandFeedback.sendCommandFeedback("ban");

                    Bukkit.getBanList(BanList.Type.NAME).addBan(event.getPlayer().getName(), banReason, null,
                            chatPrefix);
                    event.getPlayer().kickPlayer(banReason);
                }

                case "warn" -> event.getPlayer()
                        .sendMessage(CommandFeedback.sendCommandFeedback("warn"));

                case "notify" -> {
                    // Notify all staff members with permission or Op that someone tried to break a
                    // honeypot block
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission("honeypot.notify") || player.hasPermission(WILDCARD_PERMISSION)
                                || player.isOp()) {
                            player.sendMessage(chatPrefix + " " + ChatColor.RED + event.getPlayer().getName()
                                    + " was caught breaking a Honeypot block at x=" + block.getX() + ", y="
                                    + block.getY()
                                    + ", z=" + block.getZ() + " in world " + block.getWorld().getName());
                        }
                    }

                    Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(chatPrefix + " " + ChatColor.RED
                            + event.getPlayer().getName() + " was caught breaking a Honeypot block");
                }

                case "nothing" -> {
                    // Do...nothing
                }

                default -> {
                    // Default path is likely due to custom actions. Run whatever the action was
                    YamlDocument config = HoneypotConfigManager.getHoneypotsConfig();
                    if (config.contains(action)) {
                        String type = config.getString(action + ".type");
                        switch (type) {
                            case "command" -> {
                                List<String> commands = config.getStringList(action + ".commands");
                                List<String> messages = config.getStringList(action + ".messages");
                                if (commands.isEmpty()) {
                                    Honeypot.getPlugin().getLogger().warning(
                                            "Commands list is empty for Honeypot type " + action
                                                    + "! Please verify config");
                                    return;
                                }

                                for (String command : commands) {
                                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                            formatCommand(command, event));
                                }

                                if (!messages.isEmpty()) {
                                    for (String message : messages) {
                                        event.getPlayer().sendMessage(formatMessage(message, event));
                                    }
                                }
                            }

                            case "permission" -> {
                                List<String> permissionsAdd = config.getStringList(action + ".permissions-add");
                                List<String> permissionsRemove = config.getStringList(action + ".permissions-remove");
                                List<String> messages = config.getStringList(action + ".messages");
                                if (permissionsAdd.isEmpty() && permissionsRemove.isEmpty()) {
                                    Honeypot.getPlugin().getLogger()
                                            .warning("Permissions lists are empty for Honeypot type "
                                                    + action + "! Please verify config");
                                    return;
                                }

                                for (String permission : permissionsAdd) {
                                    Honeypot.getPermissions().playerAdd(null, event.getPlayer(), permission);
                                }

                                for (String permission : permissionsRemove) {
                                    Honeypot.getPermissions().playerRemove(null, event.getPlayer(), permission);
                                }

                                if (!messages.isEmpty()) {
                                    for (String message : messages) {
                                        event.getPlayer().sendMessage(formatMessage(message, event));
                                    }
                                }

                            }

                            case "broadcast" -> {
                                List<String> broadcasts = config.getStringList(action + ".broadcasts");
                                List<String> messages = config.getStringList(action + ".messages");

                                if (broadcasts.isEmpty()) {
                                    Honeypot.getPlugin().getLogger().warning(
                                            "Broadcasts list is empty for Honeypot type " + action
                                                    + "! Please verify config");
                                    return;
                                }

                                for (String broadcast : broadcasts) {
                                    Honeypot.getPlugin().getServer().broadcastMessage(formatMessage(broadcast, event));
                                }

                                if (!messages.isEmpty()) {
                                    for (String message : messages) {
                                        event.getPlayer().sendMessage(formatMessage(message, event));
                                    }
                                }
                            }

                            default -> {
                                Honeypot.getPlugin().getLogger().warning("Honeypot " + action
                                        + " tried to run as a type that doesn't exist! Please verify config");
                            }
                        }
                    }
                }
            }

            // At this point we know the player has one of those permissions above. Now we
            // need to figure out which
        } else if (event.getPlayer().hasPermission(REMOVE_PERMISSION)
                || event.getPlayer().hasPermission(WILDCARD_PERMISSION) || event.getPlayer().isOp()) {
            event.getPlayer().sendMessage(CommandFeedback.sendCommandFeedback("staffbroke"));

            // If it got to here, then they are exempt but can't break blocks anyway.
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(CommandFeedback.sendCommandFeedback("exemptnobreak"));
        }
    }

    private static void countBreak(BlockBreakEvent event) {

        // Get the config value and the amount of blocks broken
        int breaksBeforeAction = HoneypotConfigManager.getPluginConfig().getInt("blocks-broken-before-action-taken");
        int blocksBroken = HoneypotPlayerManager.getInstance().getCount(event.getPlayer());

        // getCount returns -1 if the player doesn't exist in the DB. If that's the
        // case, add the player to the DB
        if (blocksBroken == -1) {
            HoneypotPlayerManager.getInstance().addPlayer(event.getPlayer(), 0);
            blocksBroken = 0;
        }

        // Increment the blocks broken counter
        blocksBroken += 1;

        // If the blocks broken are larger than or equals 'breaks before action' or if
        // breaks before action equal equals
        // 1,
        // reset the count and perform the break
        if (blocksBroken >= breaksBeforeAction || breaksBeforeAction == 1) {
            HoneypotPlayerManager.getInstance().setPlayerCount(event.getPlayer(), 0);
            breakAction(event);
        } else {
            // Just count it
            HoneypotPlayerManager.getInstance().setPlayerCount(event.getPlayer(), blocksBroken);
        }
    }

    private static String formatMessage(String message, BlockBreakEvent event) {
        String formattedString = message.replace("%player%", event.getPlayer().getName());
        formattedString = formattedString.replace("%pLocation%", event.getPlayer().getLocation().getX() + " "
                + event.getPlayer().getLocation().getY() + " " + event.getPlayer().getLocation().getZ());
        formattedString = formattedString.replace("%bLocation%", event.getBlock().getLocation().getX() + " "
                + event.getBlock().getLocation().getY() + " " + event.getBlock().getLocation().getZ());

        return ChatColor.translateAlternateColorCodes('&', formattedString);
    }

    private static String formatCommand(String command, BlockBreakEvent event) {
        String formattedCommand = command.replace("%player%", event.getPlayer().getName());
        formattedCommand = formattedCommand.replace("%pLocation%", event.getPlayer().getLocation().getX() + " "
                + event.getPlayer().getLocation().getY() + " " + event.getPlayer().getLocation().getZ());
        formattedCommand = formattedCommand.replace("%bLocation%", event.getBlock().getLocation().getX() + " "
                + event.getBlock().getLocation().getY() + " " + event.getBlock().getLocation().getZ());

        return formattedCommand;
    }
}
