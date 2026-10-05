/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022 - 2022-2026 TerrorByte and Honeypot Contributors.
 *
 * This program is free software: You can redistribute it and/or modify it under
 *  the terms of the Mozilla Public License 2.0 as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including,
 * without limitation, warranties that the Covered Software is free of defects, merchantable,
 * fit for a particular purpose or non-infringing. See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.utils;

import com.google.inject.Inject;
import dev.dejvokep.boostedyaml.YamlDocument;
import me.clip.placeholderapi.PlaceholderAPI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.BehaviorProcessor;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.api.events.TriggerType;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.store.HoneypotPlayerHistoryManager;
import org.reprogle.honeypot.common.store.HoneypotPlayerManager;
import org.reprogle.honeypot.common.store.HoneypotRegionManager;
import org.reprogle.honeypot.common.utils.discord.DiscordWebhookNotifier;
import org.reprogle.honeypot.common.utils.discord.WebhookActionType;
import org.reprogle.honeypot.common.utils.integrations.AdapterManager;

import java.util.List;

public class ActionHandler {
    private static final String BREAK_PERMISSION = "honeypot.break";
    private static final String WILDCARD_PERMISSION = "honeypot.*";
    private static final String EXEMPT_PERMISSION = "honeypot.exempt";

    private final JavaPlugin plugin;
    private final HoneypotPlayerManager playerManager;
    private final HoneypotPlayerHistoryManager playerHistoryManager;
    private final HoneypotLogger logger;
    private final BytePluginConfig config;
    private final CommandFeedback commandFeedback;
    private final AdapterManager adapterManager;
    private final HoneypotRegionManager regionManager;

    private final MiniMessage mm = MiniMessage.miniMessage();

    @Inject
    public ActionHandler(JavaPlugin plugin, HoneypotLogger logger, HoneypotPlayerManager playerManager, HoneypotPlayerHistoryManager playerHistoryManager, BytePluginConfig config, CommandFeedback commandFeedback, AdapterManager adapterManager, HoneypotRegionManager regionManager) {
        this.plugin = plugin;
        this.regionManager = regionManager;
        this.logger = logger;
        this.playerManager = playerManager;
        this.playerHistoryManager = playerHistoryManager;
        this.config = config;
        this.commandFeedback = commandFeedback;
        this.adapterManager = adapterManager;
    }

    /**
     * The outcome of {@link #checkAndHandle(Player, Block, TriggerType)}
     */
    public enum TriggerResult {
        /**
         * The player is exempt from this trigger type. Nothing was counted, logged, or run
         */
        EXEMPT,
        /**
         * The Honeypot has no action, so nothing was counted, logged, or run
         */
        NO_ACTION,
        /**
         * The trigger was counted and logged, but the player hasn't reached the trigger limit, so the action wasn't run
         */
        COUNTED,
        /**
         * The player reached the trigger limit. The trigger was counted and logged, and the action was run
         */
        ACTION_TAKEN
    }

    /**
     * Checks whether a player is exempt from having a trigger counted or an action run against them.
     * Players with {@code honeypot.exempt}, {@code honeypot.*}, or op are exempt from every trigger type, and players
     * with {@code honeypot.break} are exempt from {@link TriggerType#BREAK}.
     *
     * @param player      The player to check
     * @param triggerType The type of trigger
     * @return True if the player is exempt
     */
    public boolean isExempt(Player player, TriggerType triggerType) {
        return player.hasPermission(EXEMPT_PERMISSION) || player.hasPermission(WILDCARD_PERMISSION) || player.isOp()
            || (triggerType == TriggerType.BREAK && player.hasPermission(BREAK_PERMISSION));
    }

    /**
     * Processes a player triggering a Honeypot. Unless the player is exempt, this counts the trigger, records it in the
     * player's history, and sends a Discord webhook if configured. If the player has reached the trigger limit for the
     * trigger type, it also runs the Honeypot's action, updates their lifetime stats, and resets their count.
     * <p>
     * This does not fire any Honeypot API events or cancel the underlying Bukkit event; that is left to the caller.
     *
     * @param player      The player that triggered the Honeypot
     * @param block       The Honeypot block that was triggered
     * @param triggerType The type of trigger. Must not be {@link TriggerType#NON_PLAYER}
     * @return The outcome of the trigger
     */
    public TriggerResult checkAndHandle(Player player, Block block, TriggerType triggerType) {
        return checkAndHandle(player, block, block.getType(), triggerType);
    }

