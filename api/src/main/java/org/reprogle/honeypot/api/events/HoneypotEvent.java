package org.reprogle.honeypot.api.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HoneypotEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Boilerplate function for Bukkit
     *
     * @return HandlerList
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Boilerplate function for Bukkit
     *
     * @return HandlerList
     */
    @SuppressWarnings("java:S4144")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}