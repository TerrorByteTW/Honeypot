package org.reprogle.honeypot.common.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

@SuppressWarnings("java:S1192")
public class HoneypotDebug implements HoneypotSubCommand {

    private final Honeypot plugin;
    private final CommandFeedback commandFeedback;
    private final HoneypotConfigManager configManager;

    @Inject
    public HoneypotDebug(Honeypot plugin, CommandFeedback commandFeedback, HoneypotConfigManager configManager) {
        this.plugin = plugin;
        this.commandFeedback = commandFeedback;
        this.configManager = configManager;
    }

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public void perform(Player p, String[] args) throws IOException {
        NamespacedKey key = new NamespacedKey(plugin, "honeypot-debug-enabled");

        if (!configManager.getPluginConfig().getString("storage-method").equalsIgnoreCase("pdc")) {
            p.sendMessage(commandFeedback.sendCommandFeedback("debug"));
            return;
        }

        if (p.getPersistentDataContainer().has(key)) {
            p.getPersistentDataContainer().remove(key);
            p.sendMessage(commandFeedback.sendCommandFeedback("debug", false));
        } else {
            p.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
            p.sendMessage(commandFeedback.sendCommandFeedback("debug", true));
        }
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public List<HoneypotPermission> getRequiredPermissions() {
        List<HoneypotPermission> permissions = new ArrayList<>();
        permissions.add(new HoneypotPermission("honeypot.debug"));
        return permissions;
    }

}
