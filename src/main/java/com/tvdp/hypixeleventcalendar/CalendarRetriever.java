package com.tvdp.hypixeleventcalendar;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class CalendarRetriever
{
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler;
    private final HttpClient httpClient;

    public CalendarRetriever()
    {
        this.executorService = Executors.newCachedThreadPool();
        this.scheduler = Executors.newScheduledThreadPool(28);
        this.httpClient = HttpClientBuilder.create().build();
    }

    public void requestTimers()
    {
        getTimerForType("zoo/estimate").whenComplete(scheduleTimer());
        getTimerForType("bosstimer/magma/estimatedSpawn").whenComplete(scheduleTimer());
        getTimerForType("darkauction/estimate").whenComplete(scheduleTimer());
        getTimerForType("newyear/estimate").whenComplete(scheduleTimer());
        getTimerForType("spookyFestival/estimate").whenComplete(scheduleTimer());
        getTimerForType("winter/estimate").whenComplete(scheduleTimer());
        getTimerForType("jerryWorkshop/estimate").whenComplete(scheduleTimer());
    }

    public CompletableFuture<JsonObject> getTimerForType(String type)
    {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        try {
            StringBuilder url = new StringBuilder("https://hypixel-api.inventivetalent.org/api/skyblock/");
            url.append(type);

            executorService.submit(() -> {
                try {
                    JsonObject response = httpClient.execute(new HttpGet(url.toString()), obj -> {
                        String content = EntityUtils.toString(obj.getEntity(), "UTF-8");
                        return new Gson().fromJson(content, JsonObject.class);
                    });

                    future.complete(response);
                } catch (Throwable e) {
                    future.completeExceptionally(e);
                }
            });
        } catch (Throwable e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    public BiConsumer<JsonObject, Throwable> scheduleTimer()
    {
        return (obj, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("").withLocale(Locale.ENGLISH).withZone(ZoneId.of("UTC"));

            Duration duration2 = Duration.between(Instant.now(), Instant.ofEpochMilli(obj.get("estimate").getAsLong()));
            long s2 = duration2.getSeconds();
            Instant end = Instant.ofEpochMilli(obj.get("estimate").getAsLong());

            scheduler.schedule(() -> {
                Duration duration = Duration.between(Instant.now(), Instant.ofEpochMilli(obj.get("estimate").getAsLong()));
                long s = duration.getSeconds();
                HypixelEventCalendarBot.botChannel.flatMap(messageChannel -> messageChannel.createMessage(getEventName(obj.get("type").getAsString()) + " in " + (s / 3600 / 24 > 0 ? String.format("%d days ", s / 3600 / 24) : "") + String.format("%02d:%02d:%02d h", (s / 3600) % 24, (s % 3600) / 60, (s % 60)))).subscribe();
            }, Duration.between(Instant.now(), end).minus(7, ChronoUnit.MINUTES).get(ChronoUnit.SECONDS), TimeUnit.SECONDS);

            System.out.println(obj.get("type").getAsString() + " over " + String.format("%d days %02d:%02d:%02d h", s2 / 3600 / 24, (s2 / 3600) % 24, (s2 % 3600) / 60, (s2 % 60)));
        };
    }

    public String getEventName(String key)
    {
        switch (key)
        {
            case "zoo":
                return "Traveling Zoo arrives";
            case "magmaBoss":
                return "Magma Boss spawns";
            case "jerryWorkshopEvent":
                return "Jerry's Workshop opens";
            case "darkAuction":
                return "Dark Auction starts";
            case "newYear":
                return "It's New Year";
            case "spookyFestival":
                return "Spooky Spooky festival";
            case "winterEvent":
                return "The Winter starts";
        }
        return "";
    }
}
