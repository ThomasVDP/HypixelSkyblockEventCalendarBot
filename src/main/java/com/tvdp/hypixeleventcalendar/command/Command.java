package com.tvdp.hypixeleventcalendar.command;

import discord4j.core.event.domain.message.MessageCreateEvent;

public interface Command
{
    boolean commandCanBeRun(MessageCreateEvent event);

    void execute(MessageCreateEvent event);

    default boolean isNotBot(MessageCreateEvent event)
    {
        return event.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false);
    }

    default boolean isCommand(MessageCreateEvent event, String regex)
    {
        return event.getMessage().getContent().matches(regex);
    }
}
