package org.reprogle.honeypot.common.utils;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class HoneypotCreatingPlayer {
    public final Player player;
    public @Nullable Location pos1 = null;
    public @Nullable Location pos2 = null;

    @Getter
    private final String action;

    public HoneypotCreatingPlayer(Player player, String action) {
        this.player = player;
        this.action = action;
    }
}
