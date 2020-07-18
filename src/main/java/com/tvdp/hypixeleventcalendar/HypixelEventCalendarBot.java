package com.tvdp.hypixeleventcalendar;

import com.tvdp.hypixeleventcalendar.command.Command;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class HypixelEventCalendarBot
{
    public static final Map<String, Tuple2<Boolean, Command>> commands = new HashMap<>();
    public static Mono<MessageChannel> botChannel;

    public static void main(String[] args)
    {
        GatewayDiscordClient client = DiscordClientBuilder.create(args[0])
                .build()
                .login()
                .block();

        long channelIdMain = 733664471066738719L;
        long channelIdTest = 726125979193573427L;
        botChannel = client.getChannelById(Snowflake.of(channelIdTest)).flatMap(channel -> Mono.just((MessageChannel)channel));

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
                .flatMap(event -> Mono.just(event.getMessage().getContent())
                    .flatMap(content -> Flux.fromIterable(commands.entrySet())
                        .filter(entry -> content.toLowerCase().startsWith(entry.getKey().toLowerCase()))
                        .filter(entry -> event.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
                        .groupBy(entry -> entry.getValue().getT1())
                        .flatMap(group -> group.key() ? group.groupBy(entry -> event.getMessage().getChannel()
                                .flatMap(messageChannel -> Mono.just(messageChannel.getId().equals(Snowflake.of(channelIdMain)) || messageChannel.getId().equals(Snowflake.of(channelIdTest)))).block())
                            .flatMap(group2 -> group2.key() ? group2.flatMap(commandEntry -> commandEntry.getValue().getT2().execute(event)).flatMap(aVoid -> Mono.just(false)) : Mono.just(true))
                            .doOnNext(aBoolean -> {
                                if (aBoolean) {
                                    wrongChannelMessage(event).block();
                                }
                            }) : group.flatMap(entry -> entry.getValue().getT2().execute(event)))
                        .then())
                    .then())
                .subscribe();

        client.onDisconnect().block();
    }

    static {
        commands.put("bing!", Tuples.of(true, event -> event.getMessage().getChannel().flatMap(channel -> channel.createMessage("Bong!")).then()));
        commands.put("!bing", Tuples.of(true, event -> event.getMessage().getChannel().flatMap(channel -> channel.createMessage("Bong!")).then()));

        //add rhythm bot
        commands.put("!p", Tuples.of(false, HypixelEventCalendarBot::denyRhythmbot));
        commands.put("!loop", Tuples.of(false, HypixelEventCalendarBot::denyRhythmbot));
        commands.put("!queue", Tuples.of(false, HypixelEventCalendarBot::denyRhythmbot));
        commands.put("!repeat", Tuples.of(false, HypixelEventCalendarBot::denyRhythmbot));
        commands.put("!join", Tuples.of(false, HypixelEventCalendarBot::denyRhythmbot));
    }

    public static Mono<Void> denyRhythmbot(MessageCreateEvent event)
    {
        return Mono.just(Objects.requireNonNull(event.getMessage().getAuthor().orElse(null))).filter(Objects::nonNull).flatMap(User::getPrivateChannel).flatMap(privateChannel -> privateChannel.createMessage("I'm sorry, but we don't have a rhythm bot on this server!")).then();
    }

    public static Mono<Void> wrongChannelMessage(MessageCreateEvent event)
    {
        return event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage("\uD83D\uDEAB You cannot use commands in this channel!")).then();
    }
}
