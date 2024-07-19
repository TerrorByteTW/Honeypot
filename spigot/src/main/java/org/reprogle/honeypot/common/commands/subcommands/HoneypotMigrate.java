package org.reprogle.honeypot.common.commands.subcommands;

import com.google.inject.Inject;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockObject;
import org.reprogle.honeypot.common.storagemanager.pdc.DataStoreManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HoneypotMigrate implements HoneypotSubCommand {

    private final CommandFeedback commandFeedback;
    private final HoneypotConfigManager configManager;
    private final HoneypotBlockManager hbm;
    private final Honeypot plugin;
    private final DataStoreManager dataStoreManager;

    @Inject
    public HoneypotMigrate(CommandFeedback commandFeedback, HoneypotConfigManager configManager, HoneypotBlockManager hbm, Honeypot plugin, DataStoreManager dataStoreManager) {
        this.commandFeedback = commandFeedback;
        this.configManager = configManager;
        this.hbm = hbm;
        this.plugin = plugin;
        this.dataStoreManager = dataStoreManager;
    }

    @Override
    public String getName() {
        return "migrate";
    }

    @Override
    public void perform(Player p, String[] args) throws IOException {
        if (!configManager.getPluginConfig().getString("storage-method").equalsIgnoreCase("pdc")) {
            p.sendMessage(commandFeedback.sendCommandFeedback("migrate", false));
            return;
        }

        if (args.length == 1) {
            p.sendMessage(commandFeedback.sendCommandFeedback("migrate"));
        } else if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("confirm")) {
                // Get all worlds in the server, since PDC works on a per-world basis
                List<World> worlds = plugin.getServer().getWorlds();

                // For every world on the server, get all blocks. Add each of those blocks to PDc
                for (World world : worlds) {
                    List<HoneypotBlockObject> blocks = hbm.getAllHoneypots(world);
                    for (HoneypotBlockObject block : blocks) {
                        dataStoreManager.createHoneypotBlock(block.getBlock(), block.getAction());
                    }
                }

                p.sendMessage(commandFeedback.sendCommandFeedback("migrate", true));

                // Change the storage method to pdc and shutdown the plugin to prevent potential issues. The storage method is decided on server start,
                // and absolutely cannot be changed in runtime due to it being injected everywhere via Guice.
                configManager.getPluginConfig().set("storage-method", "pdc");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            }
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return List.of("confirm");
    }

    @Override
    public List<HoneypotPermission> getRequiredPermissions() {
        List<HoneypotPermission> permissions = new ArrayList<>();
        permissions.add(new HoneypotPermission("honeypot.migrate"));
        return permissions;
    }
}
