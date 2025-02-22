package com.zenith.feature.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Suppliers;
import com.zenith.feature.queue.mcping.MCPing;
import com.zenith.feature.queue.mcping.PingOptions;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zenith.Shared.*;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Objects.isNull;

public class Queue {
    private static final String apiUrl = "https://api.2b2t.vc";
    private static final Supplier<HttpClient> httpClient = Suppliers.memoize(() -> HttpClient.create()
        .secure()
        .baseUrl(apiUrl)
        .headers(h -> h.add(HttpHeaderNames.ACCEPT, HttpHeaderValues.APPLICATION_JSON))
        .headers(h -> h.add(HttpHeaderNames.USER_AGENT, "ZenithProxy/" + LAUNCH_CONFIG.version)));
    private static QueueStatus queueStatus;
    private static final Pattern digitPattern = Pattern.compile("\\d+");
    private static final MCPing mcPing = new MCPing();
    private static final PingOptions pingOptions = new PingOptions();
    static {
        pingOptions.setHostname("connect.2b2t.org");
        pingOptions.setPort(25565);
        pingOptions.setTimeout(3000);
        pingOptions.setProtocolVersion(763);
        pingOptions.setResolveDns(false);
    }

    public static void start() {
        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(
            () -> Thread.ofVirtual().name("Queue Update").start(Queue::updateQueueStatus),
            500L,
            Duration.of(CONFIG.server.queueStatusRefreshMinutes, MINUTES).toMillis(),
            TimeUnit.MILLISECONDS);
    }

    public static QueueStatus getQueueStatus() {
        if (isNull(queueStatus)) {
            updateQueueStatus();
        }
        return queueStatus;
    }

    public static void updateQueueStatus() {
        if (!pingUpdate()) {
            if (!apiUpdate()) {
                SERVER_LOG.error("Failed updating queue status. Is the network down?");
                if (isNull(queueStatus)) {
                    queueStatus = new QueueStatus(0, 0, Instant.EPOCH.getEpochSecond());
                }
            }
        }
    }

    // probably only valid for regular queue, prio seems to move a lot faster
    // returns double representing seconds until estimated queue completion time.
    public static double getQueueWait(final Integer queuePos) {
        return 87.0 * (Math.pow(queuePos.doubleValue(), 0.962));
    }

    public static String getEtaStringFromSeconds(final double totalSeconds) {
        final int hour = (int) (totalSeconds / 3600);
        final int minutes = (int) ((totalSeconds / 60) % 60);
        final int seconds = (int) (totalSeconds % 60);
        final String hourStr = hour >= 10 ? "" + hour : "0" + hour;
        final String minutesStr = minutes >= 10 ? "" + minutes : "0" + minutes;
        final String secondsStr = seconds >= 10 ? "" + seconds : "0" + seconds;
        return hourStr + ":" + minutesStr + ":" + secondsStr;
    }

    public static String getQueueEta(final Integer queuePos) {
        return getEtaStringFromSeconds(getQueueWait(queuePos));
    }

    public static boolean pingUpdate() {
        try {
            final MCPing.ResponseDetails pingWithDetails = mcPing.getPingWithDetails(pingOptions);
            final String queueStr = pingWithDetails.standard.getPlayers().getSample().get(1).getName();
            final Matcher regularQMatcher = digitPattern.matcher(queueStr.substring(queueStr.lastIndexOf(" ")));
            final String prioQueueStr = pingWithDetails.standard.getPlayers().getSample().get(2).getName();
            final Matcher prioQMatcher = digitPattern.matcher(prioQueueStr.substring(prioQueueStr.lastIndexOf(" ")));
            if (!queueStr.contains("Queue")) {
                throw new IOException("Queue string doesn't contain Queue: " + queueStr);
            }
            if (!prioQueueStr.contains("Priority")) {
                throw new IOException("Priority queue string doesn't contain Priority: " + prioQueueStr);
            }
            if (!regularQMatcher.find()) {
                throw new IOException("didn't find regular queue len: " + queueStr);
            }
            if (!prioQMatcher.find()) {
                throw new IOException("didn't find priority queue len: " + prioQueueStr);
            }
            final int regular = Integer.parseInt(regularQMatcher.group());
            final int prio = Integer.parseInt(prioQMatcher.group());
            queueStatus = new QueueStatus(prio, regular, ZonedDateTime.now().toEpochSecond());
            return true;
        } catch (final Exception e) {
            SERVER_LOG.error("Failed updating queue with ping", e);
            return false;
        }
    }

    private static boolean apiUpdate() {
        try {
            final String response = httpClient.get()
                .get()
                .uri("/queue")
                .responseContent()
                .aggregate()
                .asString()
                .block();
            QueueApiResponse queueApiResponse = OBJECT_MAPPER.readValue(response, QueueApiResponse.class);
            queueStatus = new QueueStatus(queueApiResponse.prio, queueApiResponse.regular, queueApiResponse.time.toEpochSecond());
            return true;
        } catch (final Exception e) {
            SERVER_LOG.error("Failed updating queue status from API", e);
            return false;
        }
    }

    private static class QueueApiResponse {
        @JsonProperty("prio") public Integer prio;
        @JsonProperty("regular") public Integer regular;
        @JsonProperty("time") public OffsetDateTime time;
    }
}
