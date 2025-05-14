package com.github.Books_store.userInterface.vk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Books_store.model.response;
import com.vk.api.sdk.objects.messages.Message;
import org.springframework.stereotype.Component;

@Component
public class vkUpdateHandler {

    private final response resp;
    private static final String VK = "vk";

    public vkUpdateHandler(response resp) {
        this.resp = resp;
    }

    public void handleMessage(Message msg) throws Exception {
        Long peerId = msg.getPeerId().longValue();

        String payload = msg.getPayload();

        String status = resp.getStatus(peerId);
        if ("login".equals(status)) {
            resp.processLoginInput(peerId, VK, msg.getText().trim());
            return;
        }
        if ("password".equals(status)) {
            resp.processPasswordInput(peerId, VK, msg.getText().trim());
            return;
        }
        if ("awaiting_book_id".equals(status) || "awaiting_quantity".equals(status)) {
            resp.addToCartFlow(peerId, VK, msg.getText().trim());
            return;
        }

        String command;

        if (payload != null && !payload.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(payload);
            command = node.has("command") ? node.get("command").asText().toLowerCase() : null;
        } else {
            command = msg.getText().trim().toLowerCase();
        }


        if (command == null) {
            resp.unknown(peerId, "", VK);
            return;
        }

        switch (command) {
            case "/start", "начать" -> resp.startCommand(peerId, VK);
            case "список книг", "list_books" -> resp.listBooks(peerId, VK);

            case "add_to_cart", "добавить в корзину" -> {
                resp.sendMsg(peerId, "Введите ID книги для добавления в корзину:", VK);
                resp.setStatus(peerId, "awaiting_book_id");
            }

            case "корзина", "cart" -> resp.showCart(peerId, VK);
            case "purchase_cart", "купить всю корзину" -> resp.purchaseCart(peerId, VK);
            case "clear_cart", "очистить корзину" -> resp.clearCart(peerId, VK);
            case "sign_up", "зарегистрироваться" -> resp.requestLogin(peerId, VK);
            case "назад", "back" -> resp.startCommand(peerId, VK);

            case "опция 1", "opt1" -> resp.option1Callback(peerId, VK);
            case "опция 2", "opt2" -> resp.option2Callback(peerId, VK);

            case "выйти из аккаунта", "logout" -> resp.unlogging(peerId, VK);

            case "покупки", "purchases" -> resp.showPurchases(peerId, VK);

            default -> {
                if (command.startsWith("купить ")) {
                    try {
                        int bookId = Integer.parseInt(command.substring(6).trim());
                        resp.buyBook(peerId, VK, bookId);
                    } catch (NumberFormatException e) {
                        resp.sendMsg(peerId, "Неверный формат команды. Используйте: купить [номер книги]", VK);
                    }
                } else if ("awaiting_book_id".equals(resp.getStatus(peerId)) || "awaiting_quantity".equals(resp.getStatus(peerId))) {
                    resp.addToCartFlow(peerId, VK, msg.getText().trim());
                } else {
                    resp.unknown(peerId, command, VK);
                }
            }
        }
    }
}
