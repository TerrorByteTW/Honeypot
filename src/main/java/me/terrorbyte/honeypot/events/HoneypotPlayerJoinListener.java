package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.HoneypotUpdateChecker;
import me.terrorbyte.honeypot.commands.HoneypotCommandFeedback;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class HoneypotPlayerJoinListener implements Listener {

    //Player join event
    @EventHandler
    public static void PlayerJoinEvent(PlayerJoinEvent event){
        Player p = event.getPlayer();
        if(p.hasPermission("honeypot.update") || p.hasPermission("honeypot.*") || p.isOp()){
            new HoneypotUpdateChecker(Honeypot.getPlugin(), "https://raw.githubusercontent.com/redstonefreak589/Honeypot/master/version.txt").getVersion(version -> {
                if (!Honeypot.getPlugin().getDescription().getVersion().equals(version)) {
                    p.sendMessage(HoneypotCommandFeedback.sendCommandFeedback("updateavailable"));
                }
            });
        }
    }

}
