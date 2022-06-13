package me.terrorbyte.honeypot.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import me.terrorbyte.honeypot.commands.CommandFeedback;
import me.terrorbyte.honeypot.commands.HoneypotSubCommand;

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

}
