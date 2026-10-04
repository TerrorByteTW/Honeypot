package org.reprogle.honeypot.api.events;

import org.bukkit.event.Event;

/**
 * Base class for Honeypot events.
 * <p>
 * Bukkit requires every event class to declare its own static {@code HandlerList} and {@code getHandlerList()}, so
 * each subclass must provide them itself. A shared list here would register every listener of every subclass into the
 * same list.
 */
public abstract class HoneypotEvent extends Event {
}
