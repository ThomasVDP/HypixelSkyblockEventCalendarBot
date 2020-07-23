package com.tvdp.hypixeleventcalendar.reaction;

import com.tvdp.hypixeleventcalendar.CalendarRetriever;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.ArrayList;
import java.util.List;

public class SubscribeReaction implements Reaction
{
    @Override
    public void onAdded(ReactionAddEvent event)
    {
        String emojiName = getInteralName(event.getEmoji().asCustomEmoji().map(ReactionEmoji.Custom::getName).orElse(""));
        if (CalendarRetriever.subscribers.containsKey(emojiName)) {
            if (!CalendarRetriever.subscribers.get(emojiName).contains(event.getUserId())) {
                CalendarRetriever.subscribers.get(emojiName).add(event.getUserId());
            }
        } else {
            List<Snowflake> list = new ArrayList<>();
            list.add(event.getUserId());
            CalendarRetriever.subscribers.put(emojiName, list);
        }
        //System.out.println("Subscribed " + event.getUser().map(User::getUsername).block() + " to " + emojiName);
    }

    @Override
    public void onRemoved(ReactionRemoveEvent event)
    {
        String emojiName = getInteralName(event.getEmoji().asCustomEmoji().map(ReactionEmoji.Custom::getName).orElse(""));
        if (CalendarRetriever.subscribers.containsKey(emojiName)) {
            CalendarRetriever.subscribers.get(emojiName).remove(event.getUserId());
        }
        //System.out.println("Unsubscribed " + event.getUser().map(User::getUsername).block() + " from " + emojiName);
    }

    public String getInteralName(String emojiName)
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
