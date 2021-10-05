package me.terrorbyte.honeypot.commands.subcommands;

import me.terrorbyte.honeypot.CustomBlockData;
import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.commands.CommandFeedback;
import me.terrorbyte.honeypot.commands.SubCommand;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.logging.Logger;

public class HoneypotRemove extends SubCommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Removes a honeypot";
    }

    @Override
    public String getSyntax() {
        return "/honeypot remove";
    }

    @Override
    public void perform(Player p, String[] args) {

        //Create a new NamedspaceKey called honeypot
        final NamespacedKey key = new NamespacedKey(Honeypot.getPlugin(), "honeypot");

        //Get the block data for the block the player is looking at
        Block block = p.getTargetBlock(null, 5);
        final PersistentDataContainer blockData = new CustomBlockData(block, Honeypot.getPlugin());

        //If it is a pot
        if(blockData.has(key, PersistentDataType.INTEGER) && blockData.get(key, PersistentDataType.INTEGER).equals((1))){
            blockData.remove(key);
            p.sendMessage(CommandFeedback.sendCommandFeedback("success", false));

            //If it is not a pot
        } else {
            p.sendMessage(CommandFeedback.sendCommandFeedback("notapot"));
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return null;
    }
}
