package com.zenith.discord.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Color;

import java.util.Optional;
import java.util.function.Function;

import static com.zenith.util.Constants.CONFIG;

public abstract class Command {
    public static <T> RequiredArgumentBuilder<CommandContext, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public abstract CommandUsage commandUsage();

    public abstract void register(CommandDispatcher<CommandContext> dispatcher);

    public boolean validateAccountOwner(final CommandContext context) {
        final MessageCreateEvent event = context.getMessageCreateEvent();
        final Optional<String> roleContainsOptional = event.getMember()
                .orElseThrow(() -> new RuntimeException("Message does not have a valid member"))
                .getRoleIds()
                .stream()
                .map(Snowflake::asString)
                .filter(roleId -> roleId.equals(CONFIG.discord.accountOwnerRoleId))
                .findAny();
        if (!roleContainsOptional.isPresent()) {
            context.getEmbedBuilder()
                    .title("Not Authorized!")
                    .color(Color.RUBY)
                    .addField("Error",
                            "User: " + event.getMember().get().getUsername() + "#" + event.getMember().get().getDiscriminator()
                                    + " is not authorized to execute this command! Contact the account owner", true)
                    .build();
            return false;
        }
        return true;
    }

    public CaseInsensitiveLiteralArgumentBuilder<CommandContext> literal(String literal) {
        return CaseInsensitiveLiteralArgumentBuilder.literal(literal);
    }

    public CaseInsensitiveLiteralArgumentBuilder<CommandContext> literal(String literal, Function<CommandContext, Void> errorHandler) {
        return literal(literal).withErrorHandler(errorHandler);
    }

    public CaseInsensitiveLiteralArgumentBuilder<CommandContext> command(String literal) {
        return literal(literal).withErrorHandler(this::usageErrorHandler);
    }

    public Void usageErrorHandler(final CommandContext context) {
        context.getEmbedBuilder()
                .title("Invalid command usage")
                .addField("Usage", commandUsage().serialize(), false)
                .color(Color.RUBY);
        return null;
    }
}
