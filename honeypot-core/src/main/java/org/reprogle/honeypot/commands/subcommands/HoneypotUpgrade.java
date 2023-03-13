package org.reprogle.honeypot.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.commands.HoneypotSubCommand;
import org.reprogle.honeypot.storagemanager.HoneypotBlockManager;
import org.reprogle.honeypot.storagemanager.HoneypotBlockObject;
import org.reprogle.honeypot.utils.HoneypotConfigManager;
import org.reprogle.honeypot.utils.HoneypotPermission;

@SuppressWarnings("java:S1192")
public class HoneypotUpgrade implements HoneypotSubCommand {

	@Override
	public String getName() {
		return "upgrade";
	}

	@Override
	public void perform(Player p, String[] args) throws IOException {
		if (HoneypotConfigManager.getHoneypotsConfig().contains("upgraded")
				&& Boolean.TRUE.equals(HoneypotConfigManager.getHoneypotsConfig().getBoolean("upgraded"))) {
			p.sendMessage(CommandFeedback.sendCommandFeedback("alreadyupgraded"));
			return;
		}

		if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
			List<HoneypotBlockObject> oldBlocks = HoneypotBlockManager.getInstance().getAllHoneypots();
			int customBlock = 0;

			for (HoneypotBlockObject block : oldBlocks) {
				String action = block.getAction();
				if (!"ban".equals(action) && !"warn".equals(action) && !"kick".equals(action)
						&& !"notify".equals(action) && !"nothing".equals(action)) {
					List<String> commands = new ArrayList<>();
					commands.add(action);

					String route = "import" + customBlock;

					HoneypotConfigManager.getHoneypotsConfig().createSection(route);

					HoneypotConfigManager.getHoneypotsConfig().createSection(route + ".type");
					HoneypotConfigManager.getHoneypotsConfig().set(route + ".type", "command");

					HoneypotConfigManager.getHoneypotsConfig().createSection(route + ".commands");
					HoneypotConfigManager.getHoneypotsConfig().set(route + ".commands", commands);

					++customBlock;

					HoneypotBlockManager.getInstance().deleteBlock(block.getBlock());
					HoneypotBlockManager.getInstance().createBlock(block.getBlock(), route);
				}
			}

			// Mark custom actions as having already been upgraded to prevent an accidental
			// double-upgrade (Which will break things)
			HoneypotConfigManager.getHoneypotsConfig().createSection("upgraded").set("upgraded", true);

			HoneypotConfigManager.getHoneypotsConfig().save();
			p.sendMessage(CommandFeedback.sendCommandFeedback("success"));

		} else {
			p.sendMessage(CommandFeedback.sendCommandFeedback("upgrade"));
		}
	}

	// Even though this command has subcommands, I do not want to return any
	// to avoid the player confirming an upgrade before they read the warning
	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public List<HoneypotPermission> getRequiredPermissions() {
		List<HoneypotPermission> permissions = new ArrayList<>();
		permissions.add(new HoneypotPermission("honeypot.upgrade"));
		return permissions;
	}

}
