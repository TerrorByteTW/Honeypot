/*
 * Honeypot is a tool for griefing auto-moderation
 *
 * Copyright TerrorByte (c) 2024
 * Copyright Honeypot Contributors (c) 2024
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
