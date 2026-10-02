package org.reprogle.honeypot.common.commands;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.ProvidesIntoSet;
import org.reprogle.bytelib.commands.CommandFactory;
import org.reprogle.bytelib.commands.CommandRegistration;
import org.reprogle.bytelib.commands.dsl.*;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.common.commands.subcommands.*;

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
            .then(Create.commandTree(config, factory))
            .then(GUI.commandTree(config, factory))
            .then(Help.commandTree())
            .then(History.commandTree(factory))
            .then(Info.commandTree(factory))
            .then(List.commandTree(factory))
            .then(Locate.commandTree(factory))
            .then(Reload.commandTree(factory))
            .then(Remove.commandTree(factory))
            .executes(Help.class, factory);

        return new DslCommandRegistration(root);
    }

}
