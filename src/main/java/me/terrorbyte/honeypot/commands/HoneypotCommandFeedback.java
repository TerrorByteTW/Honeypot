package me.terrorbyte.honeypot.commands;

import me.terrorbyte.honeypot.HoneypotConfigColorManager;
import org.bukkit.ChatColor;

public class HoneypotCommandFeedback {

    public static String sendCommandFeedback(String feedback, Boolean... success) {
        String feedbackMessage;
        String chatPrefix = HoneypotConfigColorManager.getChatPrefix();

        if (feedback.equalsIgnoreCase("usage")) {
            feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Incorrect usage!\n" +
                    chatPrefix + " " + ChatColor.WHITE + "/honeypot create [ban | kick | warn | notify | nothing]\n" +
                    chatPrefix + " " + ChatColor.WHITE + "/honeypot remove");

        } else if (feedback.equalsIgnoreCase("alreadyexists")) {
            feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "This is already a honeypot block. If you wish to change the action, please remove and recreate it.");

        } else if (feedback.equalsIgnoreCase("notapot")) {
            feedbackMessage = (chatPrefix + " " + ChatColor.RED + "This is not a honeypot block");

        } else if (feedback.equalsIgnoreCase("success")) {
            if (success[0].equals(true)) {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Successfully created honeypot!");

            } else if (success[0].equals(false)) {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Successfully removed honeypot!");

            } else {
                feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Success!");

            }
        } else if (feedback.equalsIgnoreCase("nopermission")){
            feedbackMessage = (chatPrefix + " " + ChatColor.RED + "You don't have permission to use this command");
        } else if (feedback.equalsIgnoreCase("reload")){
            feedbackMessage = (chatPrefix + " " + ChatColor.WHITE + "Reloading config file");
        } else {
            feedbackMessage = (chatPrefix + " " + ChatColor.DARK_RED + "Unknown error, please contact server admin");
        }

        return feedbackMessage;
    }

}
