// src/main/java/com/github/Books_store/userInterface/vk/vkTools.java
package com.github.Books_store.userInterface.vk;

import com.github.Books_store.hiddenConstants;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.springframework.stereotype.Component;

@Component
public class vkTools {
    private final VkApiClient vkClient;
    private final GroupActor groupActor;

    public vkTools() {
        TransportClient transport = HttpTransportClient.getInstance();
        this.vkClient   = new VkApiClient(transport);
        this.groupActor = new GroupActor(
                hiddenConstants.VK_GROUP_ID,
                hiddenConstants.VK_TOKEN
        );
    }

    // Отправка простого текстового сообщения
    public void sendMessage(long peerId, String text) throws Exception {
        vkClient.messages()
                .sendDeprecated(groupActor)
                .peerId(peerId)
                .message(text)
                .randomId((int)(System.currentTimeMillis() & 0xFFFFFFF))
                .execute();
    }

    // Отправка сообщения с «сырым» JSON-клавиатурой
    public void sendMessageWithKeyboard(long peerId, String text, String keyboardJson) throws Exception {
        vkClient.messages()
                .sendDeprecated(groupActor)
                .peerId(peerId)
                .message(text)
                // вместо .keyboard(keyboardJson):
                .unsafeParam("keyboard", keyboardJson)
                .randomId((int)(System.currentTimeMillis() & 0xFFFFFFF))
                .execute();
    }
}
