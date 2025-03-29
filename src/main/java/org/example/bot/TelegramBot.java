package org.example.bot;

import org.example.model.Book;
import org.example.model.User;
import org.example.service.BookService;
import org.example.service.CartService;
import org.example.repository.UserRepository;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.username}")
    private String botUsername;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private BookService bookService;

    @Autowired
    private CommandHandler commandHandler;

    // Храним текущую страницу для каждого пользователя (chatId -> номер страницы)
    private Map<Long, Integer> userPageMap = new ConcurrentHashMap<>();

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Если пришёл callback от inline-кнопок
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
            return;
        }

        // Обработка обычных текстовых сообщений
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText().trim();

            // Проверяем, существует ли пользователь, иначе создаём
            User user = userRepository.findByChatId(chatId);
            if (user == null) {
                user = new User(chatId, "User_" + chatId, "dummy");
                userRepository.save(user);
            }

            // Если команда "Список книг" — запускаем пагинацию (страница 0)
            if (text.equalsIgnoreCase("Список книг")) {
                userPageMap.put(chatId, 0);
                sendBooksPage(chatId, 0);
                return;
            }

            // Остальные команды обрабатываем через CommandHandler
            String response = commandHandler.handleCommand(text, chatId.toString());
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(response);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Обрабатывает callback-запросы от inline-кнопок, удаляя предыдущее сообщение.
     */
    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData(); // ожидаем "books_next" или "books_prev"
        Long chatId = callbackQuery.getMessage().getChatId();

        int currentPage = userPageMap.getOrDefault(chatId, 0);
        int newPage = currentPage;
        if (data.equals("books_next")) {
            newPage = currentPage + 1;
        } else if (data.equals("books_prev")) {
            newPage = Math.max(currentPage - 1, 0);
        }
        userPageMap.put(chatId, newPage);

        // Удаляем предыдущее сообщение, чтобы не засорять чат
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId.toString());
        deleteMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        // Отправляем новую страницу книг
        sendBooksPage(chatId, newPage);

        // Отправляем ответ на callback, чтобы убрать "крутящийся" индикатор
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQuery.getId());
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправляет страницу книг с пагинацией (по 15 книг на страницу).
     *
     * @param chatId идентификатор чата пользователя
     * @param page   номер страницы (начинается с 0)
     */
    private void sendBooksPage(Long chatId, int page) {
        // Получаем все книги и сортируем их по ID
        List<Book> books = bookService.getAllBooks();
        books.sort(Comparator.comparingLong(Book::getId));

        int pageSize = 15;
        int totalBooks = books.size();
        int totalPages = (int) Math.ceil(totalBooks / (double) pageSize);

        // Корректируем номер страницы, если вышел за пределы
        if (page >= totalPages) {
            page = totalPages - 1;
        }
        if (page < 0) {
            page = 0;
        }

        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalBooks);
        List<Book> pageBooks = books.subList(startIndex, endIndex);

        // Формируем текст сообщения
        StringBuilder sb = new StringBuilder();
        sb.append("Список книг (страница ").append(page + 1).append(" из ").append(totalPages).append("):\n\n");
        int count = 1;
        for (Book book : pageBooks) {
            sb.append(count++).append(") [ID: ").append(book.getId()).append("]\n")
                    .append("   Название: ").append(book.getTitle()).append("\n")
                    .append("   Автор: ").append(book.getAuthor()).append("\n")
                    .append("   Цена: ").append(book.getPrice()).append("\n")
                    .append("   В наличии: ").append(book.getQuantity() == null ? 0 : book.getQuantity())
                    .append("\n\n");
        }

        // Создаем inline-клавиатуру с кнопками "Назад" и "Вперёд"
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton btnPrev = new InlineKeyboardButton();
        btnPrev.setText("<< Назад");
        btnPrev.setCallbackData("books_prev");
        row.add(btnPrev);

        InlineKeyboardButton btnNext = new InlineKeyboardButton();
        btnNext.setText("Вперёд >>");
        btnNext.setCallbackData("books_next");
        row.add(btnNext);

        rows.add(row);
        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(sb.toString());
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
