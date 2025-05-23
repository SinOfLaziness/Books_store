package com.github.Books_store.userInterface.vk;

import com.github.Books_store.hiddenConstants;
import com.vk.api.sdk.actions.Messages;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import org.springframework.stereotype.Component;

@Component
public class vkBot {
    private final VkApiClient vkClient;
    private final GroupActor groupActor;
    private final vkUpdateHandler handler;

    // Spring подставит сюда ваш vkUpdateHandler, который сам инжектит response
    public vkBot(vkUpdateHandler handler) {
        this.handler = handler;

        TransportClient transport = HttpTransportClient.getInstance();
        this.vkClient = new VkApiClient(transport);
        this.groupActor = new GroupActor(
                hiddenConstants.VK_GROUP_ID,
                hiddenConstants.VK_TOKEN
        );
    }

    /**
     * Вызывается из VkLongPollService при получении нового сообщения.
     */
    public void onMessageReceived(Message msg) throws Exception {
        handler.handleMessage(msg);
    }

    /**
     * Отправить простое текстовое сообщение.
     */
    public void sendMessage(String peerId, String text) throws Exception {
        Messages api = vkClient.messages();
        api.sendDeprecated(groupActor)
                .peerId(Long.parseLong(peerId))
                .message(text)
                .randomId((int)(System.currentTimeMillis() & 0xFFFFFFF))
                .execute();
    }

    /**
     * Отправить сообщение вместе с переданной клавиатурой.
     */
    public void sendMessageWithKeyboard(String peerId, String text, com.vk.api.sdk.objects.messages.Keyboard kb) throws Exception {
        Messages api = vkClient.messages();
        api.sendDeprecated(groupActor)
                .peerId(Long.parseLong(peerId))
                .message(text)
                .keyboard(kb)
                .randomId((int)(System.currentTimeMillis() & 0xFFFFFFF))
                .execute();
    }
}
