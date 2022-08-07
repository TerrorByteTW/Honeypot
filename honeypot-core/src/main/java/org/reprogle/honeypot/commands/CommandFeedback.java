package org.reprogle.honeypot.commands;

import java.util.Objects;

import org.bukkit.ChatColor;
import org.reprogle.honeypot.HoneypotConfigManager;

import dev.dejvokep.boostedyaml.YamlDocument;

public class CommandFeedback {

    /**
     * Create private constructor to hide implicit one
     */
    private CommandFeedback() {

    }

    /**
     * A helper class which helps to reduce boilerplate player.sendMessage code by providing the strings to send instead
     * of having to copy and paste them.
     * @param feedback The string to send back
     * @param success An optional Boolean which is used for the success feedback. If none is passed, success just
     * replies "Success!"
     * @return The Feedback string
     */
    @SuppressWarnings("java:S1192")
    public static String sendCommandFeedback(String feedback, Boolean... success) {
        String feedbackMessage;
        String chatPrefix = getChatPrefix();
        YamlDocument languageFile = HoneypotConfigManager.getLanguageFile();
        
        switch (feedback.toLowerCase()) {
            case "usage" -> {
                feedbackMessage = ("\n \n \n \n \n \n-----------------------\n \n" + chatPrefix + " " + ChatColor.WHITE + "Need Help?\n" +
                "  " + "/honeypot " + ChatColor.GRAY + "create [ban | kick | warn | notify | nothing | custom]\n" +
                "  " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "remove (all | near) (optional)\n" +
                "  " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "reload\n" +
                "  " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "locate\n" + 
                "  " + ChatColor.WHITE + "/honeypot " + ChatColor.GRAY + "gui\n \n" + 
                ChatColor.WHITE + "-----------------------");
            }

            case "kick" -> feedbackMessage = chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(languageFile.getString("kick-reason"), "Kick reason is null"));

            case "ban" -> feedbackMessage = chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(languageFile.getString("ban-reason"), "Ban reason is null"));

            case "warn" -> feedbackMessage = chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(languageFile.getString("warn-message"), "Warn message is null"));
            
            case "alreadyexists" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("already-exists")));

            case "success" -> {
                if (success[0].equals(true)) {
                    feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("success.created")));

                }
                else if (success[0].equals(false)) {
                    feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("success.removed")));

                }
                else {
                    feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("success.default")));

                }
            }

            case "notapot" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("not-a-honeypot")));

            case "nopermission" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("no-permission")));

            case "reload" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("reload")));

            case "foundpot" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("found-pots")));

            case "nopotfound" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("no-pots-found")));

            case "updateavailable" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("update-available")));

            case "againstfilter" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("against-filter")));

            case "notlookingatblock" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("not-looking-at-block")));

            case "noexist" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("no-exist")));

            case "deletedall" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("deleted.all")));

            case "deletednear" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("deleted.near")));

            case "upgrade" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("upgrade")));

            case "alreadyupgraded" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("already-upgraded")));

            case "worldguard" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("worldguard")));

            case "griefprevention" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("griefprevention")));

            case "staffbroke" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("staff-broke")));

            case "exemptnobreak" -> feedbackMessage = (chatPrefix + " " + ChatColor.translateAlternateColorCodes('&', languageFile.getString("exempt-no-break")));

            default -> feedbackMessage = (chatPrefix + " " + ChatColor.DARK_RED + ChatColor.translateAlternateColorCodes('&', languageFile.getString("unknown-error")));
        }
        return feedbackMessage;
    }

    /**
     * Return the chat prefix object from config
     * 
     * @return The chat prefix, preformatted with color and other modifiers
     */
    public static String getChatPrefix() {
        return ChatColor.translateAlternateColorCodes('&',Objects.requireNonNull(HoneypotConfigManager.getLanguageFile().getString("prefix")));
    }

}
