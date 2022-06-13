package org.reprogle.honeypot.commands;

import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

public interface HoneypotSubCommand {

    /**
     * Get's the name of the command
     * @return The String name
     */
    public abstract String getName();

    /**
     * Performs the command
     * @param p The Player running the command
     * @param args Any arguments to pass
     * @throws IOException Throws if any IO actions fail inside the perform command (Such as DB calls)
     */
    public abstract void perform(Player p, String[] args) throws IOException;

    /**
     * Gets all subcommands of the main command if any (Such as with the create or remove command)
     * @param p The Player running the command
     * @param args Any arguments to pass
     * @return A list of all subcommands as strings
     */
    public abstract List<String> getSubcommands(Player p, String[] args);

}
