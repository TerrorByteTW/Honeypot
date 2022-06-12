package me.terrorbyte.honeypot.commands;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.HoneypotConfigManager;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotCreate;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotGUI;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotHelp;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotLocate;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotReload;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotRemove;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandManager implements TabExecutor {

    //Create an ArrayList of SubCommands called subcommands
    private final ArrayList<HoneypotSubCommand> SUBCOMMANDS = new ArrayList<>();
    private final ArrayList<String> SUBCOMMANDS_NAME_ONLY = new ArrayList<>();

    /**
     * Registers all commands
     */
    public CommandManager(){
        SUBCOMMANDS.add(new HoneypotCreate());
        SUBCOMMANDS.add(new HoneypotRemove());
        SUBCOMMANDS.add(new HoneypotReload());
        SUBCOMMANDS.add(new HoneypotLocate());
        SUBCOMMANDS.add(new HoneypotGUI());
        SUBCOMMANDS.add(new HoneypotHelp());

        for (int i = 0; i < getSubcommands().size(); i++) {
            SUBCOMMANDS_NAME_ONLY.add(getSubcommands().get(i).getName());
        }
    }

    /**
     * Called by Bukkit when a player runs a command registered to our plugin.
     * When called, the plugin will check if the sender is a player. If it is, it will first verify permissions,
     * then verify if there were any subcommands. If not, show the GUI. If there were subcommands but they aren't valid, show the usage
     * 
     * If the sender is not a player, it will check if the command was reload. If it was, 
     * it'll allow the command to be run, otherwise it will throw an error.
     * 
     * @param sender The Sender sending the command. Not necessarily a player, could be console or a plugin
     * @param command The Command being executed
     * @param label The lable of the command
     * @param args Any arguments passed to the command
     * @return True if it ran successfully, false if it errored at any point. Defaults as false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the command sender is a player
        if(sender instanceof Player p) {

            if(!(p.hasPermission("honeypot.commands") || p.hasPermission("honeypot.*") || p.isOp())) {
                p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
            }

            //If it's a player, ensure there is at least 1 argument given
            if (args.length > 0) {
                //For each subcommand in the subcommands array list, check if the argument is the same as the command. If so, run said subcommand
                for (HoneypotSubCommand subcommand : SUBCOMMANDS) {
                    if (args[0].equalsIgnoreCase(subcommand.getName())) {
                        try {
                            subcommand.perform(p, args);
                            return true;
                        } catch (IOException e) {
                            Honeypot.getPlugin().getLogger().severe("Error while running command " + args[0] + "! Full stack trace: " + e);
                        }
                    }
                }

                p.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
            } else {
                //If no subcommands are passed, open the GUI. This is done by looping through all the subcommands and finding the GUI one, then performing it
                for (HoneypotSubCommand subcommand : SUBCOMMANDS) {
                    if (subcommand.getName().equals("gui")) {
                        try {
                            subcommand.perform(p, args);
                            return true;
                        } catch (IOException e) {
                            Honeypot.getPlugin().getLogger().severe("Error while running command " + args[0] + "! Full stack trace: " + e);
                        }
                    }
                }
            }

        } else {
            if (args.length > 0 && args[0].equals("reload")){
                Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(CommandFeedback.sendCommandFeedback("reload"));
                try {
                    HoneypotConfigManager.getPluginConfig().reload();
                    HoneypotConfigManager.getPluginConfig().save();
                    return true;
                } catch (IOException e) {
                    Honeypot.getPlugin().getLogger().severe("Could not reload honeypot config! Full stack trace: " + e);
                }
            } else {
                //If the sender is not a player (Probably the console) and did not use the reload command, send this message
                Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "You must run this command as an in-game player!");
            }
        }

        return false;
    }

    /**
     * Returns a list of all subcommands for tab completion
     * @return List of all subcommands
     */
    public ArrayList<HoneypotSubCommand> getSubcommands(){
        return SUBCOMMANDS;
    }

    /**
     * This function is responsible for tab completion of our pluign. It will check if the tab completer is at the first arg.
     * If it is, return partial matches for the tab completer. If it's longer than one arg, return partial matches for the subcommand (such as create)
     * 
     * @param sender The sender of the command
     * @param command The command being tab completed
     * @param alias The alias of the command
     * @param args The arguments passed to the tab completer (Required for tab completion)
     * @return A list of valid tab completed commands
     */
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        //Cast the CommandSender object to Player
        Player p = (Player) sender;

        //Only auto-complete if they have the permissions
        if(p.hasPermission("honeypot.commands") || p.hasPermission("honeypot.*") || p.isOp()){
            //If the argument is the first one return the subcommands
            if (args.length == 1) {
                //Create a subcommands array list and a subcommandsString array list to store the subcommands as strings
                ArrayList<String> subcommands = new ArrayList<>();

                //Copy each partial match to the subcommands list
                StringUtil.copyPartialMatches(args[0], SUBCOMMANDS_NAME_ONLY, subcommands);

                return subcommands;
            } else if (args.length >= 2) {
                //If the argument is the 2nd one or more, return the subcommands for that subcommand
                for (HoneypotSubCommand subcommand : SUBCOMMANDS) {
                    /*
                    I didn't know how this code worked at first, even though I wrote it myself and didn't copy from anywhere on the internet. I took some time to figure it
                    out and am now commenting in the explanation so I don't forget lol.

                    First we need to figure out which command of Honeypot we're using. There are 6: Create, Locate, Reload, Remove, GUI, and Help.
                    We are going to iterate through all the Honeypot original subcommands until we figure out which one we're on.
                    Once we figure out which command we're on, we're going to create a NEW subcommands ArrayList.
                    In that new ArrayList we're going to pull all the 2nd level subcommands from the original subcommand we passed (One of the original six) and copy partial
                    matches into the new subcommands array we created, which then we'll return to the player.
                    */
                    if (args[0].equalsIgnoreCase(subcommand.getName())) {
                        ArrayList<String> subcommands = new ArrayList<>();

                        StringUtil.copyPartialMatches(args[args.length - 1], subcommand.getSubcommands(p, args), subcommands);

                        return subcommands;
                    }
                }
            }
        }

        //If the argument does not exist at all
        return null;
    }

}
