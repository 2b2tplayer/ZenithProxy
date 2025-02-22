package com.zenith.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.command.Command;
import com.zenith.command.CommandContext;
import com.zenith.command.CommandUsage;
import discord4j.rest.util.Color;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.zenith.Shared.COMMAND_MANAGER;
import static java.util.Arrays.asList;

public class HelpCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.simpleAliases(
                "help",
                "ZenithProxy command list",
                aliases());
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("help").executes(c -> {
                c.getSource().getEmbedBuilder()
                    .title("Commands")
                    .color(Color.CYAN);
                final String brigadierCommands = COMMAND_MANAGER.getCommands().stream()
                    .sorted((c1, c2) -> c1.commandUsage().getName().compareToIgnoreCase(c2.commandUsage().getName()))
                    .map(command -> command.commandUsage().shortSerialize(c.getSource().getCommandSource()))
                    .collect(Collectors.joining("\n"));
                c.getSource().getEmbedBuilder()
                    .description("**More info:** \n  " + COMMAND_MANAGER.getCommandPrefix(c.getSource().getCommandSource()) + "help <command>\n\n**Command List**\n" + brigadierCommands);
            })
            .then(argument("commandName", string()).executes(c -> {
                c.getSource().getEmbedBuilder()
                    .title("Command Usage")
                    .color(Color.CYAN);
                final String commandName = StringArgumentType.getString(c, "commandName");
                final Optional<Command> foundCommand = COMMAND_MANAGER.getCommands().stream()
                    .filter(command -> command.commandUsage().getName().equalsIgnoreCase(commandName)
                        || command.commandUsage().getAliases().stream().anyMatch(a -> a.equalsIgnoreCase(commandName)))
                    .findFirst();
                if (foundCommand.isPresent()) {
                    c.getSource().getEmbedBuilder().description(foundCommand.get().commandUsage().serialize(c.getSource().getCommandSource()));
                } else {
                    c.getSource().getEmbedBuilder().description("Unknown command");
                }
                return 1;
            }));
    }

    @Override
    public List<String> aliases() {
        return asList("h");
    } // I love undertime slopper
}
