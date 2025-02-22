package com.zenith.command;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.zenith.Proxy;
import com.zenith.network.server.ServerConnection;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.zenith.Shared.*;

public abstract class Command {
    public static <T> RequiredArgumentBuilder<CommandContext, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static boolean validateAccountOwner(final CommandContext context) {
        try {
            final boolean allowed = switch (context.getCommandSource()) {
                case DISCORD -> validateAccountOwnerDiscord(context);
                case TERMINAL -> true;
                case IN_GAME_PLAYER -> validateAccountOwnerInGame(context);
            };
            if (!allowed) {
                context.getEmbedBuilder()
                    .title("Not Authorized!")
                    .color(Color.RUBY);
            }
            return allowed;
        } catch (final Throwable e) {
            DEFAULT_LOG.error("Error validating command account owner authorization", e);
            return false;
        }
    }

    private static boolean validateAccountOwnerInGame(final CommandContext context) {
        final ServerConnection currentPlayer = Proxy.getInstance().getCurrentPlayer().get();
        if (currentPlayer == null) return false;
        final GameProfile playerProfile = currentPlayer.getProfileCache().getProfile();
        if (playerProfile == null) return false;
        final UUID playerUUID = playerProfile.getId();
        if (playerUUID == null) return false;
        final GameProfile proxyProfile = CACHE.getProfileCache().getProfile();
        if (proxyProfile == null) return false;
        final UUID proxyUUID = proxyProfile.getId();
        if (proxyUUID == null) return false;
        final boolean allowed = playerUUID.equals(proxyUUID);// we have to be logged in with the owning MC account
        if (!allowed) {
            context.getEmbedBuilder()
                .addField("Error",
                          "Player: " + playerProfile.getName()
                              + " is not authorized to execute this command! You must be logged in with the proxy's MC account!", true);
        }
        return allowed;
    }

    private static boolean validateAccountOwnerDiscord(final CommandContext context) {
        final DiscordCommandContext discordCommandContext = (DiscordCommandContext) context;
        final MessageCreateEvent event = discordCommandContext.getMessageCreateEvent();
        final boolean hasAccountOwnerRole = event.getMember()
            .orElseThrow(() -> new RuntimeException("Message does not have a valid member"))
            .getRoleIds()
            .stream()
            .map(Snowflake::asString)
            .anyMatch(roleId -> roleId.equals(CONFIG.discord.accountOwnerRoleId));
        if (!hasAccountOwnerRole) {
            context.getEmbedBuilder()
                .addField("Error",
                          "User: " + event.getMember().map(User::getTag).orElse("Unknown")
                              + " is not authorized to execute this command! Contact the account owner", true);
            return false;
        }
        return true;
    }

    public static CaseInsensitiveLiteralArgumentBuilder<CommandContext> literal(String literal) {
        return CaseInsensitiveLiteralArgumentBuilder.literal(literal);
    }

    public static CaseInsensitiveLiteralArgumentBuilder<CommandContext> literal(String literal, Function<CommandContext, Void> errorHandler) {
        return literal(literal).withErrorHandler(errorHandler);
    }

    public static CaseInsensitiveLiteralArgumentBuilder<CommandContext> requires(String literal, Predicate<CommandContext> requirement) {
        return literal(literal).requires(requirement);
    }

    public static String toggleStr(boolean state) {
        return state ? "on" : "off";
    }

    /**
     * Required. Registers {@link CommandUsage}
     */
    public abstract CommandUsage commandUsage();

    /**
     * Required. Register a {@link #command}
     */
    public abstract LiteralArgumentBuilder<CommandContext> register();

    /**
     * Override to populate the embed builder after every successful execution
     */
    public void postPopulate(final EmbedCreateSpec.Builder builder) {}

    /**
     * Optional override to register command aliases.
     * Also check these are set in {@link #commandUsage()}
     * todo: auto-set these in commandUsage
     */
    public List<String> aliases() {
        return Collections.emptyList();
    }

    public CaseInsensitiveLiteralArgumentBuilder<CommandContext> command(String literal) {
        return literal(literal)
            .withErrorHandler(this::usageErrorHandler)
            .withSuccesshandler((context) -> {
                postPopulate(context.getEmbedBuilder());
                return null;
            });
    }

    /**
     * Workaround for no-arg redirect nodes
     * see https://github.com/Mojang/brigadier/issues/46
     * 4 years and no official fix T.T
     */
    public LiteralArgumentBuilder<CommandContext> redirect(String literal, final CommandNode<CommandContext> destination) {
        final LiteralArgumentBuilder<CommandContext> builder = command(literal)
                .requires(destination.getRequirement())
                .forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
                .executes(destination.getCommand());
        for (final CommandNode<CommandContext> child : destination.getChildren()) {
            builder.then(child);
        }
        return builder;
    }

    public Void usageErrorHandler(final CommandContext context) {
        context.getEmbedBuilder()
                .title("Invalid command usage")
                .addField("Usage", commandUsage().serialize(context.getCommandSource()), false)
                .color(Color.RUBY);
        return null;
    }
}
