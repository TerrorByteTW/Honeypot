/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) TerrorByte and Honeypot Contributors 2022 - 2025.
 *
 * This program is free software: You can redistribute it and/or modify it under
 *  the terms of the Mozilla Public License 2.0 as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including,
 * without limitation, warranties that the Covered Software is free of defects, merchantable,
 * fit for a particular purpose or non-infringing. See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jspecify.annotations.NonNull;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.storageproviders.StorageProvider;
import org.reprogle.honeypot.common.utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Command implements BasicCommand {

    private final String rootPermission;
    private final String usageMessageKey;
    private final String noPermissionKey;

    private final CommandFeedback feedback;
    private final Set<HoneypotSubCommand> subcommands;

    private final JavaPlugin plugin;
    private final HoneypotLogger logger;
    private final BytePluginConfig config;
    private final GhostHoneypotFixer fixer;
    private final HoneypotSupportedVersions supportedVersions;

    public Command(
            String rootPermission,
            String usageMessageKey,
            String noPermissionKey,
            JavaPlugin plugin,
            HoneypotLogger logger,
            CommandFeedback feedback,
            Set<HoneypotSubCommand> subcommands,
            GhostHoneypotFixer fixer,
            BytePluginConfig config,
            HoneypotSupportedVersions supportedVersions
    ) {
        this.plugin = plugin;
        this.rootPermission = rootPermission;
        this.usageMessageKey = usageMessageKey;
        this.noPermissionKey = noPermissionKey;
        this.feedback = feedback;
        this.subcommands = subcommands;
        this.logger = logger;
        this.config = config;
        this.fixer = fixer;
        this.supportedVersions = supportedVersions;
    }


    /**
     * Called by Bukkit when a player runs a command registered to our plugin. When
     * called, the plugin will check if the
     * sender is a player. If it is, it will first verify permissions, then verify
     * if there were any subcommands. If
     * not, show the GUI. If there were subcommands, but they aren't valid, show the
     * usage.
     * <p>
     * If the sender is not a player, it will check if the command was reload. If it
     * was, it'll allow the command to be
     * run, otherwise it will throw an error.
     *
     * @param source The CommandSourceStack of the command executed. Not necessarily a player,
     *               could be console or a plugin
     * @param args   The arguments passed
     */
    @Override
    public void execute(CommandSourceStack source, String @NonNull [] args) {
        CommandSender sender = source.getSender();

        if (sender instanceof Player p) {
            if (!(p.hasPermission(rootPermission) || p.hasPermission("honeypot.*") || p.isOp())) {
                p.sendMessage(feedback.sendCommandFeedback(noPermissionKey));
            }

            if (args.length > 0) {
                // For each subcommand in the subcommands array list, check if the argument is
                // the same as the command.
                // If so, run said subcommand
                for (HoneypotSubCommand subcommand : subcommands) {
                    if (args[0].equalsIgnoreCase(subcommand.getName())) {
                        try {
                            if (!hasAnyPermission(p, subcommand.getRequiredPermissions())) {
                                p.sendMessage(feedback.sendCommandFeedback(noPermissionKey));
                                return;
                            }

                            subcommand.perform(p, args);
                            return;
                        } catch (IOException e) {
                            logger.severe(Component.text("Error while running command " + args[0] + "! Full stack trace: " + e));
                        }
                    }
                }

                p.sendMessage(feedback.sendCommandFeedback(usageMessageKey));
            } else {
                // If no subcommands are passed, open the GUI. This is done by looping through
                // all the subcommands and
                // finding the GUI one, then performing it
                for (HoneypotSubCommand subcommand : subcommands) {
                    if (subcommand.getName().equals("gui")) {
                        try {
                            if (!hasAnyPermission(p, subcommand.getRequiredPermissions())) {
                                p.sendMessage(feedback.sendCommandFeedback(noPermissionKey));
                                return;
                            }

                            subcommand.perform(p, args);
                            return;
                        } catch (IOException e) {
                            logger.severe(Component.text("Error while running command! Full stack trace: " + e));
                        }
                    }
                }
            }
        } else {
            if (args.length > 0 && args[0].equals("reload")) {
                config.reload();
                fixer.cancelTask();
                if (config.config().getBoolean("ghost-honeypot-checker.enable")) {
                    fixer.startTask();
                }

                String providerName = config.config().getString("storage-method");
                if (!Registry.getStorageProvider().getProviderName().equalsIgnoreCase(providerName)) {
                    StorageProvider provider = Registry.getStorageManagerRegistry().getStorageProvider(providerName);
                    if (provider != null) {
                        if (!config.config().getBoolean("allow-third-party-storage-providers")) {
                            logger.severe(Component.text("The storage method was updated to a custom provider, but the server is not configured to allow third-party storage providers! On your next reboot Honeypot WILL crash ON PURPOSE! Please validate your config"));
                        }
                        Registry.setStorageProvider(provider);
                        logger.info(Component.text("The storage provider was updated to \"" + providerName + "\""));
                    } else {
                        logger.severe(Component.text("The storage provider was updated to \"" + providerName + "\" but it is not registered! On your next reboot Honeypot WILL crash ON PURPOSE! Please validate your config"));
                    }
                }

                sender.sendMessage(feedback.sendCommandFeedback("reload"));
                logger.info(Component.text("Honeypot has successfully been reloaded"));
            } else {
                ConsoleCommandSender console = plugin.getServer().getConsoleSender();
                console.sendMessage(feedback.buildSplash(plugin));
                console.sendMessage(feedback.getChatPrefix().append(Component.text("Honeypot running on Paper version " + Bukkit.getVersion())));
                supportedVersions.checkIfServerSupported();
            }
        }
    }

    /**
     * This function is responsible for tab completion of our plugin. It will check
     * if the tab completer is at the first
     * arg. If it is, return partial matches for the tab completer. If it's longer
     * than one arg, return partial matches
     * for the subcommand (such as create)
     *
     * @param source  The sender of the command
     * @param args    The arguments passed
     * @return A Collection of valid tab completed commands
     */
    @Override
    public @NonNull Collection<String> suggest(CommandSourceStack source, String @NonNull [] args) {
        CommandSender sender = source.getSender();

        if (!(source.getSender() instanceof Player p)) return List.of();

        if (!sender.hasPermission(rootPermission) && !sender.hasPermission("honeypot.*") && !sender.isOp()) {
            return List.of();
        }

        if (args.length == 0) {
            return List.of(subcommands.stream().map(HoneypotSubCommand::getName).toArray(String[]::new));
        } else if (args.length == 1) {
            // Create a subcommands array list and a subcommandsString array list to store
            // the subcommands as strings
            ArrayList<String> subcommandsTabComplete = new ArrayList<>();

            // Copy each partial match to the subcommands list
            StringUtil.copyPartialMatches(args[0], List.of(subcommands.stream().map(HoneypotSubCommand::getName).toArray(String[]::new)), subcommandsTabComplete);

            return subcommandsTabComplete;
        } else {
            // If the argument is the 2nd one or more, return the subcommands for that
            // subcommand
            for (HoneypotSubCommand subcommand : subcommands) {
                // Check if the first argument equals the command in the current iteration
                if (args[0].equalsIgnoreCase(subcommand.getName())) {
                    // Create a new array and copy partial matches of the current argument.
                    // getSubcommands can actually handle more than one subcommand per
                    // root command, meaning if the argument length is 3 or 4 or 5, it can handle
                    // those accordingly. See HoneypotCreate.java for this in action
                    ArrayList<String> subcommandsTabComplete = new ArrayList<>();

                    StringUtil.copyPartialMatches(args[args.length - 1], subcommand.getSubcommands(p, args),
                            subcommandsTabComplete);

                    return subcommandsTabComplete;
                }
            }
        }

        return List.of();
    }

    private static boolean hasAnyPermission(CommandSender sender, List<HoneypotPermission> perms) {
        if (perms == null || perms.isEmpty()) return true;
        for (HoneypotPermission perm : perms) {
            if (sender.hasPermission(perm.permission())) return true;
        }

        return false;
    }
}
