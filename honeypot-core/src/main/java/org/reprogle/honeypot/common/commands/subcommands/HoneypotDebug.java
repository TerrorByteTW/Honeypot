package org.reprogle.honeypot.common.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.commands.HoneypotSubCommand;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;
import org.reprogle.honeypot.common.utils.HoneypotPermission;

@SuppressWarnings("java:S1192")
public class HoneypotDebug implements HoneypotSubCommand {

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public void perform(Player p, String[] args) throws IOException {
        NamespacedKey key = new NamespacedKey(Honeypot.plugin, "honeypot-debug-enabled");

        if (!HoneypotConfigManager.getPluginConfig().getString("storage-method").equalsIgnoreCase("pdc")) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("debug"));
            return;
        }

        if (p.getPersistentDataContainer().has(key)) {
            p.getPersistentDataContainer().remove(key);
            p.sendMessage(CommandFeedback.sendCommandFeedback("debug", false));
        } else {
            p.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
            p.sendMessage(CommandFeedback.sendCommandFeedback("debug", true));
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
