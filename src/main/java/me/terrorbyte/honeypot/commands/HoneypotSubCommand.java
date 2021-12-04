package me.terrorbyte.honeypot.commands;

import org.bukkit.entity.Player;

import java.util.List;

public abstract class HoneypotSubCommand {

    public abstract String getName();

    public abstract void perform(Player p, String[] args);
    public abstract List<String> getSubcommands(Player p, String[] args);

}
