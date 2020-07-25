package com.tvdp.hypixeleventcalendarbot.command;

import com.tvdp.hypixeleventcalendarbot.EventListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateEmojisCommand implements Command
{
    @Override
    public boolean canCommandBeRun(MessageReceivedEvent event) {
        return isNotBot(event) && isCommand(event, "^hp!createEmojis$") && userHasPermission(event, Permission.MANAGE_EMOTES);
    }

    @Override
    public void execute(MessageReceivedEvent event)
    {
        List<Emote> emotes = event.getJDA().getEmotes();

        EventListener.reactions.keySet().forEach(names -> names.forEach(name -> {
            AtomicBoolean found = new AtomicBoolean(false);
            emotes.forEach(emote -> {
                if (emote.getGuild().getId().equals(event.getGuild().getId())) {
                    if (emote.getName().equals(name)) {
                        found.set(true);
                    }
                }
            });
            if (!found.get()) {
                try {
                    event.getGuild().createEmote(name, Icon.from(getClass().getClassLoader().getResourceAsStream(name + ".png"), Icon.IconType.PNG)).queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
