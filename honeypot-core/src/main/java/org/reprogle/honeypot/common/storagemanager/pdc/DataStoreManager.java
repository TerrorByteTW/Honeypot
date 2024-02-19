package org.reprogle.honeypot.common.storagemanager.pdc;

import org.bukkit.NamespacedKey;
import org.reprogle.honeypot.Honeypot;

public class DataStoreManager {
    
    Honeypot plugin;

    public DataStoreManager(Honeypot instance) {
        NamespacedKey key = new NamespacedKey(instance, "honeypot");
        plugin = instance;
    }

}
