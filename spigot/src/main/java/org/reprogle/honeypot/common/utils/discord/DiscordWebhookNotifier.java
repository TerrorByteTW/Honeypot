package org.reprogle.honeypot.common.utils.discord;

import okhttp3.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.reprogle.honeypot.common.utils.HoneypotLogger;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordWebhookNotifier {

    private final WebhookActionType webhookType;
    private final String webhookUrl;
    private final Block block;
    private final Player player;
    private final HoneypotLogger logger;

    private String jsonTemplate = """
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

    /**
     * Create a Discord webhook to send. This method does not automatically send the webhook, giving the developer more control over when it is actually sent.
     *
     * @param webhookType The type of webhook. See {@link WebhookActionType}
     * @param webhookUrl  The url of the Discord webhook to send it to
     * @param block       The block that triggered the webhook
     * @param player      The player that triggered the webhook
     * @param logger      The HoneypotLogger to log to in case something goes wrong
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
        String tempBody;

        switch (webhookType) {
            case ACTION:
                tempBody = jsonTemplate.replace("%webhookType%", "Action Taken");
                break;
            case BREAK:
                tempBody = jsonTemplate.replace("%webhookType%", "Block Broken");
                break;
            default:
                tempBody = jsonTemplate.replace("%webhookType%", "Unknown");
                break;
        }

        final String finalBody = tempBody.replace("%coordinates%", block.getX() + ", " + block.getY() + ", " + block.getZ())
                .replace("%world%", "\\\"" + block.getWorld().getName() + "\\\"")
                .replace("%player%", player.getName())
                .replace("%UUID%", player.getUniqueId().toString());

        new Thread(() -> {
            RequestBody body = RequestBody.create(finalBody, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(webhookUrl)
                    .post(body)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    response.close();
                    return;
                }
            });
        }).start();

    }
}
