package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.HoneypotUpdateChecker;
import me.terrorbyte.honeypot.commands.CommandFeedback;
import me.terrorbyte.honeypot.storagemanager.HoneypotPlayerStorageManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    /**
     * Create a private constructor to hide the implicit one
     */
    PlayerJoinListener() {

    }

    // Player join event
    @EventHandler
    public static void playerJoinEvent(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        // Convert player names to UUIDs
        int breaks = HoneypotPlayerStorageManager.getCount(p);
        if (breaks >= 0) {
            HoneypotPlayerStorageManager.setPlayerCount(p, breaks);
        }

        if (p.hasPermission("honeypot.update") || p.hasPermission("honeypot.*") || p.isOp()) {
            new HoneypotUpdateChecker(Honeypot.getPlugin(),
                    "https://raw.githubusercontent.com/TerrrorByte/Honeypot/master/version.txt").getVersion(version -> {
                        if (!Honeypot.getPlugin().getDescription().getVersion().equals(version)) {
                            TextComponent message = new TextComponent(
                                    CommandFeedback.sendCommandFeedback("updateavailable"));
                            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                    "https://github.com/TerrrorByte/Honeypot"));
                            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new Text("Click me to download the latest update!")));
                            p.spigot().sendMessage(message);
                        }
                    });
        }
    }

}
