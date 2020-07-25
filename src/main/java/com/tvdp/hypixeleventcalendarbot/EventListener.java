package com.tvdp.hypixeleventcalendarbot;

import com.tvdp.hypixeleventcalendarbot.command.Command;
import com.tvdp.hypixeleventcalendarbot.command.CreateEmojisCommand;
import com.tvdp.hypixeleventcalendarbot.command.CreateMessageCommand;
import com.tvdp.hypixeleventcalendarbot.command.RestoreSubscriptionsCommand;
import com.tvdp.hypixeleventcalendarbot.reaction.Reaction;
import com.tvdp.hypixeleventcalendarbot.reaction.SubscribeReaction;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventListener extends ListenerAdapter
{
    public static List<Command> commands = new ArrayList<>();
    public static Map<List<String>, Reaction> reactions = new HashMap<>();

    public static Map<String, String> savedMessageIds = new HashMap<>();

    public EventListener() {
        commands.add(new CreateEmojisCommand());
        commands.add(new CreateMessageCommand());
        commands.add(new RestoreSubscriptionsCommand());

        List<String> names = new ArrayList<>();
        names.add("Zoo");
        names.add("WinterEvent");
        names.add("SpookyFestival");
        names.add("NewYear");
        names.add("DarkAuction");
        names.add("JerryWorkshop");
        reactions.put(names, new SubscribeReaction());
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        commands.forEach(command -> {
            if (command.canCommandBeRun(event))
            {
                command.execute(event);
            }
        });
    }

    @Override
    public void onMessageBulkDelete(@Nonnull MessageBulkDeleteEvent event) {
        event.getMessageIds().stream()
                .distinct()
                .filter(savedMessageIds::containsKey)
                .collect(Collectors.toSet())
                .forEach(id -> savedMessageIds.remove(id));
    }

    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event)
    {
        savedMessageIds.remove(event.getMessageId());
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event)
    {
        if (savedMessageIds.containsKey(event.getMessageId()) && !event.getUserId().equals(event.getJDA().getSelfUser().getId()))
        {
            if (event.getReactionEmote().isEmote()) {
                reactions.forEach((names, reaction) -> {
                    if (names.contains(event.getReactionEmote().getEmote().getName())) {
                        reaction.onReactionAdded(event);
                    } else {
                        event.getReaction().removeReaction(event.getUser()).queue();
                    }
                });
            } else {
                event.getReaction().removeReaction(event.getUser()).queue();
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event)
    {
        if (savedMessageIds.containsKey(event.getMessageId()) && !event.getUserId().equals(event.getJDA().getSelfUser().getId()))
        {
            if (event.getReactionEmote().isEmote()) {
                reactions.forEach((names, reaction) -> {
                    if (names.contains(event.getReactionEmote().getEmote().getName())) {
                        reaction.onReactionRemoved(event);
                    }
                });
            }
        }
    }
}
