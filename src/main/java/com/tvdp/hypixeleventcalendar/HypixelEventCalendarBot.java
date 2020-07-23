package com.tvdp.hypixeleventcalendar;

import com.tvdp.hypixeleventcalendar.command.Command;
import com.tvdp.hypixeleventcalendar.command.CreateEmojiCommand;
import com.tvdp.hypixeleventcalendar.command.CreateMessageCommand;
import com.tvdp.hypixeleventcalendar.reaction.Reaction;
import com.tvdp.hypixeleventcalendar.reaction.SubscribeReaction;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.util.Image;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HypixelEventCalendarBot
{
    public static GatewayDiscordClient client;

    public static final List<Command> commands = new ArrayList<>();
    public static final Map<List<String>, Reaction> reactions = new HashMap<>();

    public static List<Snowflake> savedMessages = new ArrayList<>();

    public static void main(String[] args)
    {
        client = DiscordClientBuilder.create(System.getenv("TOKEN"))
                .build()
                .login()
                .block();

        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(event -> {
                    User self = event.getSelf();
                    System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));
                    System.out.println("Starting timers!");
                    //start countdown
                    CalendarRetriever calendar = new CalendarRetriever();
                    calendar.requestTimers();
                });


        client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(event -> Flux.fromIterable(commands)
                        .filter(command -> command.commandCanBeRun(event))
                        .doOnNext(command -> command.execute(event)))
                .subscribe();

        client.getEventDispatcher().on(ReactionAddEvent.class)
                .filterWhen(event -> event.getMessage()
                        .map(message -> savedMessages.contains(message.getId())))
                .filterWhen(event -> event.getUser()
                        .map(User::getId)
                        .map(id -> !client.getSelfId().equals(id)))
                .doOnNext(event -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    reactions.forEach((strings, reaction) -> {
                        if (strings.contains(event.getEmoji().asCustomEmoji().map(ReactionEmoji.Custom::getName).orElse(""))) {
                            reaction.onAdded(event);
                            found.set(true);
                        }
                    });
                    if (!found.get()) {
                        event.getMessage().flatMap(message -> message.removeReaction(event.getEmoji(), event.getUserId())).subscribe();
                    }
                })
                .subscribe();

        client.getEventDispatcher().on(ReactionRemoveEvent.class)
                .filterWhen(event -> event.getMessage()
                    .map(message -> savedMessages.contains(message.getId())))
                .filterWhen(event -> event.getUser()
                    .map(User::getId)
                    .map(id -> !client.getSelfId().equals(id)))
                .doOnNext(event -> {
                    AtomicBoolean found = new AtomicBoolean(false);
                    reactions.forEach((strings, reaction) -> {
                        if (strings.contains(event.getEmoji().asCustomEmoji().map(ReactionEmoji.Custom::getName).orElse(""))) {
                            reaction.onRemoved(event);
                            found.set(true);
                        }
                    });
                })
                .subscribe();

        client.onDisconnect().block();
    }

    static {
        commands.add(new CreateMessageCommand());
        commands.add(new CreateEmojiCommand());

        List<String> names = new ArrayList<>();
        names.add("Zoo");
        names.add("WinterEvent");
        names.add("SpookyFestival");
        names.add("NewYear");
        names.add("DarkAuction");
        names.add("JerryWorkshop");
        reactions.put(names, new SubscribeReaction());
    }
}
