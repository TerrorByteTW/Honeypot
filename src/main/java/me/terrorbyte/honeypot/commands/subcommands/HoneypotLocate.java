package me.terrorbyte.honeypot.commands.subcommands;

import me.terrorbyte.honeypot.commands.HoneypotSubCommand;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

import java.util.List;

public class HoneypotLocate extends HoneypotSubCommand {
    @Override
    public String getName() {
        return "locate";
    }

    @Override
    public String getDescription() {
        return "Locates nearby honeypot blocks";
    }

    @Override
    public String getSyntax() {
        return "/honeypot locate";
    }

    @Override
    public void perform(Player p, String[] args) {
        final double radius = 10d;
        final double xCoord = p.getLocation().getX();
        final double yCoord = p.getLocation().getY();
        final double zCoord = p.getLocation().getZ();

        //TODO - Test this code to see if it actually works lmao
        for (double x = xCoord - radius; x < xCoord + radius; x++) {
            for (double y = yCoord - radius; y < yCoord + radius; y++) {
                for (double z = zCoord - radius; z < zCoord + radius; z++) {
                    final Block b = new Location(p.getWorld(), x, y, z).getBlock();
                    if (HoneypotBlockStorageManager.isHoneypotBlock(b)){
                        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(252, 186, 3), 1.0F);
                        for (int i = 1; i <= 10; i++) {
                            p.getWorld().spawnParticle(Particle.REDSTONE, b.getLocation().add(0, i, 0), 10, dustOptions);
                            p.getWorld().spawnParticle(Particle.REDSTONE, b.getLocation().add(0, -i, 0), 10, dustOptions);
                            p.getWorld().spawnParticle(Particle.REDSTONE, b.getLocation().add(i, 0, 0), 10, dustOptions);
                            p.getWorld().spawnParticle(Particle.REDSTONE, b.getLocation().add(-i, 0, 0), 10, dustOptions);
                            p.getWorld().spawnParticle(Particle.REDSTONE, b.getLocation().add(0, 0, i), 10, dustOptions);
                            p.getWorld().spawnParticle(Particle.REDSTONE, b.getLocation().add(0, 0, -i), 10, dustOptions);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return null;
    }
}
