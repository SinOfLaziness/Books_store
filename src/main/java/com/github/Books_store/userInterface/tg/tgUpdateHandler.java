    package com.github.Books_store.userInterface.tg;

    import static com.github.Books_store.userInterface.templatesMessage.*;

    import com.github.Books_store.model.response;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Component;
    import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
    import org.telegram.telegrambots.meta.api.objects.Update;

    @Component
    public class tgUpdateHandler {

        private final response response;
        private static final String TG = "tg";

        @Autowired
        public tgUpdateHandler(response response) {
            this.response = response;
        }

        /**
         * Основной метод обработки апдейтов бота
         */
        public void handleUpdate(Update update) throws Exception {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update);
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            }
        }

        private void handleTextMessage(Update update) throws Exception {
            String data = update.getMessage().getText().toLowerCase();
            Long chatId = update.getMessage().getChatId();

            // Обработка состояний регистрации
            String status = response.getStatus(chatId);
            if ("awaiting_code".equals(status)) {
                response.loginByCode(chatId, TG, data);
                response.setStatus(chatId, "main_menu");
                return;
            }
            if ("login".equals(status)) {
                response.processLoginInput(chatId, TG, data);
                return;
            } else if ("password".equals(status)) {
                response.processPasswordInput(chatId, TG, data);
                return;
            }

            // Обычные команды
            switch (data) {
                case "/start" -> response.startCommand(chatId, TG);
                case "/help"  -> response.help(chatId, TG);
                default       -> {
                    if (data.startsWith("купить ")) {
                        try {
                            int bookId = Integer.parseInt(data.substring(6).trim());
                            response.buyBook(chatId, TG, bookId);
                        } catch (NumberFormatException e) {
                            response.sendMsg(chatId, "Неверный формат команды. Используйте: купить [номер книги]", TG);
                        }
                    } else if ("awaiting_book_id".equals(response.getStatus(chatId)) || "awaiting_quantity".equals(response.getStatus(chatId))) {
                        response.addToCartFlow(chatId, TG, data.trim());
                    } else {
                        response.unknown(chatId, data, TG);
                    }
                }
            }
        }

        private void handleCallbackQuery(Update update) throws Exception {
            CallbackQuery cq = update.getCallbackQuery();
            String data = cq.getData();
            Long chatId = cq.getMessage().getChatId();

            switch (data) {
                case "opt1"      -> response.option1Callback(chatId, TG);
                case "opt2"      -> response.option2Callback(chatId, TG);
                case "logout"    -> response.unlogging(chatId, TG);
                case "list_books"   -> response.listBooks(chatId, TG);
                case "cart"         -> response.showCart(chatId, TG);
                case "purchases"    -> response.showPurchases(chatId, TG);
                case "add_to_cart"  -> {
                    response.sendMsg(chatId,"Введите ID книги для добавления в корзину:", TG);
                    response.setStatus(chatId, "awaiting_book_id");
                }
                case "purchase_cart"-> response.purchaseCart(chatId, TG);
                case "clear_cart"   -> response.clearCart(chatId, TG);
                case "sign_up"      -> response.requestLogin(chatId, TG);
                case "back"         -> response.startCommand(chatId, TG);
                case "get_code"     -> response.requestOneTimeCode(chatId, TG);
                case "login_by_code" -> response.promptOneTimeCode(chatId, TG);
                default              -> {
                    if (data.startsWith("купить ")) {
                        try {
                            int bookId = Integer.parseInt(data.substring(6).trim());
                            response.buyBook(chatId, TG, bookId);
                        } catch (NumberFormatException e) {
                            response.sendMsg(chatId, "Неверный формат команды. Используйте: купить [номер книги]", TG);
                        }
                    } else if ("awaiting_book_id".equals(response.getStatus(chatId)) || "awaiting_quantity".equals(response.getStatus(chatId))) {
                        response.addToCartFlow(chatId, TG, data.trim());
                    } else {
                        response.unknown(chatId, data, TG);
                    }
                }
            }
        }
    }