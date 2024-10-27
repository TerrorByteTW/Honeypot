/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright TerrorByte & Honeypot Contributors (c) 2022 - 2024
 *
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.storagemanager.pdc;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.storagemanager.CacheManager;
import org.reprogle.honeypot.common.storageproviders.HoneypotBlockObject;
import org.reprogle.honeypot.common.storageproviders.Storage;
import org.reprogle.honeypot.common.storageproviders.StorageProvider;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import javax.annotation.Nullable;

@SuppressWarnings("java:S1192")
@Storage(name = "pdc")
public class DataStoreManager extends StorageProvider {

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

    public void removeHoneypotBlock(Block block) {
        block.getWorld().getPersistentDataContainer().remove(formatKey(block));
    }

    public boolean isHoneypotBlock(Block block) {
        return block.getWorld().getPersistentDataContainer().has(formatKey(block));
    }

    public NamespacedKey formatKey(Block block) {
        String coordinates = block.getX() + "_" + block.getY() + "_" + block.getZ();
        return new NamespacedKey(plugin, "honeypot-container-" + coordinates);
    }

    public HoneypotBlockObject getHoneypotBlock(Block block) {

        if (Boolean.TRUE.equals(isHoneypotBlock(block)))
            return new HoneypotBlockObject(block, getAction(block));

        return null;
    }

    public String getAction(Block block) {
        return block.getWorld().getPersistentDataContainer().get(formatKey(block), PersistentDataType.STRING);
    }

    public void deleteAllHoneypotBlocks(@Nullable World world) {
        Set<NamespacedKey> keys = world.getPersistentDataContainer().getKeys();

        for (NamespacedKey key : keys) {
            if (key.getKey().startsWith("honeypot-container-"))
                world.getPersistentDataContainer().remove(key);
        }

        CacheManager.clearCache();

        logger.debug(Component.text("Deleted all Honeypot blocks!"));
    }

    public List<HoneypotBlockObject> getAllHoneypots(World world) {
        List<HoneypotBlockObject> blocks = new ArrayList<>();
        Set<NamespacedKey> keys = world.getPersistentDataContainer().getKeys();

        for (NamespacedKey key : keys) {
            if (key.getKey().startsWith("honeypot-container-")) {
                String coordinatesRaw = key.getKey().split("honeypot-container-")[1];
                String coordinates = coordinatesRaw.replace("_", ", ");

                blocks.add(new HoneypotBlockObject(world.getName(), coordinates,
                        world.getPersistentDataContainer().get(key, PersistentDataType.STRING)));
            }
        }

        return blocks;
    }

    public List<HoneypotBlockObject> getNearbyHoneypots(Location location, int radius) {
        List<HoneypotBlockObject> honeypots = new ArrayList<>();

        for (HoneypotBlockObject honeypot : getAllHoneypots(location.getWorld())) {
            if (honeypot.getLocation().distance(location) <= radius) {
                honeypots.add(honeypot);
            }
        }

        return honeypots;
    }
}
