package me.terrorbyte.honeypot.commands;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotCreate;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotLocate;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotReload;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotRemove;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import me.terrorbyte.honeypot.storagemanager.HoneypotPlayerStorageManager;
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
    private final ArrayList<HoneypotSubCommand> subcommands = new ArrayList<>();
    private final ArrayList<String> subcommandsNameOnly = new ArrayList<>();

    //Register all our subcommands to the array list
    public CommandManager(){
        subcommands.add(new HoneypotCreate());
        subcommands.add(new HoneypotRemove());
        subcommands.add(new HoneypotReload());
        subcommands.add(new HoneypotLocate());

        for (int i = 0; i < getSubcommands().size(); i++) {
            subcommandsNameOnly.add(getSubcommands().get(i).getName());
        }
    }

    //This method allows for running our commands
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the command sender is a player
        if(sender instanceof Player p) {

            if(!(p.hasPermission("honeypot.commands") || p.hasPermission("honeypot.*") || p.isOp())) {
                p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
                return false;
            }

            //If it's a player, ensure there is at least 1 argument given
            if (args.length > 0) {
                //For each subcommand in the subcommands array list, check if the argument is the same as the command. If so, run said subcommand
                for (HoneypotSubCommand subcommand : subcommands) {
                    if (args[0].equalsIgnoreCase(subcommand.getName())) {
                        try {
                            subcommand.perform(p, args);
                        } catch (IOException e) {
                            Honeypot.getPlugin().getLogger().severe("Error while running command " + args[0] + "! Full stack trace: " + e);
                        }
                    }
                }
            } else {
                //If no subcommands are passed, send the usage command feedback.
                p.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
            }

        } else {
            if (args.length > 0 && args[0].equals("reload")){
                Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(CommandFeedback.sendCommandFeedback("reload"));
                try {
                    Honeypot.config.reload();
                    Honeypot.config.save();
                } catch (IOException e) {
                    Honeypot.getPlugin().getLogger().severe("Could not reload honeypot config! Full stack trace: " + e);
                }

                try {
                    HoneypotBlockStorageManager.loadHoneypotBlocks(Honeypot.getPlugin());
                    HoneypotPlayerStorageManager.loadHoneypotPlayers(Honeypot.getPlugin());
                } catch (IOException e) {
                    //Nothing
                }
            } else {
                //If the sender is not a player (Probably the console) and did not use the reload command, send this message
                Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "You must run this command as an in-game player!");
            }
        }

        return true;
    }

    //Return a list of all subcommands (used for tab completion).
    public ArrayList<HoneypotSubCommand> getSubcommands(){
        return subcommands;
    }

    //This method allows for tab completion of our command
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
                StringUtil.copyPartialMatches(args[0], subcommandsNameOnly, subcommands);

                return subcommands;
            } else if (args.length >= 2) {
                //If the argument is the 2nd one or more, return the subcommands for that subcommand
                for (HoneypotSubCommand subcommand : subcommands) {
                    /*
                    I didn't know how this code worked at first, even though I wrote it myself and didn't copy from anywhere on the internet. I took some time to figure it
                    out and am now commenting in the explanation so I don't forget lol.

                    First we need to figure out which command of Honeypot we're using. There are 4: Create, Locate, Reload, and Remote.
                    We are going to iterate through all the Honeypot original subcommands until we figure out which one we're on.
                    Once we figure out which command we're on, we're going to create a NEW subcommands ArrayList.
                    In that new ArrayList we're going to pull all the 2nd level subcommands from the original subcommand we passed (One of the original four) and copy partial
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
