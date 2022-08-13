package org.reprogle.honeypot.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.HoneypotConfigManager;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.commands.HoneypotSubCommand;
import org.reprogle.honeypot.storagemanager.HoneypotPlayerHistoryObject;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

@SuppressWarnings({"java:S1192", "java:S3776"})
public class HoneypotHistory implements HoneypotSubCommand{

    @Override
    public String getName() {
        return "history";
    }

    @Override
    public void perform(Player p, String[] args) throws IOException {
        if (!p.hasPermission("honeypot.history")) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        if (args.length >= 3 && args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("query")) {
            Player argPlayer = Bukkit.getPlayer(args[2]);

            if (argPlayer == null || !Bukkit.getPlayer(args[2]).isOnline()) {
                p.sendMessage(CommandFeedback.sendCommandFeedback("notonline"));
                return;
            }

            if (args[1].equalsIgnoreCase("query")) {
                p.sendMessage(CommandFeedback.sendCommandFeedback("searching"));

                List<HoneypotPlayerHistoryObject> history = Honeypot.getPlayerHistoryManager().getPlayerHistory(argPlayer);
                int length = HoneypotConfigManager.getPluginConfig().getInt("history-length");

                if (history.size() > length) { 
                    p.sendMessage(CommandFeedback.sendCommandFeedback("truncating"));
                }

                if (history.isEmpty()) {
                    p.sendMessage(CommandFeedback.sendCommandFeedback("nohistory"));
                    return;
                }

                int limit = 0;

                if (history.size() > length) {
                    limit = length;
                } else {
                    limit = history.size();
                }

                for (int i = 0; i < limit; i++) {
                    p.sendMessage(ChatColor.GOLD + "\n-------[ " + ChatColor.WHITE + history.get(i).getDateTime() + ChatColor.GOLD + " ]-------");
                    TextComponent playerInfo = new TextComponent("Player: " + ChatColor.GOLD + history.get(i).getPlayer() + ChatColor.WHITE + " @ " + ChatColor.WHITE + ChatColor.GOLD + history.get(i).getHoneypot().getWorld() + " " + history.get(i).getHoneypot().getCoordinates());
                    playerInfo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hpteleport " + (history.get(i).getHoneypot().getLocation().getX() + 0.5) + " " + (history.get(i).getHoneypot().getLocation().getY() + 1) + " " + (history.get(i).getHoneypot().getLocation().getZ() + 0.5)));
                    playerInfo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + "" + ChatColor.ITALIC + "Click to teleport")));

                    p.spigot().sendMessage(playerInfo);
                    p.sendMessage("Action: " + ChatColor.GOLD + history.get(i).getHoneypot().getAction());
                    p.sendMessage(ChatColor.GOLD + "----------------------------------");
                }
                
            } else if (args[1].equalsIgnoreCase("delete")) {
                if (args.length >= 4) {
                    Honeypot.getPlayerHistoryManager().deletePlayerHistory(argPlayer, Integer.parseInt(args[3]));
                } else {
                    Honeypot.getPlayerHistoryManager().deletePlayerHistory(argPlayer);
                }
                p.sendMessage(CommandFeedback.sendCommandFeedback("success"));
            }
        } else if (args[1].equalsIgnoreCase("purge")) {
            Honeypot.getPlayerHistoryManager().deleteAllHistory();
            p.sendMessage(CommandFeedback.sendCommandFeedback("success"));
        } else {
            p.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
        }
        
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        List<String> subcommands = new ArrayList<>();

        // Base arguments
        if (args.length == 2) {
            subcommands.add("delete");
            subcommands.add("query");
            subcommands.add("purge");
        // If the args length is 3 and they passed a valid sub-subcommand (yikes), do this
        } else if (args.length == 3 && (args[1].equalsIgnoreCase("query") || args[1].equalsIgnoreCase("delete"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                subcommands.add(player.getName());
            }
        // If the args length is 4 and they typed delete, just give them a list of numbers
        } else if (args.length == 4 && args[1].equalsIgnoreCase("delete")) {
            for (int i = 1; i < 10; i++) {
                subcommands.add(Integer.toString(i));
            }
        }

        return subcommands;
    }
    
}
