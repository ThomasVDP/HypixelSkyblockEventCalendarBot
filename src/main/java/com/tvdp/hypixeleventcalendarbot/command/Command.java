package com.tvdp.hypixeleventcalendarbot.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.atomic.AtomicBoolean;

public interface Command
{
    boolean canCommandBeRun(MessageReceivedEvent event);

    void execute(MessageReceivedEvent event);

    default boolean isNotBot(MessageReceivedEvent event) {
        return !event.getAuthor().isBot();
    }

    default boolean isCommand(MessageReceivedEvent event, String regex) {
        return event.getMessage().getContentRaw().matches(regex);
    }

    default boolean userHasPermission(MessageReceivedEvent event, Permission permission) {
        if (event.getMember() == null) return false;

        AtomicBoolean found = new AtomicBoolean(false);
        event.getMember().getRoles().forEach(role -> {
            if (role.getPermissions().contains(permission) || role.getPermissions().contains(Permission.ADMINISTRATOR)) {
                found.set(true);
            }
        });

        return found.get();
    }
}
