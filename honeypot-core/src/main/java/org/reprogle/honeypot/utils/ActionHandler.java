package org.reprogle.honeypot.utils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;

import dev.dejvokep.boostedyaml.YamlDocument;

public class ActionHandler {

    private ActionHandler() {
    }

    @SuppressWarnings({ "java:S3776", "java:S2629", "java:S1192" })
    public static void handleCustomAction(String action, Block block, Player player) {
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
                                formatCommand(command, block, player));
                    }

                    if (!messages.isEmpty()) {
                        for (String message : messages) {
                            player.sendMessage(formatMessage(message, block, player));
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
                        Honeypot.getPermissions().playerAdd(null, player, permission);
                    }

                    for (String permission : permissionsRemove) {
                        Honeypot.getPermissions().playerRemove(null, player, permission);
                    }

                    if (!messages.isEmpty()) {
                        for (String message : messages) {
                            player.sendMessage(formatMessage(message, block, player));
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
                        Honeypot.getPlugin().getServer().broadcastMessage(formatMessage(broadcast, block, player));
                    }

                    if (!messages.isEmpty()) {
                        for (String message : messages) {
                            player.sendMessage(formatMessage(message, block, player));
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

    private static String formatMessage(String message, Block block, Player player) {
        String formattedString = message.replace("%player%", player.getName());
        formattedString = formattedString.replace("%pLocation%", player.getLocation().getX() + " "
                + player.getLocation().getY() + " " + player.getLocation().getZ());
        formattedString = formattedString.replace("%bLocation%", block.getLocation().getX() + " "
                + block.getLocation().getY() + " " + block.getLocation().getZ());

        return ChatColor.translateAlternateColorCodes('&', formattedString);
    }

    private static String formatCommand(String command, Block block, Player player) {
        String formattedCommand = command.replace("%player%", player.getName());
        formattedCommand = formattedCommand.replace("%pLocation%", player.getLocation().getX() + " "
                + player.getLocation().getY() + " " + player.getLocation().getZ());
        formattedCommand = formattedCommand.replace("%bLocation%", block.getLocation().getX() + " "
                + block.getLocation().getY() + " " + block.getLocation().getZ());

        return formattedCommand;
    }
}
