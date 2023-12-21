package org.javacrafters.core;

import org.javacrafters.banking.CurrencyHolder;
import org.javacrafters.banking.NormalizeCurrencyPair;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.nio.charset.StandardCharsets;
import java.util.Map;

    public class ChatBot extends TelegramLongPollingBot {
        private static final Logger logger = LoggerFactory.getLogger(ChatBot.class);
        private BotDialogHandler bd = new BotDialogHandler();
        private final String appName;
        private final String botName;
        private final String botToken;
        private int startMesID;

        public ChatBot(String appName, String botName, String botToken) {
            this.appName = appName;
            this.botName = botName;
            this.botToken = botToken;
        }
        @Override
        public String getBotUsername() { return this.botName; }
        @Override
        public String getBotToken() { return this.botToken; }

        public void botRun() {
            TelegramBotsApi api = null;
            try {
                api = new TelegramBotsApi(DefaultBotSession.class);
                api.registerBot(this);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            AppRegistry.setChatBot(this);
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

        public void addUser(Long chatId, Update update) {
            User user = new User(chatId, update.getMessage().getFrom().getFirstName(), update.getMessage().getFrom().getUserName());
            user.addBank(AppRegistry.getConfBank());
            user.addCurrency(AppRegistry.getConfCurrency());
            user.setCountLastDigits(AppRegistry.getConfCountLastDigits());
            user.setNotifyTime(AppRegistry.getConfNotifyTime());
            user.setNotifyStatus(AppRegistry.getConfNotifyStatus());
            Scheduler.addUserSchedule(chatId, user, AppRegistry.getConfNotifyTime());
            AppRegistry.addUser(user);
            UserLoader.save(user);
        }

        @Override
        public void onUpdateReceived(Update update) {

            Long chatId = getChatId(update);

            // Messages processing
            if (update.hasMessage()) {
                String messageText = update.getMessage().getText();

                logger.trace(messageText, update);
                if (messageText.equals("/start")) {
//                    sendMessage(chatId);
                    startMesID = update.getMessage().getMessageId();
                    sendApiMethodAsync(bd.createWelcomeMessage(chatId));
                    if (!AppRegistry.hasUser(chatId)) {
                        addUser(chatId, update);
                    }
                    // while testing
//                    userNotify(AppRegistry.getUser(chatId));
                }else if (messageText.equals(new String("Налаштування".getBytes(), StandardCharsets.UTF_8))) {
                    sendApiMethodAsync(bd.createSettingsMessage(chatId));
                }else if (messageText.equals(new String("Отримати інформацію".getBytes(), StandardCharsets.UTF_8))){
                    sendApiMethodAsync(bd.createMessage(createNotifyMessage(AppRegistry.getUser(chatId)), chatId));
                }else if (isTimeSelection(messageText)) {
                    // Обработка выбранного времени
                    handleTimeSelection(chatId, messageText);
                }
            }

            // Callbacks processing
            if (update.hasCallbackQuery()) {
                String data = update.getCallbackQuery().getData();
                int messageId = update.getCallbackQuery().getMessage().getMessageId();
                switch (data) {
                    //Main callbacks
                    case "get_info" -> sendApiMethodAsync(bd.createMessage(createNotifyMessage(AppRegistry.getUser(chatId)), chatId));
                    case "settings", "to_settings" -> {
                        try {
                            execute(bd.onSettingMessage(chatId, messageId));
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    case "decimal_places" -> {
                        try {
                            execute(bd.onDecimalMessage(chatId, messageId));
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    case "bank" -> {
                        try {
                            execute(bd.onBankMessage(chatId, messageId));
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    case "currencies" -> {
                        try {
                            execute(bd.onCurrencyMessage(chatId, messageId));
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    case "notification_time" -> {
                        try {
                            execute(bd.createSetNotifyMessage(chatId));
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        // Метод для проверки, является ли текст сообщения выбором времени
        private boolean isTimeSelection(String messageText) {
            return messageText.matches("\\d{1,2}:00")||
                    messageText.equals(new String("До налаштувань".getBytes(), StandardCharsets.UTF_8))||
                    messageText.equals(new String("Вимкнути сповіщення".getBytes(), StandardCharsets.UTF_8));
        }
        // Метод для обработки выбора времени
        private void handleTimeSelection(Long chatId, String selectedTime) {
            User user = AppRegistry.getUser(chatId);
            if (selectedTime.equals(new String("До налаштувань".getBytes(), StandardCharsets.UTF_8))){
                SendMessage settingMessage = bd.createSettingsMessage(chatId);
                SendMessage informMessage = bd.createMessage("Налаштування відправки повідомлень були прийняті", chatId);

                informMessage.setReplyMarkup(bd.getPermanentKeyboard());

                sendApiMethodAsync(informMessage);
                sendApiMethodAsync(settingMessage);
            } else if(selectedTime.equals(new String("Вимкнути сповіщення".getBytes(), StandardCharsets.UTF_8))){
                sendApiMethodAsync(bd.createMessage("Сповіщення вимнкуті", chatId));
                user.setNotifyStatus(false);
            }else {
                sendApiMethodAsync(bd.createMessage("Встановленный час: "+selectedTime, chatId));
                if (selectedTime.length()==4){
                    user.setNotifyTime(Integer.parseInt(selectedTime.substring(0,1)));
                }else {
                    user.setNotifyTime(Integer.parseInt(selectedTime.substring(0,2)));
                }
            }
        }
        public void userNotify(User user) {
            System.out.println("userNotify() = " + user.getId() + " " + user.getName());

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(user.getId()));

            String message = createNotifyMessage(user);
            if (message != null) {
                sendMessage.setText(new String(message.getBytes(), StandardCharsets.UTF_8));
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {

                }
            }

        }

        private String createNotifyMessage(User user) {
            StringBuilder sb = new StringBuilder("Поточні курси валют:\n");

            // {"PB" => {"USD" => {"USD", "36.95000", "37.45000"}}}
            Map<String, Map<String, NormalizeCurrencyPair>> currencyRates = CurrencyHolder.getRates();

            if (currencyRates == null || currencyRates.isEmpty()) {
                return null;
            }

            for (String bankLocalName : user.getBanks()) {
                sb.append(AppRegistry.getBank(bankLocalName).getName()).append("\n");

                // {"USD" => {"USD", "36.95000", "37.45000"}}
                Map<String, NormalizeCurrencyPair> currencySet = currencyRates.get(bankLocalName);

                for (String currency : user.getCurrency()) {

                    // {"USD", "36.95000", "37.45000"}
                    NormalizeCurrencyPair curCurrency = currencySet.get(currency);

                    if (currency.equals(curCurrency.getName())) {
                        sb.append(curCurrency.getName()).append("\n");
                        sb.append("Купівля: ");
                        String format = "%." + user.getCountLastDigits() + "f";
                        sb.append(String.format(format, Float.valueOf(curCurrency.getBuy()))).append("\n");
                        sb.append("Продаж: ");
                        sb.append(String.format(format, Float.valueOf(curCurrency.getSale()))).append("\n\n");
                    }
                }
            }
            return !sb.toString().isEmpty() ? sb.toString() : null;
        }

    }