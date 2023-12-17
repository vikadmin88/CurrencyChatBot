package org.javacrafters.core;

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

            String message = dialogHandler.getCurrencyRate(user);
            if (message != null) {
                SendMessage message1 = dialogHandler.createMessage(message, user.getId());
                sendApiMethodAsync(message1);
            }

        }

        @Override
        public void onUpdateReceived(Update update) {
            Long chatId = getChatId(update);

            // Messages processing
            if (update.hasMessage()) {
                if (update.getMessage().getText().equals("/start")) {
                    sendApiMethodAsync(dialogHandler.createWelcomeMessage(chatId));

                    if (getUser(chatId) == null) {
                        User user = new User(chatId, update.getMessage().getFrom().getFirstName(), update.getMessage().getFrom().getUserName());
                        user.setBank(new PrivatBank(new NetworkStreamReader()));
                        user.addCurrency("USD");
                        user.addCurrency("EUR");
                        user.setScheduledTask(new Scheduler().schedule(this, user, 15));
                        addUser(user);
                    }
                    // while testing
                    //userNotify(getUser(chatId));
                    System.out.printf("Next notify will be sent in %d minutes...", (60 - Calendar.getInstance().get(Calendar.MINUTE)));
                }
            }

             //Callbacks processing
            if (update.hasCallbackQuery()) {
                String data = update.getCallbackQuery().getData();
                //Main callbacks
                if (data.equals("settings")) {
                    sendApiMethodAsync(dialogHandler.createSettingMessage(chatId));

                }else if (data.equals("get_info")) {
                    sendApiMethodAsync(dialogHandler.createInfoMessage(getUser(chatId), chatId));

                }else if(data.equals("to_main")){
                    sendApiMethodAsync(dialogHandler.createWelcomeMessage(chatId));
                }
                //Setting callbacks
                else if (data.equals("bank")) {
                    sendApiMethodAsync(dialogHandler.createBankMessage(chatId));
                }else if (data.equals("currencies")) {
                    sendApiMethodAsync(dialogHandler.createCurrencyMessage(chatId));
                }else if (data.equals("decimal_places")){
                    sendApiMethodAsync(dialogHandler.createDecimalMessage(chatId));
                }//else if (data.equals("currencies")){
//                    sendApiMethodAsync(dialogHandler.createBankMessage(chatId));
//                }
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
    }