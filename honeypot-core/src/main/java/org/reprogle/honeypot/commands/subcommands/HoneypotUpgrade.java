package org.reprogle.honeypot.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.HoneypotConfigManager;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.commands.HoneypotSubCommand;
import org.reprogle.honeypot.storagemanager.HoneypotBlockObject;

public class HoneypotUpgrade implements HoneypotSubCommand{

	@Override
	public String getName() {
		return "upgrade";
	}

	@Override
	public void perform(Player p, String[] args) throws IOException {
		if (!(p.hasPermission("honeypot.upgrade"))) {
            p.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
            return;
        }

		if(args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
			List<HoneypotBlockObject> oldBlocks = Honeypot.getHBM().getAllHoneypots();
			int customBlock = 0;

			for (HoneypotBlockObject block : oldBlocks) {
				String action = block.getAction();
				if(!"ban".equals(action) && !"warn".equals(action) && !"kick".equals(action) && !"notify".equals(action) && !"nothing".equals(action)) {
					List<String> commands = new ArrayList<>();
					commands.add(action);

					String route = "import" + customBlock;

					HoneypotConfigManager.getHoneypotsConfig().createSection(route);

					HoneypotConfigManager.getHoneypotsConfig().createSection(route + ".type");	
					HoneypotConfigManager.getHoneypotsConfig().set(route + ".type", "command");
					
					HoneypotConfigManager.getHoneypotsConfig().createSection(route + ".commands");
					HoneypotConfigManager.getHoneypotsConfig().set(route + ".commands", commands);

					++customBlock;

					Honeypot.getHBM().deleteBlock(block.getBlock());
					Honeypot.getHBM().createBlock(block.getBlock(), route);
				}
			}

			HoneypotConfigManager.getHoneypotsConfig().save();
			p.sendMessage(CommandFeedback.sendCommandFeedback("success"));

		} else {
			p.sendMessage(CommandFeedback.sendCommandFeedback("upgrade"));
		}
	}

	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		return new ArrayList<>();
	}
	
}
