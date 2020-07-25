package com.tvdp.hypixeleventcalendarbot.reaction;

import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;

public interface Reaction
{
    void onReactionAdded(GuildMessageReactionAddEvent event);

    void onReactionRemoved(GuildMessageReactionRemoveEvent event);
}
