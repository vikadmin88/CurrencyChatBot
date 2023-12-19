package org.javacrafters.core;

import org.javacrafters.banking.Bank;
import org.javacrafters.banking.MonoBank;
import org.javacrafters.banking.NbuBank;
import org.javacrafters.banking.PrivatBank;
import org.javacrafters.networkclient.NetworkStreamReader;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
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
                String messageText = update.getMessage().getText();
                if (messageText.equals("/start")) {
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
                }else if (isTimeSelection(messageText)) {
                    // Обработка выбранного времени
                    handleTimeSelection(chatId, messageText);
                }
            }

             //Callbacks processing
            if (update.hasCallbackQuery()) {
                String data = update.getCallbackQuery().getData();
                User user = getUser(chatId);
                Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

                switch (data) {
                    //Main callbacks
                    case "settings", "to_settings" -> sendApiMethodAsync(dialogHandler.createSettingMessage(chatId));
                    case "get_info" -> sendApiMethodAsync(dialogHandler.createInfoMessage(user, chatId));
                    case "to_main" -> sendApiMethodAsync(dialogHandler.createWelcomeMessage(chatId));

                    //Setting callbacks
                    case "bank" -> sendApiMethodAsync(dialogHandler.createBankMessage(user, chatId));
                    case "currencies" -> sendApiMethodAsync(dialogHandler.createCurrencyMessage(user, chatId));
                    case "decimal_places" -> sendApiMethodAsync(dialogHandler.createDecimalMessage(user, chatId));
                    case "notification_time" -> sendApiMethodAsync(dialogHandler.createSetNotifyMessage(chatId));


                    //Bank callback
//                    case "nbu", "mono", "private" -> {
//                        toggleBank(user, data.toUpperCase());
//                        EditMessageText newMessage = dialogHandler.updateBankSelectionMessage(chatId, messageId, user);
//                        try {
//                            execute(newMessage);
//                        } catch (TelegramApiException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
                    //Currency callback
                    case "usd", "eur" -> {
                        toggleCurrency(user, data.toUpperCase());
                        EditMessageText newMessage = dialogHandler.updateCurrencySelectionMessage(chatId, messageId, user);
                        try {
                            execute(newMessage);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    //Digits callback
                    case "two_digits", "three_digits", "four_digits" -> {
                        toggleDigit(user, data);
                        EditMessageText newMessage = dialogHandler.updateNumOfDigitsSelectionMessage(chatId, messageId, user);
                        try {
                            execute(newMessage);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        // Mетод для переключения банков
//        private void toggleBank(User user, String digits) {
//            if (user.getBank().getLocalName().toLowerCase().equals(digits)) {
//                user.setBank(null);
//            } else {
//                user.setBank(switch (digits) {
//                    case "nbu" -> new NbuBank();
//                    case "mb" -> new MonoBank();
//                    default -> new PrivatBank();
//                });
//            }
//        }
        // Mетод для переключения знаков полсе запятой
        private void toggleDigit(User user, String digits) {
            int dig = switch (digits) {
                case "three_digits" -> 3;
                case "four_digits" -> 4;
                default -> 2;
            };
            if (user.getNumOfDigits()==dig) {
                user.setNumOfDigits(0);
            } else {
                user.setNumOfDigits(dig);
            }
        }
        // Mетод для переключения валюты
        private void toggleCurrency(User user, String currency) {
            if (user.getCurrencies().contains(currency)) {
                user.getCurrencies().remove(currency);
            } else {
                user.getCurrencies().add(currency);
            }
        }
        // Метод для проверки, является ли текст сообщения выбором времени
        private boolean isTimeSelection(String messageText) {
            return messageText.matches("\\d{1,2}:00")||
                    messageText.equals(new String("Повернутись до налаштування".getBytes(), StandardCharsets.UTF_8))||
                    messageText.equals(new String("Вимкнути сповіщення".getBytes(), StandardCharsets.UTF_8));
        }

        // Метод для обработки выбора времени
        private void handleTimeSelection(Long chatId, String selectedTime) {
            User user = getUser(chatId);
            if (selectedTime.equals(new String("Повернутись до налаштування".getBytes(), StandardCharsets.UTF_8))){
                SendMessage settingMessage = dialogHandler.createSettingMessage(chatId);
                SendMessage infromMessage = dialogHandler.createMessage("Налаштування відправки повідомлень були прийняті", chatId);

                infromMessage.setReplyMarkup(new ReplyKeyboardRemove(true));

                sendApiMethodAsync(infromMessage);
                sendApiMethodAsync(settingMessage);
            } else {
                sendApiMethodAsync(dialogHandler.createMessage("Встановленный час: "+selectedTime, chatId));
                if (selectedTime.length()==4){
                    user.setNotifyTime(Integer.parseInt(selectedTime.substring(0,1)));
                }else {
                    user.setNotifyTime(Integer.parseInt(selectedTime.substring(0,2)));
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
    }