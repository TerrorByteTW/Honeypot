package org.reprogle.honeypot.gui.pagination;

import org.reprogle.honeypot.gui.GUIMenu;
import org.reprogle.honeypot.gui.button.GUIButton;

public interface GUIPageButtonBuilder {
	GUIButton buildPaginationButton(GUIPageButtonType type, GUIMenu inventory);
}