    /**
     * Same as {@link #checkAndHandle(Player, Block, TriggerType)}, but records {@code blockType} in the player's history
     * rather than the block's current type. Use this when the Honeypot block has already been destroyed, such as a torch
     * that broke because the block it was attached to was broken.
     *
     * @param player      The player that triggered the Honeypot
     * @param block       The Honeypot block that was triggered
     * @param blockType   The type of the block when it was triggered
     * @param triggerType The type of trigger. Must not be {@link TriggerType#NON_PLAYER}
     * @return The outcome of the trigger
     */
    public TriggerResult checkAndHandle(Player player, Block block, Material blockType, TriggerType triggerType) {
        if (triggerType == TriggerType.NON_PLAYER)
            throw new IllegalArgumentException("NON_PLAYER triggers aren't tracked against players");

        if (isExempt(player, triggerType)) {
            logger.debug(Component.text("Player " + player.getName() + " is exempt from Honeypot triggers of type " + triggerType + ", nothing was counted or logged"));
            return TriggerResult.EXEMPT;
        }

        String action = regionManager.getAction(block);
        if (action == null) {
            logger.debug(Component.text("A " + triggerType + " trigger was called for player: " + player.getName() + ", UUID of " + player.getUniqueId() + ". However, the action was null, so this must be a FAKE HONEYPOT. Please investigate the block at " + block.getX() + ", " + block.getY() + ", " + block.getZ()));
            return TriggerResult.NO_ACTION;
        }

        // Outdated player stores can't count this trigger type, so take action immediately like older versions did
        int limit = playerManager.canCount(triggerType) ? getTriggerLimit(triggerType) : 1;
        int count = playerManager.getCount(player, triggerType) + 1;

        playerManager.addPlayer(player, triggerType, 1);

        if (limit > 1 && count < limit) {
            logger.debug(Component.text("Player " + player.getName() + " is at " + count + "/" + limit + " " + triggerType + " triggers, counting it without taking action"));
            playerHistoryManager.addPlayerHistory(player, block, blockType, action, historyType(triggerType, false));

            // Don't send on "onaction", otherwise the notification would incorrectly be tagged as an action
            if (config.config().getString("discord.send-when").equalsIgnoreCase("onbreak"))
                sendWebhook(player, block, triggerType, false);

            return TriggerResult.COUNTED;
        }

        logger.debug(Component.text("Player " + player.getName() + " has reached the " + triggerType + " trigger limit, taking action against them"));
        playerManager.playerTriggeredAction(player);
        playerManager.resetPlayerCount(player, triggerType);
        playerHistoryManager.addPlayerHistory(player, block, blockType, action, historyType(triggerType, true));

        handle(action, block, player);
        logger.debug(Component.text("Action successfully taken for block " + block + " on player " + player.getName() + " via " + triggerType + " trigger"));

        sendWebhook(player, block, triggerType, true);
        return TriggerResult.ACTION_TAKEN;
    }

    /**
     * Gets the configured number of triggers a player may cause before action is taken
     *
     * @param triggerType The type of trigger
     * @return The configured limit
     */
    public int getTriggerLimit(TriggerType triggerType) {
        return config.config().getInt(triggerLimitPath(triggerType), 1);
    }

    /**
     * Gets the config path of the trigger limit for a trigger type
     *
     * @param triggerType The trigger type
     * @return The config path holding that trigger type's limit
     */
    public static String triggerLimitPath(TriggerType triggerType) {
        return switch (triggerType) {
            case BREAK -> "trigger-limits.blocks-broken";
            case INVENTORY_OPEN -> "trigger-limits.inventories-opened";
            case INVENTORY_INTERACT -> "trigger-limits.inventories-interacted";
            case GENERIC, NON_PLAYER -> "trigger-limits.generic";
        };
    }

