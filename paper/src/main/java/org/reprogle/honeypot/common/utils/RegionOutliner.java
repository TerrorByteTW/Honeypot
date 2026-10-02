package org.reprogle.honeypot.common.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class RegionOutliner {
    public static void outlineBoundingBox(
        JavaPlugin plugin,
        Player player,
        Location pos1,
        Location pos2
    ) {
        if (pos1.getWorld() == null || pos2.getWorld() == null) return;
        if (!pos1.getWorld().equals(pos2.getWorld())) return;

        World world = pos1.getWorld();

        double minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        double minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        double minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());

        double maxX = Math.max(pos1.getBlockX(), pos2.getBlockX()) + 1;
        double maxY = Math.max(pos1.getBlockY(), pos2.getBlockY()) + 1;
        double maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ()) + 1;

        double step = 0.5;
        Particle particle = Particle.DUST;
        Particle.DustOptions dust = new Particle.DustOptions(Color.AQUA, 1.5f);

        ScheduledTask task = Bukkit.getAsyncScheduler().runAtFixedRate(
            plugin,
            _ -> {
                // X edges
                for (double x = minX; x <= maxX; x += step) {
                    spawnParticle(player, world, x, minY, minZ, particle, dust);
                    spawnParticle(player, world, x, minY, maxZ, particle, dust);
                    spawnParticle(player, world, x, maxY, minZ, particle, dust);
                    spawnParticle(player, world, x, maxY, maxZ, particle, dust);
                }

                // Y edges
                for (double y = minY; y <= maxY; y += step) {
                    spawnParticle(player, world, minX, y, minZ, particle, dust);
                    spawnParticle(player, world, minX, y, maxZ, particle, dust);
                    spawnParticle(player, world, maxX, y, minZ, particle, dust);
                    spawnParticle(player, world, maxX, y, maxZ, particle, dust);
                }

                // Z edges
                for (double z = minZ; z <= maxZ; z += step) {
                    spawnParticle(player, world, minX, minY, z, particle, dust);
                    spawnParticle(player, world, minX, maxY, z, particle, dust);
                    spawnParticle(player, world, maxX, minY, z, particle, dust);
                    spawnParticle(player, world, maxX, maxY, z, particle, dust);
                }
            },
            0L,
            300L,
            TimeUnit.MILLISECONDS
        );

        Bukkit.getScheduler().runTaskLater(plugin, task::cancel, 20L * 5);

        spawnSlime(plugin, world, minX, minY, minZ);
        spawnSlime(plugin, world, minX, minY, maxZ - 1);
        spawnSlime(plugin, world, minX, maxY - 1, minZ);
        spawnSlime(plugin, world, minX, maxY - 1, maxZ - 1);
        spawnSlime(plugin, world, maxX - 1, minY, minZ);
        spawnSlime(plugin, world, maxX - 1, minY, maxZ - 1);
        spawnSlime(plugin, world, maxX - 1, maxY - 1, minZ);
        spawnSlime(plugin, world, maxX - 1, maxY - 1, maxZ - 1);
    }

    private static void spawnParticle(
        Player player,
        World world,
        double x,
        double y,
        double z,
        Particle particle,
        Particle.DustOptions dust
    ) {
        player.spawnParticle(
            particle,
            new Location(world, x, y, z),
            1,
            0, 0, 0,
            0,
            dust
        );
    }

    public static void spawnSlime(
        JavaPlugin plugin,
        World world,
        double x,
        double y,
        double z
    ) {
        Slime slime = (Slime) world.spawnEntity(
            new Location(world, x + 0.5, y, z + 0.5),
            EntityType.SLIME
        );

        slime.setSize(2);
        slime.setAI(false);
        slime.setGlowing(true);
        slime.setInvulnerable(true);
        slime.setHealth(slime.getAttribute(Attribute.MAX_HEALTH).getValue());
        slime.setInvisible(true);

        // Remove the slime after 5 seconds
        // If we kill it, a death animation plays and the slime splits and drops items
        slime.getScheduler().runDelayed(
            plugin,
            _ -> slime.remove(),
            null,
            20L * 5
        );
    }
}
