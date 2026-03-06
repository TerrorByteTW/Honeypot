package org.reprogle.honeypot.common.commands;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.ProvidesIntoSet;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.reprogle.bytelib.commands.CommandFactory;
import org.reprogle.bytelib.commands.CommandRegistration;
import org.reprogle.bytelib.commands.dsl.*;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.Registry;
import org.reprogle.honeypot.common.commands.subcommands.*;
import org.reprogle.honeypot.common.providers.BehaviorProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public final class CommandModule extends AbstractModule {

    @ProvidesIntoSet
    CommandRegistration command(BytePluginConfig config, CommandFactory factory) {
        LiteralNode root = CommandDsl.literal("honeypot")
            .requires(
                PermissionChecks.anyOf(
                    PermissionChecks.permission("honeypot.commands"),
                    PermissionChecks.consoleOnly()
                )
            )
            .then(
                CommandDsl.literal("create")
                    .requires(
                        PermissionChecks.allOf( // Requires that at least one of the permissions listed is owned, and that the sender is a player
                            PermissionChecks.anyOf(
                                PermissionChecks.permission("honeypot.create"),
                                PermissionChecks.permission("honeypot.*"),
                                PermissionChecks.isOp()
                            ),
                            PermissionChecks.playerOnly()
                        )
                    )
                    .then(
                        CommandDsl.argument("type", StringArgumentType.string())
                            .suggests(
                                Suggest.dynamic(
                                    (ctx, remaining) -> {
                                        // Get all registered behavior providers
                                        Collection<BrigadierSuggestion> suggestions = new ArrayList<>();
                                        ConcurrentMap<String, BehaviorProvider> map = Registry.getBehaviorRegistry().getBehaviorProviders();
                                        map.forEach((providerName, provider) -> suggestions.add(Suggest.suggestion(providerName)));

                                        // Get all custom honeypots in honeypots.yml
                                        Set<Object> keys = config.require("honeypots").getKeys();
                                        keys.forEach(key -> suggestions.add(Suggest.suggestion(key.toString())));

                                        return suggestions;
                                    }
                                )
                            )
                            .executes(HoneypotCreate.class, factory)
                    )
                    .executes(HoneypotHelp.class, factory)
            )
            .then(
                CommandDsl.literal("gui")
                    .requires(
                        PermissionChecks.allOf(
                            PermissionChecks.anyOf(
                                PermissionChecks.permission("honeypot.gui"),
                                PermissionChecks.permission("honeypot.*"),
                                PermissionChecks.isOp()
                            ),
                            PermissionChecks.playerOnly()
                        )
                    )
                    .executes(HoneypotGUI.class, factory)
            )
            .then(
                CommandDsl.literal("help")
            )
            .then(
                CommandDsl.literal("history")
                    .requires(
                        PermissionChecks.anyOf(
                            PermissionChecks.permission("honeypot.history"),
                            PermissionChecks.permission("honeypot.*"),
                            PermissionChecks.isOp()
                        )
                    )
                    .then(
                        CommandDsl.argument("action", StringArgumentType.string())
                            .suggests(
                                Suggest.fixedWithTooltip(List.of(
                                        Suggest.suggestion("delete", Component.text("Delete history record for a player")),
                                        Suggest.suggestion("query", Component.text("Query history for a player")),
                                        Suggest.suggestion("purge", Component.text("Purges all history for all players"))
                                    )
                                )
                            )
                            .then(
                                CommandDsl.argument("player", ArgumentTypes.player())
                                    .suggests(
                                        Suggest.dynamic(
                                            (ctx, remaining) ->
                                                Bukkit.getOnlinePlayers()
                                                    .stream()
                                                    .filter(player -> player.getName().startsWith(remaining))
                                                    .map(player -> Suggest.suggestion(player.getName()))
                                                    .toList()
                                        )
                                    )
                                    .then(
                                        CommandDsl.argument("count", IntegerArgumentType.integer(1, 100000))
                                    )
                            )
                            .executes(HoneypotHistory.class, factory))
            )
            .then(
                CommandDsl.literal("info")
                    .executes(HoneypotInfo.class, factory)
            )
            .then(
                CommandDsl.literal("list")
                    .requires(
                        PermissionChecks.allOf(
                            PermissionChecks.anyOf(
                                PermissionChecks.permission("honeypot.gui"),
                                PermissionChecks.permission("honeypot.*"),
                                PermissionChecks.isOp()
                            ),
                            PermissionChecks.playerOnly()
                        )
                    )
                    .executes(HoneypotList.class, factory)
            )
            .then(
                CommandDsl.literal("locate")
                    .requires(
                        PermissionChecks.allOf(
                            PermissionChecks.anyOf(
                                PermissionChecks.permission("honeypot.locate"),
                                PermissionChecks.permission("honeypot.*"),
                                PermissionChecks.isOp()
                            ),
                            PermissionChecks.playerOnly()
                        )
                    )
                    .then(
                        CommandDsl.argument("radius", IntegerArgumentType.integer(1))
                    )
                    .executes(HoneypotLocate.class, factory)
            )
            .then(
                CommandDsl.literal("migrate")
                    .requires(
                        PermissionChecks.anyOf(
                            PermissionChecks.permission("honeypot.migrate"),
                            PermissionChecks.permission("honeypot.*"),
                            PermissionChecks.isOp()
                        )
                    )
                    .then(
                        CommandDsl.argument("confirmation", BoolArgumentType.bool())
                    )
                    .executes(HoneypotMigrate.class, factory)
            )
            .then(
                CommandDsl.literal("reload")
                    .requires(
                        PermissionChecks.anyOf(
                            PermissionChecks.permission("honeypot.reload"),
                            PermissionChecks.permission("honeypot.*"),
                            PermissionChecks.isOp()
                        )
                    )
                    .executes(HoneypotReload.class, factory)
            )
            .then(
                CommandDsl.literal("remove")
                    .requires(
                        PermissionChecks.allOf(
                            PermissionChecks.anyOf(
                                PermissionChecks.permission("honeypot.remove"),
                                PermissionChecks.permission("honeypot.*"),
                                PermissionChecks.isOp()
                            ),
                            PermissionChecks.playerOnly()
                        )
                    )
                    .then(
                        CommandDsl.argument("qualifier", StringArgumentType.string())
                            .suggests(Suggest.fixed("all", "near"))
                    )
                    .executes(HoneypotRemove.class, factory)
            )
            .executes(HoneypotHelp.class, factory);

        return new DslCommandRegistration(root);
    }

}
