package me.terrorbyte.honeypot.events;

import me.terrorbyte.honeypot.ConfigColorManager;
import me.terrorbyte.honeypot.commands.CommandFeedback;
import me.terrorbyte.honeypot.commands.subcommands.HoneypotCreate;
import me.terrorbyte.honeypot.storagemanager.HoneypotBlockStorageManager;
import org.bukkit.block.Block;
import org.bukkit.conversations.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerConversationListener extends StringPrompt implements ConversationAbandonedListener {

    public static Block block;

    @NotNull
    @Override
    public String getPromptText(@NotNull ConversationContext conversationContext) {
        return ConfigColorManager.getChatPrefix() + " Enter action command without the / here. This command will run as the server, so be careful!";
    }

    @Nullable
    @Override
    public Prompt acceptInput(@NotNull ConversationContext conversationContext, @Nullable String s) {
        HoneypotBlockStorageManager.createBlock(block, s);
        conversationContext.getForWhom().sendRawMessage(CommandFeedback.sendCommandFeedback("success", true));
        return END_OF_CONVERSATION;
    }

    @Override
    public void conversationAbandoned(@NotNull ConversationAbandonedEvent conversationAbandonedEvent) {
        if (!conversationAbandonedEvent.gracefulExit()){
            conversationAbandonedEvent.getContext().getForWhom().sendRawMessage(CommandFeedback.sendCommandFeedback("inputcancelled"));
        }
    }
}
