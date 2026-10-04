package org.reprogle.honeypot.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HoneypotPreTriggerEvent extends HoneypotEvent implements Cancellable {

    private boolean isCancelled;

    private final Entity entity;

    private final Player player;

    private final Block block;

    private final TriggerType trigger;

    /**
     * Called before action is taken on a player who triggered a Honeypot. If canceled, the Honeypot is
     * ignored
     *
     * @param player The Player breaking with the Honeypot
     * @param block  The Honeypot block
     */
    public HoneypotPreTriggerEvent(Player player, Block block, TriggerType trigger) {
        this.player = player;
        this.entity = player;
        this.block = block;
        this.trigger = trigger;
    }

    /**
     * Called before a trigger is processed for an entity. May be used for Players, but is primarily used for non-players
     *
     * @param entity  The entity triggering the Honeypot
     * @param block   The Honeypot block
     * @param trigger The type of trigger
     */
    public HoneypotPreTriggerEvent(Entity entity, Block block, TriggerType trigger) {
        if (entity instanceof Player p)
            this.player = p;
        else
            this.player = null;

        this.entity = entity;
        this.block = block;
        this.trigger = trigger;
    }

    /**
     * Called before a trigger is processed for a Honeypot, but the source of the trigger is unknown, such as blocks burning.
     *
     * @param block The Honeypot block
     * @param trigger The type of trigger
     */
    public HoneypotPreTriggerEvent(Block block, TriggerType trigger) {
        this.player = null;
        this.entity = null;
        this.block = block;
        this.trigger = trigger;
    }

    /**
     * Get the player that broke the block
     *
     * @return {@link Player}, or null if the Honeypot was not triggered by a player
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the entity that triggered the Honeypot
     *
     * @return {@link Entity}, or null if the trigger has no known source (e.g., TNT lit by redstone)
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
     * Get the type of trigger that caused the event
     *
     * @return {@link TriggerType}
     */
    public TriggerType getTrigger() {
        return trigger;
    }

    /**
     * Check if the event is canceled
     *
     * @return True if canceled, false if not
     */
    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Set the event as canceled or not
     *
     * @param cancel Boolean value notating if the event is canceled or not
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

}
