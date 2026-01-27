/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) TerrorByte and Honeypot Contributors 2022 - 2025.
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

package org.reprogle.honeypot;

import com.google.inject.*;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.reprogle.bytelib.boot.lifecycle.PluginLifecycle;
import org.reprogle.bytelib.commands.CommandRegistration;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotCommandRegistration;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.commands.subcommands.*;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.providers.included.Ban;
import org.reprogle.honeypot.common.providers.included.Kick;
import org.reprogle.honeypot.common.providers.included.Notify;
import org.reprogle.honeypot.common.providers.included.Warn;
import org.reprogle.honeypot.common.storagemanager.pdc.DataStoreManager;
import org.reprogle.honeypot.common.storagemanager.sqlite.HoneypotRepository;
import org.reprogle.honeypot.common.storageproviders.StorageProvider;

import java.io.File;
import java.nio.file.Path;

public class HoneypotModule extends AbstractModule {
    private final Path dataDir;

    public HoneypotModule(Path dataDir) {
        this.dataDir = dataDir;
    }

    @Override
    protected void configure() {
        bind(CommandFeedback.class).in(Singleton.class);

        // Bind all the behavior providers
        Multibinder<BehaviorProvider> behaviorBinder = Multibinder.newSetBinder(binder(), BehaviorProvider.class);
        behaviorBinder.addBinding().to(Ban.class);
        behaviorBinder.addBinding().to(Warn.class);
        behaviorBinder.addBinding().to(Kick.class);
        behaviorBinder.addBinding().to(Notify.class);

        Multibinder<StorageProvider> storageBinder = Multibinder.newSetBinder(binder(), StorageProvider.class);
        storageBinder.addBinding().to(HoneypotRepository.class);
        storageBinder.addBinding().to(DataStoreManager.class);

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

        // Commands
        Multibinder.newSetBinder(binder(), CommandRegistration.class)
                .addBinding()
                .to(HoneypotCommandRegistration.class);

        // Not really necessary but cool and I'm learning :P
        File file = new File(dataDir.toFile(), "honeypot.log");
        file.getParentFile().mkdirs();
        bind(File.class).annotatedWith(Names.named("HoneypotLogFile")).toInstance(file);

        Multibinder<PluginLifecycle> lifecycles = Multibinder.newSetBinder(binder(), PluginLifecycle.class);
        lifecycles.addBinding().to(HoneypotLifecycle.class);
    }
}
