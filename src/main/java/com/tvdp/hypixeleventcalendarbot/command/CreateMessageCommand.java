package com.tvdp.hypixeleventcalendarbot.command;

import com.tvdp.hypixeleventcalendarbot.EventListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class CreateMessageCommand implements Command
{
    @Override
    public boolean canCommandBeRun(MessageReceivedEvent event) {
        return isNotBot(event) && isCommand(event, "^hp!getyourmessage$") && userHasPermission(event, Permission.MANAGE_CHANNEL);
    }

    @Override
    public void execute(MessageReceivedEvent event)
    {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("Subscribe to the specific event using the emojis below!", "You will get a DM 30 min & 10 min in advance (also when it starts of course)!", false));
        event.getChannel().sendMessage(new MessageEmbed(null, "Hypixel Skyblock Event Bot!", null, EmbedType.RICH, null, Color.GREEN.getRGB(), null, null, null, null, null, null, fields)).queue(message -> {
            EventListener.savedMessageIds.put(message.getId(), event.getChannel().getId());

            EventListener.reactions.forEach((names, reaction) -> names.forEach(name -> event.getGuild().getEmotes().forEach(emote -> {
                    if (emote.getName().equals(name)) {
                        message.addReaction(emote).queue();
                    }
                })
            ));
        });
    }
}
