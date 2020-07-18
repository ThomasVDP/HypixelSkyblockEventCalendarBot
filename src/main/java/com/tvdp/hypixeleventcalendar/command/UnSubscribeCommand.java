package com.tvdp.hypixeleventcalendar.command;

import com.tvdp.hypixeleventcalendar.CalendarRetriever;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class UnSubscribeCommand implements Command
{
    @Override
    public Mono<Void> execute(MessageCreateEvent event)
    {
        if (event.getMessage().getContent().matches("^!unsubscribe (.*?)$")) {
            User user = event.getMessage().getAuthor().orElse(null);
            if (user != null) {
                String option = event.getMessage().getContent().split(" ")[1];
                if (Arrays.stream(CalendarRetriever.TYPES).anyMatch(option::equals)) {
                    CalendarRetriever.subscribers.get(option).remove(user.getId());
                    return event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createEmbed(embedCreateSpec -> embedCreateSpec.setColor(Color.GREEN).setTitle("Successfully unsubscribed " + user.getUsername() + " from " + option + "!"))).then();
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
