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

package org.reprogle.honeypot.common.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.reprogle.honeypot.Honeypot;
import org.reprogle.honeypot.common.utils.HoneypotConfigManager;

import java.util.Objects;

@Singleton
public class CommandFeedback {

    public static final MiniMessage mm = MiniMessage.miniMessage();
    @Inject
    private HoneypotConfigManager configManager;

    /**
     * A helper class which helps to reduce boilerplate player.sendMessage code by
     * providing the strings to send instead
     * of having to copy and paste them.
     *
     * @param feedback The string to send back
     * @param success  An optional Boolean which is used for the success feedback.
     *                 If none is passed, success just
     *                 replies "Success!"
     * @return The Feedback string
     */
    @SuppressWarnings("java:S1192")
    public Component sendCommandFeedback(String feedback, Boolean... success) {
        Component feedbackMessage;
        Component chatPrefix = getChatPrefix();
        YamlDocument languageFile = configManager.getLanguageFile();

        switch (feedback.toLowerCase()) {
            case "usage" ->
                    feedbackMessage = Component.text().content("\n \n \n \n \n \n-----------------------\n \n").color(NamedTextColor.WHITE)
                            .append(chatPrefix).append(Component.text(" "))
                            .append(Component.text("Need help?\n\n", NamedTextColor.WHITE))
                            .append(Component.text(" /honeypot ", NamedTextColor.WHITE)).append(Component.text("remove (all | near) (optional)\n", NamedTextColor.GRAY))
                            .append(Component.text(" /honeypot ", NamedTextColor.WHITE)).append(Component.text("reload\n", NamedTextColor.GRAY))
                            .append(Component.text(" /honeypot ", NamedTextColor.WHITE)).append(Component.text("locate\n", NamedTextColor.GRAY))
                            .append(Component.text(" /honeypot ", NamedTextColor.WHITE)).append(Component.text("gui\n", NamedTextColor.GRAY))
                            .append(Component.text(" /honeypot ", NamedTextColor.WHITE)).append(Component.text("history [query | delete | purge] \n", NamedTextColor.GRAY))
                            .append(Component.text("-----------------------", NamedTextColor.WHITE))
                            .build();

            case "success" -> {
                if (success.length > 0 && success[0].equals(true)) {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(Component.text().append(mm.deserialize(languageFile.getString("success.created"))))
                            .build();

                } else if (success.length > 0 && success[0].equals(false)) {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(Component.text().append(mm.deserialize(languageFile.getString("success.removed"))))
                            .build();

                } else {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(Component.text().append(mm.deserialize(languageFile.getString("success.default"))))
                            .build();
                }
            }

            case "debug" -> {
                if (success.length > 0 && success[0].equals(true)) {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(Component.text("Debug mode has been enabled. Right click any block to check its PDC"))
                            .build();

                } else if (success.length > 0 && success[0].equals(false)) {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(Component.text("Debug mode has been disabled"))
                            .build();
                } else {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(Component.text("Debug mode is only useful while using PDC"))
                            .build();
                }
            }

            case "migrate" -> {
                if (success.length > 0 && success[0].equals(true)) {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(Component.text().append(mm.deserialize(languageFile.getString("migrate.confirm")))).build();
                } else if (success.length > 0 && success[0].equals(false)) {
                        feedbackMessage = Component.text().append(chatPrefix)
                                .append(Component.text(" "))
                                .append(Component.text().append(mm.deserialize(languageFile.getString("migrate.failed")))).build();
                } else {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(Component.text().append(mm.deserialize(languageFile.getString("migrate.preconfirm")))).build();
                }
            }

            case "deleted" -> {
                if (success.length > 0 && success[0].equals(true)) {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(Component.text().append(mm.deserialize(languageFile.getString("deleted.all")))).build();
                } else {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(Component.text().append(mm.deserialize(languageFile.getString("deleted.near")))).build();
                }
            }

            default -> {
                try {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(Component.text().append(mm.deserialize(languageFile.getString(feedback.toLowerCase()))))
                            .build();
                } catch (Exception e) {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(Component.text().append(mm.deserialize(languageFile.getString("unknown-error"))))
                            .build();
                }
            }
        }
        return feedbackMessage;
    }

    /**
     * Return the chat prefix object from config
     *
     * @return The chat prefix, preformatted with color and other modifiers
     */
    public Component getChatPrefix() {
        return mm.deserialize(Objects.requireNonNull(configManager.getLanguageFile().getString("prefix")));
    }

    @SuppressWarnings("UnstableApiUsage")
    public Component buildSplash(Honeypot plugin) {
        return Component.text().content("\n")
                .append(Component.text(" _____                         _\n", NamedTextColor.GOLD))
                .append(Component.text("|  |  |___ ___ ___ _ _ ___ ___| |_\n", NamedTextColor.GOLD))
                .append(Component.text("|     | . |   | -_| | | . | . |  _|    by", NamedTextColor.GOLD).append(Component.text(" TerrorByte\n", NamedTextColor.RED)))
                .append(Component.text("|__|__|___|_|_|___|_  |  _|___|_|      version ", NamedTextColor.GOLD).append(Component.text(plugin.getPluginMeta().getVersion() + "\n", NamedTextColor.RED)))
                .append(Component.text("                  |___|_|", NamedTextColor.GOLD)).build();
    }

}
