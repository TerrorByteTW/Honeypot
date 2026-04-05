/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) TerrorByte and Honeypot Contributors 2022 - 2025.
 *
 * This program is free software: You can redistribute it and/or modify it under
 *  the terms of the Mozilla Public License 2.0 as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including,
 * without limitation, warranties that the Covered Software is free of defects, merchantable,
 * fit for a particular purpose or non-infringing. See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.providers.included;

import com.google.inject.Inject;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.honeypot.common.commands.CommandFeedback;
import org.reprogle.honeypot.common.providers.Behavior;
import org.reprogle.honeypot.common.providers.BehaviorProvider;
import org.reprogle.honeypot.common.providers.BehaviorType;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Behavior(type = BehaviorType.WARN, name = "warn", icon = Material.STONE_AXE)
public class Warn extends BehaviorProvider {

    @Inject
    CommandFeedback commandFeedback;

    @Inject
    BytePluginConfig honeypotConfig;

    @Override
    public boolean process(Player p, Block block, @Nullable YamlDocument config) {
        boolean useRandom = config != null && config.getBoolean("use-random-messages", false);
        boolean rainbow = config != null && config.getBoolean("rainbowify", false);

        Component warnReason = commandFeedback.sendCommandFeedback("warn-reason");

        if (useRandom) {
            List<String> messages = config.getStringList("messages");
            if (!messages.isEmpty()) {
                String message = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
                MiniMessage mm = MiniMessage.miniMessage();
                warnReason = mm.deserialize(message, Placeholder.component("prefix", mm.deserialize(honeypotConfig.lang().getString("prefix"))));
            }
        }

        if (rainbow) {
            warnReason = rainbowify(warnReason);
        }

        p.sendMessage(warnReason);
        return true;
    }

    private static Component rainbowify(Component component) {
        String text = PlainTextComponentSerializer.plainText().serialize(component);
        int length = text.length();

        Index index = new Index();

        return applyRainbow(component, index, length);
    }

    private static Component applyRainbow(Component component, Index index, int length) {
        if (component instanceof TextComponent textComponent) {
            Component result = Component.empty();

            String content = textComponent.content();
            for (int i = 0; i < content.length(); i++) {
                float hue = (float) index.value / length;
                int rgb = Color.HSBtoRGB(hue, 1f, 1f);

                result = result.append(
                    Component.text(content.charAt(i))
                        .style(textComponent.style())
                        .color(TextColor.color(rgb))
                );

                index.value++;
            }

            for (Component child : component.children()) {
                result = result.append(applyRainbow(child, index, length));
            }

            return result;
        }

        return component;
    }

    private static class Index {
        int value = 0;
    }
}
