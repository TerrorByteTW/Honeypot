package me.terrorbyte.honeypot.gui.button;

import org.bukkit.inventory.ItemStack;

public class GUIButton {
	private GUIButtonListener listener;
	private ItemStack icon;

	public GUIButton(ItemStack icon) {
		this.icon = icon;
	}

	public void setListener(GUIButtonListener listener){
		this.listener = listener;
	}

	public GUIButton withListener(GUIButtonListener listener){
		this.listener = listener;
		return this;
	}

	public GUIButtonListener getListener(){
		return listener;
	}

	public ItemStack getIcon() {
        return icon;
    }

	public void setIcon(ItemStack icon) {
        this.icon = icon;
    }
}
