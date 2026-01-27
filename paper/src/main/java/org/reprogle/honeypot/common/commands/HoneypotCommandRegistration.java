package org.reprogle.honeypot.common.commands;

import com.google.inject.Inject;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.bytelib.commands.CommandRegistration;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.common.utils.GhostHoneypotFixer;
import org.reprogle.honeypot.common.utils.HoneypotLogger;
import org.reprogle.honeypot.common.utils.HoneypotSupportedVersions;

import java.util.List;
import java.util.Set;

public final class HoneypotCommandRegistration implements CommandRegistration {
    private final CommandFeedback feedback;
    private final Set<HoneypotSubCommand> subcommands;
    private final JavaPlugin plugin;
    private final HoneypotLogger logger;
    private final GhostHoneypotFixer ghf;
    private final BytePluginConfig config;
    private final HoneypotSupportedVersions versions;

    @Inject
    public HoneypotCommandRegistration(
            CommandFeedback feedback,
            Set<HoneypotSubCommand> subcommands,
            JavaPlugin plugin,
            HoneypotLogger logger,
            GhostHoneypotFixer ghf,
            BytePluginConfig config,
            HoneypotSupportedVersions versions
    ) {
        this.feedback = feedback;
        this.subcommands = subcommands;
        this.plugin = plugin;
        this.logger = logger;
        this.ghf = ghf;
        this.config = config;
        this.versions = versions;
    }

    @Override
    public void register(Commands commands) {
        commands.register("honeypot",
                "Main command for Honeypot",
                List.of("hp"),
                new Command("honeypot.commands",
                        "usage",
                        "no-permission",
                        plugin,
                        logger,
                        feedback,
                        subcommands,
                        ghf,
                        config,
                        versions));
    }
}
