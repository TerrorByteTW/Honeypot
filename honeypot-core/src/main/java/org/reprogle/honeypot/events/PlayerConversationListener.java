package org.reprogle.honeypot.events;

import org.bukkit.block.Block;
import org.bukkit.conversations.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reprogle.honeypot.ConfigColorManager;
import org.reprogle.honeypot.commands.CommandFeedback;
import org.reprogle.honeypot.storagemanager.HoneypotBlockStorageManager;

public class PlayerConversationListener extends StringPrompt implements ConversationAbandonedListener {

    private Block block;

    public PlayerConversationListener(Block block) {
        this.block = block;
    }

    @NotNull
    @Override
    public String getPromptText(@NotNull
    ConversationContext conversationContext) {
        return ConfigColorManager.getChatPrefix()
                + " Enter Honeypot type here.";
    }

    @Nullable
    @Override
    public Prompt acceptInput(@NotNull
    ConversationContext conversationContext, @Nullable
    String s) {
        HoneypotBlockStorageManager.createBlock(block, s);
        conversationContext.getForWhom().sendRawMessage(CommandFeedback.sendCommandFeedback("success", true));
        return END_OF_CONVERSATION;
    }

    @Override
    public void conversationAbandoned(@NotNull
    ConversationAbandonedEvent conversationAbandonedEvent) {
        if (!conversationAbandonedEvent.gracefulExit()) {
            conversationAbandonedEvent.getContext().getForWhom()
                    .sendRawMessage(CommandFeedback.sendCommandFeedback("inputcancelled"));
        }
    }
}
