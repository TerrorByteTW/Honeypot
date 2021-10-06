package me.terrorbyte.honeypot.commands;

import org.bukkit.ChatColor;

public class HoneypotCommandFeedback {

    public static String sendCommandFeedback(String feedback, Boolean... success) {
        String feedbackMessage;

        if (feedback.equalsIgnoreCase("usage")) {
            feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "Incorrect usage!\n" +
                    ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "/honeypot create [ban | kick | warn | notify | nothing]\n" +
                    ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "/honeypot remove");

        } else if (feedback.equalsIgnoreCase("alreadyexists")) {
            feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "This is already a honeypot block. If you wish to change the action, please remove and recreate it.");

        } else if (feedback.equalsIgnoreCase("notapot")) {
            feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.RED + "This is not a honeypot block");

        } else if (feedback.equalsIgnoreCase("success")) {
            if (success[0].equals(true)) {
                feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "Successfully created honeypot!");

            } else if (success[0].equals(false)) {
                feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "Successfully removed honeypot!");

            } else {
                feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "Success!");

            }
        } else if (feedback.equalsIgnoreCase("nopermission")){
            feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.RED + "You don't have permission to use this command");
        } else if (feedback.equalsIgnoreCase("reload")){
            feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "Reloading config file");
        } else {
            feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.DARK_RED + "Unknown error, please contact server admin");
        }

        return feedbackMessage;
    }

}
