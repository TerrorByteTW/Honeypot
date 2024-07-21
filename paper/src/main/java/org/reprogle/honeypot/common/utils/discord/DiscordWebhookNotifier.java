/*
 * Honeypot is a tool for griefing auto-moderation
 *
 * Copyright TerrorByte (c) 2024
 * Copyright Honeypot Contributors (c) 2024
 *
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.utils.discord;

import net.kyori.adventure.text.Component;
import okhttp3.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.io.IOException;

public class DiscordWebhookNotifier {

    private final WebhookActionType webhookType;
    private final String webhookUrl;
    private final Block block;
    private final Player player;
    private final HoneypotLogger logger;

    /**
     * Create a Discord webhook to send. This method does not automatically send the webhook, giving the developer more control over when it is actually sent.
     *
     * @param webhookType The type of webhook. See {@link WebhookActionType}
     * @param webhookUrl  The url of the Discord webhook to send it to
     * @param block       The block that triggered the webhook
     * @param player      The player that triggered the webhook
     */
    public DiscordWebhookNotifier(WebhookActionType webhookType, String webhookUrl, Block block, Player player, HoneypotLogger logger) {
        this.webhookType = webhookType;
        this.webhookUrl = webhookUrl;
        this.block = block;
        this.player = player;
        this.logger = logger;
    }

    /**
     * Send the webhook that was created. This will send asynchronously, so no success or failure result can be obtained from it.
     * In the future this may be changed to a Future to allow receiving of a result.
     */
    public void send() {
        final String finalBody = getBodyString();

        new Thread(() -> {
            RequestBody body = RequestBody.create(finalBody, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(webhookUrl)
                    .post(body)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    logger.severe(Component.text("Failed to send webhook: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    response.close();
                }
            });
        }).start();

    }

    private @NotNull String getBodyString() {
        String jsonTemplate = """
                {
                  "content": "",
                  "tts": false,
                  "embeds": [
                    {
                      "id": 595876530,
                      "description": "The honeypot located at %coordinates% in world %world% was triggered by %player%!",
                      "fields": [],
                      "title": "Honeypot Discord Alert - %webhookType%",
                      "timestamp": "2024-06-24T00:32:00.000Z",
                      "color": 11636736,
                      "thumbnail": {
                        "url": "https://mc-heads.net/avatar/%UUID%"
                      }
                    }
                  ],
                  "components": [],
                  "actions": {},
                  "username": "Honeypot"
                }
                """;

        String tempBody = switch (webhookType) {
            case ACTION -> jsonTemplate.replace("%webhookType%", "Action Taken");
            case BREAK -> jsonTemplate.replace("%webhookType%", "Block Broken");
        };

        return tempBody.replace("%coordinates%", block.getX() + ", " + block.getY() + ", " + block.getZ())
                .replace("%world%", "\\\"" + block.getWorld().getName() + "\\\"")
                .replace("%player%", player.getName())
                .replace("%UUID%", player.getUniqueId().toString());
    }
}
