package me.terrorbyte.honeypot.gui.menu;

import org.bukkit.entity.Player;

import me.terrorbyte.honeypot.gui.GUIMenu;

public class GUIOpenMenu {
	private final GUIMenu GUI;
    private final Player PLAYER;

    public GUIOpenMenu(GUIMenu gui, Player player) {
        this.GUI = gui;
        this.PLAYER = player;
    }

    public GUIMenu getGUI() {
        return this.GUI;
    }

    public Player getPlayer() {
        return this.PLAYER;
    }
}
