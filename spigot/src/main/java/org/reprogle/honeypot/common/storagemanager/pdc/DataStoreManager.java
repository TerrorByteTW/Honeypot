package org.reprogle.honeypot.common.storagemanager.pdc;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.CacheManager;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockObject;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

@SuppressWarnings("java:S1192")
public class DataStoreManager {

    private final HoneypotLogger logger;
    Honeypot plugin;

    @Inject
    public DataStoreManager(Honeypot plugin, HoneypotLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public void createHoneypotBlock(Block block, String action) {
        block.getWorld().getPersistentDataContainer().set(formatKey(block), PersistentDataType.STRING, action);
    }

    public void deleteBlock(Block block) {
        block.getWorld().getPersistentDataContainer().remove(formatKey(block));
    }

    public boolean isHoneypotBlock(Block block) {
        return block.getWorld().getPersistentDataContainer().has(formatKey(block));
    }

    public NamespacedKey formatKey(Block block) {
        String coordinates = block.getX() + "_" + block.getY() + "_" + block.getZ();
        return new NamespacedKey(plugin, "honeypot-" + coordinates);
    }

    public HoneypotBlockObject getHoneypotBlock(Block block) {

        if (Boolean.TRUE.equals(isHoneypotBlock(block)))
            return new HoneypotBlockObject(block, getAction(block));

        return null;
    }

    public String getAction(Block block) {
        return block.getWorld().getPersistentDataContainer().get(formatKey(block), PersistentDataType.STRING);
    }

    public void deleteAllHoneypotBlocks(World world) {
        Set<NamespacedKey> keys = world.getPersistentDataContainer().getKeys();

        for (NamespacedKey key : keys) {
            if (key.toString().startsWith("honeypot-"))
                world.getPersistentDataContainer().remove(key);
        }

        CacheManager.clearCache();

        logger.debug(Component.text("Deleted all Honeypot blocks!"));
    }

    public List<HoneypotBlockObject> getAllHoneypots(World world) {
        List<HoneypotBlockObject> blocks = new ArrayList<>();
        Set<NamespacedKey> keys = world.getPersistentDataContainer().getKeys();

        for (NamespacedKey key : keys) {
            if (key.toString().startsWith("honeypot-")) {
                String coordinatesRaw = key.toString().split("honeypot-")[1];
                String coordinates = coordinatesRaw.replace("_", ", ");

                blocks.add(new HoneypotBlockObject(world.getName(), coordinates,
                        world.getPersistentDataContainer().get(key, PersistentDataType.STRING)));
            }
        }

        return blocks;
    }
}
