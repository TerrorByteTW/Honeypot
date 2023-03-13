package org.reprogle.honeypot.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.commands.HoneypotSubCommand;
import org.reprogle.honeypot.utils.HoneypotPermission;

public class HoneypotList implements HoneypotSubCommand {

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public void perform(Player p, String[] args) throws IOException {
        HoneypotGUI.callAllHoneypotsInventory(p);
    }

    @Override
    public List<String> getSubcommands(Player p, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public List<HoneypotPermission> getRequiredPermissions() {
        List<HoneypotPermission> permissions = new ArrayList<>();
        permissions.add(new HoneypotPermission("honeypot.gui"));
        return permissions;
    }

}
