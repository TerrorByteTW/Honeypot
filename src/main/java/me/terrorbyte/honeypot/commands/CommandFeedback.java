package me.terrorbyte.honeypot.commands;

import org.bukkit.ChatColor;

import me.terrorbyte.honeypot.ConfigColorManager;

public class CommandFeedback {
    
    public static String sendCommandFeedback(String feedback, Boolean... success) {
        String feedbackMessage;
        String chatPrefix = ConfigColorManager.getChatPrefix();
        
        switch(feedback.toLowerCase()){
            case "usage" -> {
                feedbackMessage = ("\n \n \n \n \n \n-----------------------\n \n" + chatPrefix + " " + ChatColor.WHITE + "Need Help?\n" +
                "     " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "create [ban | kick | warn | notify | nothing]\n" +
                "     " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "remove (all | near) (optional)\n" +
                "     " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "reload\n" +
                "     " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "locate\n" + 
                "     " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "gui\n \n" + 
                ChatColor.WHITE + "-----------------------");
            }
            
            case "alreadyexists" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "This is already a honeypot block. If you wish to change the action, please remove and recreate it.");
            }
            
            case "success" -> {
                if (success[0].equals(true)) {
                    feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Successfully created honeypot!");
        
                } else if (success[0].equals(false)) {
                    feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Successfully removed honeypot!");
        
                } else {
                    feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Success!");
        
                }
            }

            case "notapot" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.RED + "This is not a honeypot block");
            }

            case "nopermission" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.RED + "You don't have permission to use this command");
            }
            
            case "reload" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Reloading config file");
            }

            case "foundpot" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Highlighting honeypot blocks within range");
            }

            case "nopotfound" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "No honeypot blocks found within range");
            }
            
            case "updateavailable" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "An update is available for this plugin. Download it at " + ChatColor.GOLD + "https://github.com/TerrrorByte/Honeypot " + ChatColor.WHITE + "for the latest features and security updates!"); 
            }

            case "againstfilter" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "This block is not in the filter, so you can't do that!");
            }

            case "inputcancelled" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Honeypot creation cancelled");
            }

            case "customactionsdisabled" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Custom actions are disabled. Enable them in the config");
            }

            case "notlookingatblock" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "You need to be looking at a block to perform this command");
            }

            case "deletedall" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Deleted all honeypot blocks");
            }

            case "deletednear" -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Deleted all honeypot blocks within a 5 block radius");
            }

            default -> {
                feedbackMessage = (chatPrefix + " " + ChatColor.DARK_RED + "Unknown error, please contact server admin");
            }
        }

        return feedbackMessage;
    }

}
