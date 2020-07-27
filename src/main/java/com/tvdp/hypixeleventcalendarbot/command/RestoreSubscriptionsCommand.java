package com.tvdp.hypixeleventcalendarbot.command;

import com.tvdp.hypixeleventcalendarbot.CalendarRetriever;
import com.tvdp.hypixeleventcalendarbot.EventListener;
import com.tvdp.hypixeleventcalendarbot.reaction.SubscribeReaction;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestoreSubscriptionsCommand implements Command
{
    @Override
    public boolean canCommandBeRun(MessageReceivedEvent event) {
        return isNotBot(event) && isCommand(event, "^hp!restoreFrom (\\d+?)$") && userHasPermission(event, Permission.MANAGE_CHANNEL);
    }

    @Override
    public void execute(MessageReceivedEvent event)
    {
        Pattern pattern = Pattern.compile("^hp!restoreFrom (?<id>\\d+?)$");
        Matcher m = pattern.matcher(event.getMessage().getContentRaw());
        if (m.find())
        {
            try {
                event.getChannel().retrieveMessageById(m.group("id")).queue(message -> {
                    EventListener.savedMessageIds.put(message.getId(), event.getChannel().getId());
                    message.getReactions().forEach(messageReaction -> {
                        AtomicBoolean found = new AtomicBoolean(false);
                        EventListener.reactions.forEach((names, reaction) -> {
                            if (names.contains(messageReaction.getReactionEmote().getName())) {
                                found.set(true);
                                messageReaction.retrieveUsers().queue(users -> {
                                    users.forEach(user -> {
                                        if (!user.isBot()) {
                                            System.out.println(user.getName());
                                            String emoteName = SubscribeReaction.getInteralName(messageReaction.getReactionEmote().getName());
                                            if (CalendarRetriever.subscribers.containsKey(emoteName)) {
                                                if (!CalendarRetriever.subscribers.get(emoteName).contains(user.getId())) {
                                                    CalendarRetriever.subscribers.get(emoteName).add(user.getId());
                                                }
                                            } else {
                                                List<String> list = new ArrayList<>();
                                                list.add(user.getId());
                                                CalendarRetriever.subscribers.put(emoteName, list);
                                            }
                                        }
                                    });
                                });
                            }
                        });
                        if (!found.get()) {
                            messageReaction.clearReactions().queue();
                        }
                    });
                });
            } catch (Exception ignored) {
                ;
            }
        }
    }
}
