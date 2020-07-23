package com.tvdp.hypixeleventcalendar;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Color;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class CalendarRetriever
{
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler;
    private final HttpClient httpClient;

    public static Map<String, List<Snowflake>> subscribers = new HashMap<>();

    public CalendarRetriever()
    {
        this.executorService = Executors.newCachedThreadPool();
        this.scheduler = Executors.newScheduledThreadPool(0);
        this.httpClient = HttpClientBuilder.create().build();
    }

    public void requestTimers()
    {
        getTimerForType("zoo/estimate").whenComplete(scheduleTimer());
        //getTimerForType("bosstimer/magma/estimatedSpawn").whenComplete(scheduleTimer());
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
                        if (content.equals("")) {
                            return new JsonObject();
                        } else {
                            return new Gson().fromJson(content, JsonObject.class);
                        }
                    });

                    response.addProperty("urlLink", type);

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

            if (!obj.has("success")) return;

            Instant end = Instant.ofEpochMilli(obj.get("estimate").getAsLong());
            Instant now = Instant.now();
            Duration duration = Duration.between(now, end);
            long s = duration.getSeconds();
            long minutes = duration.toMinutes();

            AtomicReference<JsonObject> objRef = new AtomicReference<>(obj);

            if (minutes >= 30) {
                scheduler.schedule(() -> print30minutes(objRef), duration.minus(30, ChronoUnit.MINUTES).getSeconds(), TimeUnit.SECONDS);
            }


            if (minutes > 10) {
                scheduler.schedule(() -> print10minutes(objRef), duration.minus(10, ChronoUnit.MINUTES).getSeconds(), TimeUnit.SECONDS);
            }

            scheduler.schedule(() -> printStarted(objRef), duration.getSeconds(), TimeUnit.SECONDS);

            System.out.println(obj.get("type").getAsString() + " over " + String.format("%d days %02d:%02d:%02d h", s / 3600 / 24, (s / 3600) % 24, (s % 3600) / 60, (s % 60)));
        };
    }

    public void print30minutes(AtomicReference<JsonObject> objRef)
    {
        JsonObject obj = objRef.get();

        String message = getEventName(obj.get("type").getAsString()) + " in 30 minutes!";
        /*if (!obj.get("type").getAsString().equals("darkAuction")) {
            //HypixelEventCalendarBot.botChannel.flatMap(messageChannel -> messageChannel.createEmbed(embedCreateSpec -> embedCreateSpec.setColor(Color.GREEN).setTitle(message))).subscribe();
        }*/

        subscribers.forEach((option, snowflakes) -> {
            if (obj.get("type").getAsString().equals(option)) {
                snowflakes.forEach(snowflake -> HypixelEventCalendarBot.client.getUserById(snowflake)
                        .flatMap(User::getPrivateChannel)
                        .flatMap(privateChannelMono -> privateChannelMono.createEmbed(embedCreateSpec -> embedCreateSpec.setColor(Color.GREEN).setTitle(message))).subscribe());
            }
        });

        getTimerForType(obj.get("urlLink").getAsString()).whenComplete((response, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }
            if (!response.has("success")) return;
            System.out.println("Updated in 30 last minutes: " + response.get("type").getAsString());
            objRef.set(response);
        });
    }

    public void print10minutes(AtomicReference<JsonObject> objRef)
    {
        JsonObject obj = objRef.get();

        String message = getEventName(obj.get("type").getAsString()) + " in 10 minutes!";
        /*if (!obj.get("type").getAsString().equals("darkAuction")) {
            HypixelEventCalendarBot.botChannel.flatMap(messageChannel -> messageChannel.createEmbed(embedCreateSpec -> embedCreateSpec.setColor(Color.GREEN).setTitle(message))).subscribe();
        }*/

        subscribers.forEach((option, snowflakes) -> {
            if (obj.get("type").getAsString().equals(option)) {
                snowflakes.forEach(snowflake -> HypixelEventCalendarBot.client.getUserById(snowflake)
                        .flatMap(User::getPrivateChannel)
                        .flatMap(privateChannelMono -> privateChannelMono.createEmbed(embedCreateSpec -> embedCreateSpec.setColor(Color.GREEN).setTitle(message))).subscribe());
            }
        });

        getTimerForType(obj.get("urlLink").getAsString()).whenComplete((response, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }
            if (!response.has("success")) return;
            System.out.println("Updated in ten last minutes: " + response.get("type").getAsString());
            objRef.set(response);
        });
    }

    public void printStarted(AtomicReference<JsonObject> objRef)
    {
        JsonObject obj = objRef.get();

        if (Math.abs(Duration.between(Instant.now(), Instant.ofEpochMilli(obj.get("estimate").getAsLong())).getSeconds()) < 2)
        {
            System.out.println(obj.get("urlLink").getAsString());
            executorService.submit(() -> {
                this.getTimerForType(obj.get("urlLink").getAsString()).whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        return;
                    }

                    if (!result.has("success")) return;


                    String message = getEventName(obj.get("type").getAsString()) + " NOW!";
                    /*AtomicReference<String> message = new AtomicReference<>(getEventName(obj.get("type").getAsString()) + " NOW");
                    if (result.get("endEstimate").getAsLong() > obj.get("estimate").getAsLong()) {
                        Duration endsIn = Duration.between(Instant.ofEpochMilli(result.get("estimate").getAsLong()), Instant.ofEpochMilli(result.get("endEstimate").getAsLong()));
                        long s = endsIn.getSeconds();
                        message.set(message.get() + " and ends in about " + String.format("%02d:%02d:%02d h!", (s / 3600) % 24, (s % 3600) / 60, (s % 60)));
                    } else {
                        message.set(message.get() + "!");
                    }*/
                    /*if (!obj.get("type").getAsString().equals("darkAuction")) {
                        HypixelEventCalendarBot.botChannel.flatMap(messageChannel -> messageChannel.createEmbed(embedCreateSpec -> embedCreateSpec.setColor(Color.GREEN).setTitle(message))).subscribe();
                    }*/

                    subscribers.forEach((option, snowflakes) -> {
                        if (obj.get("type").getAsString().equals(option)) {
                            snowflakes.forEach(snowflake -> HypixelEventCalendarBot.client.getUserById(snowflake)
                                    .flatMap(User::getPrivateChannel)
                                    .flatMap(privateChannelMono -> privateChannelMono.createEmbed(embedCreateSpec -> embedCreateSpec.setColor(Color.GREEN).setTitle(message))).subscribe());
                        }
                    });
                });
            });
        }

        //restart
        scheduler.schedule(() -> {
            //System.out.println("Rescheduling timer!");
            this.getTimerForType(obj.get("urlLink").getAsString()).whenComplete(this.scheduleTimer());
        }, 10, TimeUnit.SECONDS);
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
