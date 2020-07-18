package com.tvdp.hypixeleventcalendar.command;

import com.tvdp.hypixeleventcalendar.CalendarRetriever;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SubscribeCommand implements Command
{
    @Override
    public Mono<Void> execute(MessageCreateEvent event)
    {
        if (event.getMessage().getContent().matches("^!subscribe (.*?)$")) {
            User user = event.getMessage().getAuthor().orElse(null);
            if (user != null) {
                String option = event.getMessage().getContent().split(" ")[1];
                if (Arrays.stream(CalendarRetriever.TYPES).anyMatch(option::equals)) {
                    if (CalendarRetriever.subscribers.containsKey(option)) {
                        if (CalendarRetriever.subscribers.get(option).contains(user.getId())) {
                            return Mono.empty();
                        }
                        CalendarRetriever.subscribers.get(option).add(user.getId());
                    } else {
                        CalendarRetriever.subscribers.put(option, new ArrayList<>(Collections.singleton(user.getId())));
                    }
                    return event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createEmbed(embedCreateSpec -> embedCreateSpec.setColor(Color.GREEN).setTitle("Successfully subscribed " + user.getUsername() + " for " + option + "!"))).then();
                } else {
                    return event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createEmbed(embedCreateSpec -> embedCreateSpec.setColor(Color.GREEN).setTitle("This option does not exist!"))).then();
                }
            } else {
                return Mono.empty();
            }
        } else {
            return event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createEmbed(embedCreateSpec -> embedCreateSpec.setColor(Color.GREEN).setTitle("The command was masformed!"))).then();
        }
    }
}
