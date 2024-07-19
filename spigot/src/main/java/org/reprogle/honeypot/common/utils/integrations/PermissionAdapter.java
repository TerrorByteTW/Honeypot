package org.reprogle.honeypot.common.utils.integrations;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.reprogle.honeypot.Honeypot;

public class PermissionAdapter {

    private final Permission permission;

    /**
     * Retrieve the permission service provider object
     * @param plugin {@link Honeypot}
     */
    public PermissionAdapter(Honeypot plugin) {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp != null ? rsp.getProvider() : null;
    }

    /**
     * Get the permission object
     *
     * @return {@link Permission}
     */
    public Permission getPermissionProvider() {
        return permission;
    }
}
