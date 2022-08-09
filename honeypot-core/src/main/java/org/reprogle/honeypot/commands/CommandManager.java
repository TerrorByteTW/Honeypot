package org.reprogle.honeypot.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.HoneypotConfigManager;
import org.reprogle.honeypot.commands.subcommands.HoneypotCreate;
import org.reprogle.honeypot.commands.subcommands.HoneypotGUI;
import org.reprogle.honeypot.commands.subcommands.HoneypotHelp;
import org.reprogle.honeypot.commands.subcommands.HoneypotInfo;
import org.reprogle.honeypot.commands.subcommands.HoneypotLocate;
import org.reprogle.honeypot.commands.subcommands.HoneypotReload;
import org.reprogle.honeypot.commands.subcommands.HoneypotRemove;
import org.reprogle.honeypot.commands.subcommands.HoneypotUpgrade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandManager implements TabExecutor {

    // Create an ArrayList of SubCommands called subcommands
    private final ArrayList<HoneypotSubCommand> subcommands = new ArrayList<>();

    private final ArrayList<String> subcommandsNameOnly = new ArrayList<>();

    /**
     * Registers all commands
     */
    public CommandManager() {
        subcommands.add(new HoneypotCreate());
        subcommands.add(new HoneypotRemove());
        subcommands.add(new HoneypotReload());
        subcommands.add(new HoneypotLocate());
        subcommands.add(new HoneypotGUI());
        subcommands.add(new HoneypotHelp());
        subcommands.add(new HoneypotUpgrade());
        subcommands.add(new HoneypotInfo());

        for (int i = 0; i < getSubcommands().size(); i++) {
            subcommandsNameOnly.add(getSubcommands().get(i).getName());
        }
    }

    /**
     * Called by Bukkit when a player runs a command registered to our plugin. When called, the plugin will check if the
     * sender is a player. If it is, it will first verify permissions, then verify if there were any subcommands. If
     * not, show the GUI. If there were subcommands but they aren't valid, show the usage
     * 
     * If the sender is not a player, it will check if the command was reload. If it was, it'll allow the command to be
     * run, otherwise it will throw an error.
     * 
     * @param sender The Sender sending the command. Not necessarily a player, could be console or a plugin
     * @param command The Command being executed
     * @param label The lable of the command
     * @param args Any arguments passed to the command
     * @return True if it ran successfully, false if it errored at any point. Defaults as false
     */
    @Override
    @SuppressWarnings("java:S3776")
    public boolean onCommand(@NotNull
    CommandSender sender, @NotNull
    Command command, @NotNull
    String label, @NotNull
    String[] args) {

        if(!label.equalsIgnoreCase("honeypot")) return false;

        // Check if the command sender is a player
        if (sender instanceof Player p) {

            if (!(p.hasPermission("honeypot.commands") || p.hasPermission("honeypot.*") || p.isOp())) {
                p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
            }

            // If it's a player, ensure there is at least 1 argument given
            if (args.length > 0) {
                // For each subcommand in the subcommands array list, check if the argument is the same as the command.
                // If so, run said subcommand
                for (HoneypotSubCommand subcommand : subcommands) {
                    if (args[0].equalsIgnoreCase(subcommand.getName())) {
                        try {
                            subcommand.perform(p, args);
                            return true;
                        }
                        catch (IOException e) {
                            Honeypot.getPlugin().getLogger()
                                    .severe("Error while running command " + args[0] + "! Full stack trace: " + e);
                        }
                    }
                }

                p.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
            }
            else {
                // If no subcommands are passed, open the GUI. This is done by looping through all the subcommands and
                // finding the GUI one, then performing it
                for (HoneypotSubCommand subcommand : subcommands) {
                    if (subcommand.getName().equals("gui")) {
                        try {
                            subcommand.perform(p, args);
                            return true;
                        }
                        catch (IOException e) {
                            Honeypot.getPlugin().getLogger()
                                    .severe("Error while running command " + args[0] + "! Full stack trace: " + e);
                        }
                    }
                }
            }

        }
        else {
            if (args.length > 0 && args[0].equals("reload")) {
                try {
                    HoneypotConfigManager.getPluginConfig().reload();
                    HoneypotConfigManager.getPluginConfig().save();

                    HoneypotConfigManager.getGuiConfig().reload();
                    HoneypotConfigManager.getGuiConfig().save();

                    HoneypotConfigManager.getHoneypotsConfig().reload();
                    HoneypotConfigManager.getHoneypotsConfig().save();

                    Honeypot.getPlugin().getServer().getConsoleSender()
                        .sendMessage(CommandFeedback.sendCommandFeedback("reload"));
                    return true;
                }
                catch (IOException e) {
                    Honeypot.getPlugin().getLogger().severe("Could not reload honeypot config! Full stack trace: " + e);
                }
            }
            else {
                ConsoleCommandSender console = Honeypot.getPlugin().getServer().getConsoleSender();
                console.sendMessage(ChatColor.GOLD + "\n" 
                    + " _____                         _\n"
                    + "|  |  |___ ___ ___ _ _ ___ ___| |_\n" 
                    + "|     | . |   | -_| | | . | . |  _|    by" + ChatColor.RED + " TerrorByte\n" + ChatColor.GOLD 
                    + "|__|__|___|_|_|___|_  |  _|___|_|      version " + ChatColor.RED + Honeypot.getPlugin().getDescription().getVersion() + "\n" + ChatColor.GOLD
                    + "                  |___|_|"
                );
                console.sendMessage(CommandFeedback.getChatPrefix() + "Honeypot running on Spigot version " + Bukkit.getVersion());
                if (!Honeypot.versionCheck()) {
                    console.sendMessage(CommandFeedback.getChatPrefix() + "This version of Honeypot is not guaranteed to work on this version of Spigot. Some newer blocks (If any) may exhibit unusual behavior!");
                }
            }
        }

        return false;
    }

    /**
     * Returns a list of all subcommands for tab completion
     * @return List of all subcommands
     */
    public List<HoneypotSubCommand> getSubcommands() {
        return subcommands;
    }

    /**
     * This function is responsible for tab completion of our pluign. It will check if the tab completer is at the first
     * arg. If it is, return partial matches for the tab completer. If it's longer than one arg, return partial matches
     * for the subcommand (such as create)
     * 
     * @param sender The sender of the command
     * @param command The command being tab completed
     * @param alias The alias of the command
     * @param args The arguments passed to the tab completer (Required for tab completion)
     * @return A list of valid tab completed commands
     */
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull
    CommandSender sender, @NotNull
    Command command, @NotNull
    String alias, @NotNull
    String[] args) {

        // Cast the CommandSender object to Player
        Player p = (Player) sender;

        // Only auto-complete if they have the permissions
        if (p.hasPermission("honeypot.commands") || p.hasPermission("honeypot.*") || p.isOp()) {
            // If the argument is the first one return the subcommands
            if (args.length == 1) {
                // Create a subcommands array list and a subcommandsString array list to store the subcommands as
                // strings
                ArrayList<String> subcommandsTabComplete = new ArrayList<>();

                // Copy each partial match to the subcommands list
                StringUtil.copyPartialMatches(args[0], subcommandsNameOnly, subcommandsTabComplete);

                return subcommandsTabComplete;
            }
            else if (args.length >= 2) {
                // If the argument is the 2nd one or more, return the subcommands for that subcommand
                for (HoneypotSubCommand subcommand : subcommands) {
                    // Check if the first argument equals the command in the current interation
                    if (args[0].equalsIgnoreCase(subcommand.getName())) {
                        // Create a new array and copy partial matches of the current argument. getSubcommands can actually handle more than one subcommand per
                        // root command, meaning if the argument length is 3 or 4 or 5, it can handle those accordingly. See HoneypotCreate.java for this in action
                        ArrayList<String> subcommandsTabComplete = new ArrayList<>();

                        StringUtil.copyPartialMatches(args[args.length - 1], subcommand.getSubcommands(p, args),
                                subcommandsTabComplete);

                        return subcommandsTabComplete;
                    }
                }
            }
        }

        // If the argument does not exist at all
        return null;
    }

}
