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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.reprogle.bytelib.ByteLibPlugin;
import org.reprogle.bytelib.boot.wiring.PluginWiring;
import org.reprogle.bytelib.db.sqlite.SqliteModule;

import java.nio.file.Path;
import java.util.List;

/**
 * Main method for Honeypot, this is what gets the ball rolling for everything this plugin does, including setting the command executor and registering events.
 * The Wiring class is handled via ByteLib. All the plugin's wiring happens in Honeypot$Wiring
 */
public final class Honeypot extends ByteLibPlugin {
    @Inject
    public Honeypot(Injector injector, PluginMeta meta, Path dataDir, ComponentLogger logger) {
        super(injector, meta, dataDir, logger);
    }

    public static class Wiring implements PluginWiring {
        @Override
        public List<Module> modules(PluginMeta meta, Path dataDir, ComponentLogger logger) {
            return List.of(
                    new HoneypotModule(dataDir),
                    new SqliteModule("honeypot.db")
            );
        }
    }
}
