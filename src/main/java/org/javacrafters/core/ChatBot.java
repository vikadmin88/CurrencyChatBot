package org.javacrafters.core;

import org.javacrafters.banking.NormalizeCurrencyPair;
import org.javacrafters.banking.PrivatBank;
import org.javacrafters.networkclient.NetworkStreamReader;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.nio.charset.StandardCharsets;
import java.util.*;



    public class ChatBot extends TelegramLongPollingBot {

        private String appName;
        private String botName;
        private String botToken;
        private Map<Long, User> users;
        private final Map<Integer, String> messages = new HashMap<>();
        private final Map<Integer, Map<String, String>> buttonMessages = new HashMap<>();
        private final BotDialogHandler dialogHandler = new BotDialogHandler();

        public ChatBot() {

        }

        public ChatBot(Map<Long, User> users, String appName, String botName, String botToken) {
            this.users = users;
            this.botName = botName;
            this.botToken = botToken;
        }

        @Override
        public String getBotUsername() {
            return this.botName;
        }

        @Override
        public String getBotToken() {

            return this.botToken;
        }

        public void botRun() {
            TelegramBotsApi api = null;
            try {
                api = new TelegramBotsApi(DefaultBotSession.class);
                api.registerBot(new ChatBot(users, appName, botName, botToken));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        public void addUser(User user) {
            users.put(user.getId(), user);
        }

        public User getUser(Long id) {
            return users.get(id);
        }

        public Map<Long, User> getUsers() {
            return users;
        }

        public void userNotify(User user) {
            System.out.println("userNotify() = " + user.getId() + " " + user.getName());

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(user.getId()));

            String message = dialogHandler.createNotifyMessage(user);
            if (message != null) {
                sendMessage.setText(message);
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {

            }

        }



        @Override
        public void onUpdateReceived(Update update) {
            Long chatId = getChatId(update);

            // Messages processing
            if (update.hasMessage()) {
                if (update.getMessage().getText().equals("/start")) {
//                    sendMessage(chatId);

                    if (getUser(chatId) == null) {
                        User user = new User(chatId, update.getMessage().getFrom().getFirstName(), update.getMessage().getFrom().getUserName());
                        user.setBank(new PrivatBank(new NetworkStreamReader()));
                        user.addCurrency("USD");
                        user.addCurrency("EUR");
                        user.setScheduledTask(new Scheduler().schedule(this, user, 15));
                        addUser(user);
                    }
                    // while testing
                    userNotify(getUser(chatId));
                    System.out.printf("Next notify will be sent in %d minutes...", (60 - Calendar.getInstance().get(Calendar.MINUTE)));
                }
            }

            // Callbacks processing
            if (update.hasCallbackQuery()) {

                if (update.getCallbackQuery().getData().equals("level_1_task")) {
                    sendMessage(chatId);
                }
            }
        }

        public Long getChatId(Update update) {
            if (update.hasMessage()) {
                return update.getMessage().getFrom().getId();
            }
            if (update.hasCallbackQuery()) {
                return update.getCallbackQuery().getFrom().getId();
            }
            return null;
        }



        public void attachButtons(SendMessage message, Map<String, String> buttons) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            for (String buttonName : buttons.keySet()) {
                String buttonValue = buttons.get(buttonName);
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(new String(buttonName.getBytes(), StandardCharsets.UTF_8));
                button.setCallbackData(buttonValue);
                keyboard.add(Arrays.asList(button));
            }

            markup.setKeyboard(keyboard);
            message.setReplyMarkup(markup);
        }

//        public void sendImage(String name, Long chatId) {
//            SendAnimation animation = new SendAnimation();
//            InputFile inputFile = new InputFile();
//            inputFile.setMedia(new File("images/" + name + ".gif"));
//            animation.setAnimation(inputFile);
//            animation.setChatId(chatId);
//            executeAsync(animation);
//        }

//        {
//            messages.put(1, "*Джавелін твій. Повний вперед!*");
//        }
//
//        {
//            buttonMessages.put(1, Map.of(
//                    "Купити Джавелін (50 монет)", "level_4_task"
//            ));
//        }

        public void sendMessage(Long chatId) {
            SendMessage message = dialogHandler.createMessage(messages.get(chatId));
            message.setChatId(chatId);

            Map<String, String> messageCommand = new HashMap<>();
            messageCommand.put("Налаштування", "get_conf");
            messageCommand.put("Отримати інформацію", "get_info");
            attachButtons(message, messageCommand);

            sendApiMethodAsync(message);
        }


//    private final Map<Integer, Map<String, String>> buttonMessages = new HashMap<>();
//    {
//            buttonMessages.put(1, Map.of(
//            "Сплести маскувальну сітку (+15 монет)", "level_1_task",
//            "Зібрати кошти патріотичними піснями (+15 монет)", "level_1_task",
//            "Вступити в Міністерство Мемів України (+15 монет)", "level_1_task",
//            "Запустити волонтерську акцію (+15 монет)","level_1_task",
//            "Вступити до лав тероборони (+15 монет)","level_1_task"
//            ));

    }