package me.terrorbyte.honeypot.gui.pagination;

import me.terrorbyte.honeypot.gui.GUIMenu;
import me.terrorbyte.honeypot.gui.button.GUIButton;

public interface GUIPageButtonBuilder {
	GUIButton buildPaginationButton(GUIPageButtonType type, GUIMenu inventory);
}
