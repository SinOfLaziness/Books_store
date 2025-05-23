package com.github.Books_store.userInterface.vk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Books_store.hiddenConstants;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.responses.GetLongPollServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class VkLongPollService {
    private static final Logger LOG = LoggerFactory.getLogger(VkLongPollService.class);

    private final VkApiClient vkClient;
    private final GroupActor groupActor;
    private final vkTools vkTools;
    private final vkUpdateHandler handler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public VkLongPollService(vkTools vkTools, vkUpdateHandler handler) {
        TransportClient transport = HttpTransportClient.getInstance();
        this.vkClient = new VkApiClient(transport);
        this.groupActor = new GroupActor(
                hiddenConstants.VK_GROUP_ID,
                hiddenConstants.VK_TOKEN
        );
        this.vkTools = vkTools;
        this.handler = handler;
    }

    @PostConstruct
    public void start() {
        LOG.info("=== VK LongPoll стартует ===");
        GetLongPollServerResponse srv;
        try {
            srv = vkClient.messages()
                    .getLongPollServer(groupActor)
                    .execute();
        } catch (Exception e) {
            LOG.error("Не удалось получить LongPollServer", e);
            return;
        }

        String server = srv.getServer().startsWith("http")
                ? srv.getServer()
                : "https://" + srv.getServer();
        String key = srv.getKey();
        AtomicInteger ts = new AtomicInteger(srv.getTs());

        Thread poller = new Thread(() -> {
            while (true) {
                try {
                    String url = server
                            + "?act=a_check"
                            + "&key=" + key
                            + "&ts=" + ts.get()
                            + "&wait=25"
                            + "&mode=2"
                            + "&version=5.131";

                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();

                    HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                    JsonNode root = objectMapper.readTree(resp.body());
                    LOG.info(resp.body());
                    ts.set(root.path("ts").asInt());

                    for (JsonNode upd : root.path("updates")) {
                        // v3 массивный формат
                        if (upd.isArray()) {
                            int code = upd.get(0).asInt();
                            if (code != 4) continue;                 // новое сообщение

                            int flags = upd.get(2).asInt();         // флаги: 2=outbox
                            if ((flags & 2) != 0) continue;           // пропустить исходящие

                            long peerId = upd.get(3).asLong();      // peer_id
                            String text  = upd.get(5).asText();     // текст
                            handler.handleMessage(
                                    new com.vk.api.sdk.objects.messages.Message()
                                            .setPeerId(peerId)
                                            .setText(text)
                            );

                            // v5 объектный формат
                        } else if ("message_new".equals(upd.path("type").asText())) {
                            JsonNode msg = upd.path("object").path("message");
                            if (msg.path("outgoing").asBoolean(false)) continue; // пропустить исходящие

                            long peerId = msg.path("peer_id").asLong();
                            String text  = msg.path("text").asText();
                            handler.handleMessage(
                                    new com.vk.api.sdk.objects.messages.Message()
                                            .setPeerId(peerId)
                                            .setText(text)
                            );
                        }
                    }

                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    LOG.error("VK LongPoll прерван", ie);
                    break;
                } catch (Exception ex) {
                    LOG.error("Ошибка в цикле VK LongPoll", ex);
                    try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                }
            }
        }, "VK-LongPoll-Thread");

        poller.setDaemon(false);
        poller.start();
    }
}
