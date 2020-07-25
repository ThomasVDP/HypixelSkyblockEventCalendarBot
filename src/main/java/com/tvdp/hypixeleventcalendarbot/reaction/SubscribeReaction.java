package com.tvdp.hypixeleventcalendarbot.reaction;

import com.tvdp.hypixeleventcalendarbot.CalendarRetriever;
import com.tvdp.hypixeleventcalendarbot.EventListener;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;

import java.util.ArrayList;
import java.util.List;

public class SubscribeReaction implements Reaction
{
    @Override
    public void onReactionAdded(GuildMessageReactionAddEvent event)
    {
        String emoteName = getInteralName(event.getReactionEmote().getEmote().getName());
        if (CalendarRetriever.subscribers.containsKey(emoteName)) {
            if (!CalendarRetriever.subscribers.get(emoteName).contains(event.getUserId())) {
                CalendarRetriever.subscribers.get(emoteName).add(event.getUserId());
            }
        } else {
            List<String> list = new ArrayList<>();
            list.add(event.getUserId());
            CalendarRetriever.subscribers.put(emoteName, list);
        }
    }

    @Override
    public void onReactionRemoved(GuildMessageReactionRemoveEvent event)
    {
        String emoteName = getInteralName(event.getReactionEmote().getEmote().getName());
        if (CalendarRetriever.subscribers.containsKey(emoteName)) {
            CalendarRetriever.subscribers.get(emoteName).remove(event.getUserId());
            EventListener.savedMessageIds.forEach((messageId, channelId) -> {
                if (!messageId.equals(event.getMessageId())) {
                    ((TextChannel)event.getJDA().getGuildChannelById(channelId)).retrieveMessageById(messageId).queue(message -> {
                        message.getReactions().forEach(messageReaction -> {
                            if (event.getReactionEmote().getName().equals(messageReaction.getReactionEmote().getName())) {
                                messageReaction.removeReaction(event.getJDA().getUserById(event.getUserId())).queue();
                            }
                        });
                    });
                }
            });
        }
    }

    public static String getInteralName(String emojiName)
    {
        switch (emojiName)
        {
            case "Zoo":
                return "zoo";
            case "WinterEvent":
                return "winterEvent";
            case "SpookyFestival":
                return "spookyFestival";
            case "NewYear":
                return "newYear";
            case "JerryWorkshop":
                return "jerryWorkshopEvent";
            case "DarkAuction":
                return "darkAuction";
        }
        return "";
    }
}
