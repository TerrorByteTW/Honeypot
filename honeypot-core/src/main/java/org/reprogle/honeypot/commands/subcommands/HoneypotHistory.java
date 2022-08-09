package org.reprogle.honeypot.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.commands.HoneypotSubCommand;

public class HoneypotHistory implements HoneypotSubCommand{

    @Override
    public String getName() {
        return "history";
    }

    @Override
    public void perform(Player p, String[] args) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        List<String> subcommands = new ArrayList<>();

        if (args.length == 2) {
            subcommands.add("delete");
            subcommands.add("query");
        }

        return subcommands;
    }
    
}
