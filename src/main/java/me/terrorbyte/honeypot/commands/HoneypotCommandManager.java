package me.terrorbyte.honeypot.commands;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotCreate;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotReload;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotRemove;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HoneypotCommandManager implements TabExecutor {

    //Create an ArrayList of SubCommands called subcommands
    private final ArrayList<HoneypotSubCommand> subcommands = new ArrayList<>();

    //Register all our subcommands to the array list
    public HoneypotCommandManager(){
        subcommands.add(new HoneypotCreate());
        subcommands.add(new HoneypotRemove());
        subcommands.add(new HoneypotReload());
    }

    //This method allows for running our commands
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the command sender is a player
        if(sender instanceof Player p) {

            if(p.hasPermission("honeypot.commands") || p.hasPermission("honeypot.*") || p.isOp()){
                //If it's a player, ensure there is at least 1 argument given
                if(args.length > 0) {
                    //For each subcommand in the subcommands array list, check if the argument is the same as the command. If so, run said subcommand
                    for (HoneypotSubCommand subcommand : subcommands) {
                        if (args[0].equalsIgnoreCase(subcommand.getName())) {
                            subcommand.perform(p, args);
                        }
                    }
                } else {
                    //If none of the subcommands are in the list, send the usage command feedback.
                    p.sendMessage(HoneypotCommandFeedback.sendCommandFeedback("usage"));
                }
            } else {
                //If they don't have permissions, let the player know
                p.sendMessage(HoneypotCommandFeedback.sendCommandFeedback("nopermission"));
            }
        } else {
            //If the sender is not a player (Probably the console), send this message
            Honeypot.getPlugin().getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "You must run this command as an in-game player!");
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

                //For each subcommand in the subcommands array list, convert it to a string and add it to the subcommandsString list
                for (int i = 0; i < getSubcommands().size(); i++) {
                    subcommands.add(getSubcommands().get(i).getName());
                }

                return subcommands;
            } else if (args.length >= 2) {
                //If the argument is the 2nd one or more, return the subcommands for that subcommand
                for (HoneypotSubCommand subcommand : subcommands) {
                    if (args[0].equalsIgnoreCase(subcommand.getName())) {
                        return subcommand.getSubcommands((Player) sender, args);
                    }
                }
            }
        }

        //If the argument does not exist at all
        return null;
    }

}