    private static String historyType(TriggerType triggerType, boolean actionTaken) {
        // BREAK keeps its original names so existing history (and DropBlockCountColumn06) stays consistent
        String name = switch (triggerType) {
            case BREAK -> "break";
            case INVENTORY_OPEN -> "inventoryOpen";
            case INVENTORY_INTERACT -> "inventoryInteract";
            case GENERIC, NON_PLAYER -> "generic";
        };

        return actionTaken ? name : "prelim" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private void sendWebhook(Player player, Block block, TriggerType triggerType, boolean actionTaken) {
        if (!config.config().getBoolean("discord.enable")) return;

        WebhookActionType webhookType;
        if (actionTaken && !config.config().getString("discord.send-when").equalsIgnoreCase("onbreak")) {
            webhookType = WebhookActionType.ACTION;
        } else {
            webhookType = triggerType == TriggerType.BREAK ? WebhookActionType.BREAK : WebhookActionType.TRIGGER;
        }

        new DiscordWebhookNotifier(webhookType, config.config().getString("discord.url"), block, player, logger).send();
    }

    public void handle(String action, Block block, Player player) {

        logger.debug(Component.text("Handling action " + action + " for player " + player.getName() + " at location " + block.getLocation()));

        // Behavior providers take higher precedence over custom config actions.
        if (Registry.getBehaviorRegistry().getBehaviorProvider(action) != null) {
            BehaviorProcessor.process(Registry.getBehaviorRegistry().getBehaviorProvider(action), player, block, config.yaml(action));
            return;
        }

        // The default path is likely due to custom actions. Run whatever the action was
        YamlDocument config = this.config.require("honeypots");
        if (config.contains(action)) {
            List<String> commands = config.getStringList(action + ".commands");
            List<String> permissionsAdd = config.getStringList(action + ".permissions-add");
            List<String> permissionsRemove = config.getStringList(action + ".permissions-remove");
            List<String> broadcasts = config.getStringList(action + ".broadcasts");
            List<String> messages = config.getStringList(action + ".messages");

            if (!commands.isEmpty()) {
                for (String command : commands) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                        PlainTextComponentSerializer.plainText().serialize(formatMessage(command, block, player, true)));
                }
            }

            if (!messages.isEmpty()) {
                for (String message : messages) {
                    player.sendMessage(formatMessage(message, block, player, false));
                }
            }

            if (!broadcasts.isEmpty()) {
                for (String broadcast : broadcasts) {
                    plugin.getServer().broadcast(formatMessage(broadcast, block, player, false));
                }
            }

            if (adapterManager.getPermissions() != null) {
                if (!permissionsAdd.isEmpty()) {
                    for (String permission : permissionsAdd) {
                        adapterManager.getPermissions().getPermissionProvider().playerAdd(null, player, permission);
                    }
                }

                if (!permissionsRemove.isEmpty()) {
                    for (String permission : permissionsRemove) {
                        adapterManager.getPermissions().getPermissionProvider().playerRemove(null, player, permission);
                    }
                }
            }
            // I'd like to warn them if they tried to adjust permissions without Vault. If Vault is null, and they
            // *didn't* try to adjust permissions, then who cares?
            else if (!permissionsAdd.isEmpty() || !permissionsRemove.isEmpty()) {
                logger.warning(commandFeedback.getChatPrefix().append(Component.text("Vault is not installed, Honeypots that modify permissions won't work. Please download here: https://www.spigotmc.org/resources/vault.34315/", NamedTextColor.RED)));
            }
        } else {
            logger.warning(Component.text("A Honeypot tried to run using action: " + action + ", but that action doesn't exist! Please verify your honeypots.yml config"));
        }
    }

    private Component formatMessage(String message, Block block, Player player, boolean command) {
        String formattedString = message.replace("%player%", player.getName())
            .replace("%pLocation%", player.getLocation().getX() + " " + player.getLocation().getY() + " " + player.getLocation().getZ())
            .replace("%bLocation%", block.getLocation().getX() + " " + block.getLocation().getY() + " " + block.getLocation().getZ())
            .replace("%world%", block.getLocation().getWorld().getName());

        // Support for Placeholder API, this will parse any remaining placeholders in the message
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            formattedString = PlaceholderAPI.setPlaceholders(player, formattedString);

        return command ? Component.text(formattedString) : Component.text().append(mm.deserialize(formattedString)).build();
    }
}
