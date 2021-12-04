package me.terrorbyte.honeypot.commands.subcommands;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.commands.HoneypotCommandFeedback;
import me.terrorbyte.honeypot.commands.HoneypotSubCommand;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;

public class HoneypotLocate extends HoneypotSubCommand {
    @Override
    public String getName() {
        return "locate";
    }

    @Override
    public void perform(Player p, String[] args) {

        //If the player has locate permissions, do this
        if(!(p.hasPermission("honeypot.locate"))) {
            p.sendMessage(HoneypotCommandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

        //Set a 5 block search radius
        final double radius = 5d;
        final double xCoord = p.getLocation().getX();
        final double yCoord = p.getLocation().getY();
        final double zCoord = p.getLocation().getZ();
        boolean potFound = false;

        //For every x value within 5 blocks
        for (double x = xCoord - radius; x < xCoord + radius; x++) {
            //For every y value within 5 blocks
            for (double y = yCoord - radius; y < yCoord + radius; y++) {
                //For every z value within 5 blocks
                for (double z = zCoord - radius; z < zCoord + radius; z++) {

                    //Check the block at coords x,y,z to see if it's a Honeypot
                    final Block b = new Location(p.getWorld(), x, y, z).getBlock();

                    //If it is a honeypot do this
                    if (HoneypotBlockStorageManager.isHoneypotBlock(b)) {
                        potFound = true;

                        //Create a dumb, invisible, invulnerable, block-sized glowing slime and spawn it inside the block
                        Slime slime = (Slime) Objects.requireNonNull(Bukkit.getWorld(b.getWorld().getName())).spawnEntity(b.getLocation().add(0.5, 0, 0.5), EntityType.SLIME);
                        slime.setSize(2);
                        slime.setAI(false);
                        slime.setGlowing(true);
                        slime.setInvulnerable(true);
                        slime.setHealth(4.0);
                        slime.setInvisible(true);

                        //After 5 seconds, remove the slime. Setting its health to 0 causes the death animation, removing it just makes it go away. Poof!
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                slime.remove();
                            }
                        }.runTaskLater(Honeypot.getPlugin(), 20 * 5);

                    }
                }
            }
        }

        //Let the player know if a pot was found or not
        if (potFound) {
            p.sendMessage(HoneypotCommandFeedback.sendCommandFeedback("foundpot"));
        } else {
            p.sendMessage(HoneypotCommandFeedback.sendCommandFeedback("nopotfound"));
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return null;
    }
}
