package org.reprogle.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event that is fired each time a Honeypot is triggered/
 * This event is called <i>after</i> the Honeypot is triggered, not before.
 * This event is not cancellable. If you need to cancel it, use {@link HoneypotPreTriggerEvent}.
 */
public class HoneypotTriggerEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Entity entity;

    private final Player player;

    private final Block block;

    private final TriggerType trigger;

    /**
     * Called after action is taken on a player who interacted with the Honeypot. Non-cancellable.
     *
     * @param player The Player who broke with the Honeypot
     * @param block  The Honeypot block
     */
    public HoneypotTriggerEvent(Player player, Block block, TriggerType trigger) {
        this.entity = player;
        this.player = player;
        this.block = block;
        this.trigger = trigger;
    }

    /**
     * Called after action a trigger if caused by an entity, such as TNT.
     *
     * @param entity  The entity that triggered the Honeypot
     * @param block   The Honeypot block
     * @param trigger The type of trigger that caused the event
     */
    public HoneypotTriggerEvent(Entity entity, Block block, TriggerType trigger) {
        this.entity = entity;
        this.player = null;
        this.block = block;
        this.trigger = trigger;
    }

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

    /**
     * Get the player that broke the block
     *
     * @return {@link Player}
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the entity that triggered the Honeypot
     * @return {@link Entity}
     */
    @Nullable
    public Entity getEntity() {
        return entity;
    }

    /**
     * Get the block that was involved in the event
     *
     * @return {@link Block}
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Get the type of trigger for the event
     *
     * @return {@link TriggerType}
     */
    public TriggerType getTrigger() {
        return trigger;
    }

}
