package me.terrorbyte.honeypot;

import org.bukkit.ChatColor;

import java.util.Objects;

public class HoneypotConfigColorManager {

    public static String getChatPrefix(){
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Honeypot.getPlugin().getConfig().getString("chat-prefix")));
    }

    public static String getConfigMessage(String messageToRetrieve){
        String messageReturn = "";

        switch (messageToRetrieve) {
            case "kick" -> messageReturn = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Honeypot.getPlugin().getConfig().getString("kick-reason"), "Kick reason is null"));

            case "ban" -> messageReturn = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Honeypot.getPlugin().getConfig().getString("ban-reason"), "Ban reason is null"));

            case "warn" -> messageReturn = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Honeypot.getPlugin().getConfig().getString("warn-message"), "Warn message is null"));

            default -> {
                //Do nothing
            }
        }

        return messageReturn;
    }
}
