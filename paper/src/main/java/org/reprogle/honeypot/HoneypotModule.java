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

package org.reprogle.honeypot;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.commands.subcommands.*;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.providers.included.Ban;
import org.reprogle.honeypot.common.providers.included.Kick;
import org.reprogle.honeypot.common.providers.included.Notify;
import org.reprogle.honeypot.common.providers.included.Warn;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

import java.io.File;

public class HoneypotModule extends AbstractModule {

    private final Honeypot plugin;
    private final HoneypotBlockManager blockManager;
    private final HoneypotConfigManager configManager;
    private final CommandFeedback commandFeedback;

    @Override
    protected void configure() {
        // The lifeline of the entire DI system is the plugin object itself
        bind(Honeypot.class).toInstance(plugin);
        bind(HoneypotConfigManager.class).toInstance(configManager);
        bind(CommandFeedback.class).toInstance(commandFeedback);

        // We need a HoneypotBlockManager tied to a specific instance since that instance has specific settings based on configuration.
        bind(HoneypotBlockManager.class).toInstance(blockManager);

        // Bind all the behavior providers
        Multibinder<BehaviorProvider> behaviorBinder = Multibinder.newSetBinder(binder(), BehaviorProvider.class);
        behaviorBinder.addBinding().to(Ban.class);
        behaviorBinder.addBinding().to(Warn.class);
        behaviorBinder.addBinding().to(Kick.class);
        behaviorBinder.addBinding().to(Notify.class);
        
        // Bind all commands
        Multibinder<HoneypotSubCommand> subcommandBinder = Multibinder.newSetBinder(binder(), HoneypotSubCommand.class);
        subcommandBinder.addBinding().to(HoneypotCreate.class);
        subcommandBinder.addBinding().to(HoneypotRemove.class);
        subcommandBinder.addBinding().to(HoneypotReload.class);
        subcommandBinder.addBinding().to(HoneypotLocate.class);
        subcommandBinder.addBinding().to(HoneypotGUI.class);
        subcommandBinder.addBinding().to(HoneypotHelp.class);
        subcommandBinder.addBinding().to(HoneypotInfo.class);
        subcommandBinder.addBinding().to(HoneypotHistory.class);
        subcommandBinder.addBinding().to(HoneypotList.class);
        subcommandBinder.addBinding().to(HoneypotMigrate.class);

        // We only want this binding if debug for pdc is enabled
        if (configManager.getPluginConfig().getBoolean("enable-debug-mode")) {
            subcommandBinder.addBinding().to(HoneypotDebug.class);
        }

        // Not really necessary but cool and I'm learning :P
        bind(File.class).annotatedWith(Names.named("HoneypotLogFile")).toInstance(new File(plugin.getDataFolder(), "honeypot.log"));
    }

    public HoneypotModule(Honeypot plugin, HoneypotConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        configManager.setupConfig(plugin);

        this.blockManager = new HoneypotBlockManager(configManager.getPluginConfig().getString("storage-method"));
        this.commandFeedback = new CommandFeedback();
    }

    public Injector createInjector() {
        return Guice.createInjector(this);
    }


}
