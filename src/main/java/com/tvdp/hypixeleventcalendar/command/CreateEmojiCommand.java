package com.tvdp.hypixeleventcalendar.command;

import com.tvdp.hypixeleventcalendar.HypixelEventCalendarBot;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.util.Image;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateEmojiCommand implements Command
{
    @Override
    public boolean commandCanBeRun(MessageCreateEvent event) {
        return isNotBot(event) && isCommand(event, "^hp!createEmojis$") &&
                event.getMessage().getChannel().map(channel -> !channel.getId().equals(event.getClient().getSelf()
                        .map(user -> user.getPrivateChannel().map(PrivateChannel::getId)))).block() &&
                event.getMessage().getAuthor()
                        .filter(user -> event.getMessage().getChannel().map(channel -> !(channel instanceof PrivateChannel)).block())
                        .map(user -> user.asMember(event.getGuildId().get())
                                .filter(member -> !member.getRoleIds().isEmpty())
                                .map(member -> {
                                    AtomicBoolean found = new AtomicBoolean(false);
                                    member.getRoles().doOnNext(role -> {
                                        if (role.getPermissions().contains(Permission.ADMINISTRATOR))
                                            found.set(true);
                                    }).subscribe();
                                    return found.get();
                                }).block()
                        ).orElse(false);
    }

    @Override
    public void execute(MessageCreateEvent event)
    {
        HypixelEventCalendarBot.reactions.forEach((names, reaction) -> {
            names.forEach(name -> {
                AtomicBoolean found = new AtomicBoolean(false);
                event.getGuild().map(Guild::getEmojis).block()
                        .groupBy(emoji -> names.contains(emoji.getName()))
                        .doOnNext(group -> {
                            if (group.key()) found.set(true);
                        })
                        .doOnTerminate(() -> {
                            if (!found.get()) {
                                try {
                                    File file = new File(HypixelEventCalendarBot.class.getClassLoader().getResource(name + ".png").getFile());
                                    InputStream stream = HypixelEventCalendarBot.class.getClassLoader().getResourceAsStream(name + ".png");
                                    byte[] rawImage = new byte[(int)file.length()];
                                    stream.read(rawImage);
                                    System.out.println("Test2");
                                    event.getGuild().flatMap(guild -> guild.createEmoji(spec -> spec.setName(name).setImage(Image.ofRaw(rawImage, Image.Format.PNG)))).subscribe();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).subscribe();

            });
        });
    }
}
