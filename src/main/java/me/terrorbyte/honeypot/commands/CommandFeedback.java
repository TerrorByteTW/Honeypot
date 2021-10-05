package me.terrorbyte.honeypot.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CommandFeedback {

    public static String sendCommandFeedback(String feedback, Boolean... success) {
        String feedbackMessage;

        if (feedback.equalsIgnoreCase("usage")) {
            feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "Incorrect usage!\n" +
                    ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "/honeypot create <action> (Default action is kick unless specified)\n" +
                    ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "/honeypot remove");

        } else if (feedback.equalsIgnoreCase("alreadyexists")) {
            feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "This is already a honeypot block");

        } else if (feedback.equalsIgnoreCase("notapot")) {
            feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "This is not a honeypot block");

        } else if (feedback.equalsIgnoreCase("success")){
            if (success[0].equals(true)){
                feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "Successfully created honeypot!");

            } else if (success[0].equals(false)){
                feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "Successfully removed honeypot!");

            } else {
                feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "Success!");

            }
        } else {
            feedbackMessage = (ChatColor.AQUA + "[Honeypot] " + ChatColor.WHITE + "Unknown error, please contact server admin");
        }

        return feedbackMessage;
    }

}
