package org.reprogle.honeypot.gui.menu;

import org.bukkit.entity.Player;
import org.reprogle.honeypot.gui.GUIMenu;

public class GUIOpenMenu {
    private final GUIMenu gui;

    private final Player player;

    public GUIOpenMenu(GUIMenu gui, Player player) {
        this.gui = gui;
        this.player = player;
    }

    public GUIMenu getGUI() {
        return this.gui;
    }

    public Player getPlayer() {
        return this.player;
    }
}
