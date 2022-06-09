package me.terrorbyte.honeypot.commands.subcommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.terrorbyte.honeypot.Honeypot;
import me.terrorbyte.honeypot.commands.HoneypotSubCommand;
import me.terrorbyte.honeypot.gui.GUI;
import me.terrorbyte.honeypot.gui.GUIMenu;
import me.terrorbyte.honeypot.gui.button.GUIButton;
import me.terrorbyte.honeypot.gui.item.GUIItemBuilder;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockObject;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;

public class HoneypotGUI extends HoneypotSubCommand{

	@Override
	public String getName() {
		return "gui";
	}

	@Override
	public void perform(Player p, String[] args) throws IOException {
		GUI gui = new GUI(Honeypot.getPlugin());
		GUIMenu guiMenu = gui.create("Honeypots", 3);
		
		for (HoneypotBlockObject honeypotBlock : HoneypotBlockStorageManager.getAllHoneypots()) {
			
			if (Honeypot.guiConfig.getBoolean("display-button-as-honeypot")) {
				GUIItemBuilder item = new GUIItemBuilder(Material.ARROW);
			} else {
				GUIItemBuilder item = new GUIItemBuilder(Material.ARROW);
			}

			GUIButton button = new GUIButton(
				item.build()
			).withListener((InventoryClickEvent event) -> {
				event.getWhoClicked().sendMessage("Hello!");
			});
		}
		
	}

	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		return new ArrayList<>();
	}
	
}
