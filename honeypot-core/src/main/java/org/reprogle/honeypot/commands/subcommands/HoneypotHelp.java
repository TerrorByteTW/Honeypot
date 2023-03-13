package org.reprogle.honeypot.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.commands.HoneypotSubCommand;
import org.reprogle.honeypot.utils.HoneypotPermission;

public class HoneypotHelp implements HoneypotSubCommand {

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public void perform(Player p, String[] args) throws IOException {
		p.sendMessage(CommandFeedback.sendCommandFeedback("usage"));

	}

	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public List<HoneypotPermission> getRequiredPermissions() {
		return new ArrayList<>();
	}

}
