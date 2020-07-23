package com.tvdp.hypixeleventcalendar.command;

import com.tvdp.hypixeleventcalendar.HypixelEventCalendarBot;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;

public class CreateMessageCommand implements Command
{
    @Override
    public boolean commandCanBeRun(MessageCreateEvent event) {
        return isNotBot(event) && isCommand(event, "^hp!getyourmessage$") &&
                event.getMessage().getChannel().map(channel -> !channel.getId().equals(event.getClient().getSelf()
                    .map(user -> user.getPrivateChannel().map(PrivateChannel::getId)))).block() &&
                event.getMessage().getAuthor()
                    .filter(user -> event.getMessage().getChannel().map(channel -> !(channel instanceof PrivateChannel)).block())
                    .map(user -> user.asMember(event.getGuildId().get())
                            .filter(member -> !member.getRoleIds().isEmpty())
                            .map(member -> member.getHighestRole().map(role -> role.getPermissions().contains(Permission.ADMINISTRATOR)).block()).block()
                ).orElse(false);
    }

    @Override
    public void execute(MessageCreateEvent event)
    {
        event.getMessage().getChannel()
                .flatMap(channel -> channel.createEmbed(embedCreateSpec -> embedCreateSpec.setTitle("Hypixel's Skyblock Event Bot").setColor(Color.GREEN)
                    .addField("Subscribe to the specific event using the emojis below!", "You will get a DM 30 min & 10 min in advance (also when it starts of course)!", false)))
                .doOnNext(message -> HypixelEventCalendarBot.savedMessages.add(message.getId()))
                .doOnNext(message -> HypixelEventCalendarBot.reactions.forEach((strings, reaction) -> {
                    for (String name : strings) {
                        event.getGuild().map(guild -> guild.getEmojis()
                            .filter(emoji -> emoji.getName().equals(name))).block()
                            .flatMap(emoji -> message.addReaction(ReactionEmoji.custom(emoji)))
                            .subscribe();
                    }
                })).subscribe();
    }
}
