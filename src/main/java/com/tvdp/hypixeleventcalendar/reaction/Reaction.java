package com.tvdp.hypixeleventcalendar.reaction;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;

public interface Reaction
{
    void onAdded(ReactionAddEvent event);

    void onRemoved(ReactionRemoveEvent event);
}
