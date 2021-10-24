package me.terrorbyte.honeypot;

import org.bukkit.ChatColor;

public class HoneypotConfigColorManager {

    public static String getChatPrefix(){
        return ChatColor.translateAlternateColorCodes('&', Honeypot.getPlugin().getConfig().getString("chat-prefix"));
    }

    public static String getConfigMessage(String messageToRetrieve){
        String messageReturn = "";

        switch (messageToRetrieve) {
            case "kick" -> {
                messageReturn = ChatColor.translateAlternateColorCodes('&', Honeypot.getPlugin().getConfig().getString("kick-reason"));
            }

            case "ban" -> {
                messageReturn = ChatColor.translateAlternateColorCodes('&', Honeypot.getPlugin().getConfig().getString("ban-reason"));
            }

            case "warn" -> {
                messageReturn = ChatColor.translateAlternateColorCodes('&', Honeypot.getPlugin().getConfig().getString("warn-message"));
            }

            default -> {
                //Do nothing
            }
        }

        return messageReturn;
    }
}
