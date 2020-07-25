package com.tvdp.hypixeleventcalendarbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import javax.annotation.Nonnull;

public class HypixelEventCalendarBot implements EventListener
{
    public static JDA api;

    public static void main(String[] args) throws Exception
    {
        api = JDABuilder.createDefault(System.getenv("TOKEN")).addEventListeners(new HypixelEventCalendarBot()).build();
        api.addEventListener(new com.tvdp.hypixeleventcalendarbot.EventListener());

        CalendarRetriever calendarRetriever = new CalendarRetriever();
        calendarRetriever.requestTimers();
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event)
    {
        if (event instanceof ReadyEvent)  {
            System.out.println("API is ready and logged in as " + event.getJDA().getSelfUser().getName() + "#" + event.getJDA().getSelfUser().getDiscriminator());
        }
    }
}
