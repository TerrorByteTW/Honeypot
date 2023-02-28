package org.reprogle.honeypot.events;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.utils.HoneypotUpdateChecker;

public class PlayerJoinEventListener implements Listener {

    /**
     * Create a private constructor to hide the implicit one
     */
    PlayerJoinEventListener() {

    }

    // Player join event
    @EventHandler(priority = EventPriority.LOWEST)
    public static void playerJoinEvent(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        if (p.hasPermission("honeypot.update") || p.hasPermission("honeypot.*") || p.isOp()) {
            new HoneypotUpdateChecker(Honeypot.getPlugin(),
                    "https://raw.githubusercontent.com/TerrorByteTW/Honeypot/master/version.txt").getVersion(latest -> {
                        if (Integer.parseInt(latest.replace(".", "")) > Integer
                                .parseInt(Honeypot.getPlugin().getDescription().getVersion().replace(".", ""))) {
                            TextComponent message = new TextComponent(
                                    CommandFeedback.sendCommandFeedback("updateavailable"));
                            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                    "https://github.com/TerrorByteTW/Honeypot"));
                            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new Text("Click me to download the latest update!")));
                            p.spigot().sendMessage(message);
                        }
                    });
        }
    }

}
