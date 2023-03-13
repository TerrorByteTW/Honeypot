package org.reprogle.honeypot.utils;

/**
 * A class used for writing and managing permissions better. This class does not
 * yet have the ability to handle exlusivity, but I'm working on that. I've put
 * the permissions in a class to add features later
 */
public class HoneypotPermission {

    private final String permission;

    public HoneypotPermission(String permission) {
        this.permission = permission;
    }

    /**
     * Get the string of the permission required
     * 
     * @return Permission string
     */
    public String getPermission() {
        return permission;
    }

}
