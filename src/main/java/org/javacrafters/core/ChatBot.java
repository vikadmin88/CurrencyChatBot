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
                String message = update.getMessage().getText();
                logger.trace(message, update);
                if (message.equals("/start")) {
//                    sendMessage(chatId);
                    startMesID = update.getMessage().getMessageId();
                    sendApiMethodAsync(bd.createWelcomeMessage(chatId));
                    if (!AppRegistry.hasUser(chatId)) {
                        addUser(chatId, update);
                    }
                    // while testing
//                    userNotify(AppRegistry.getUser(chatId));
                }//else if (message.equals(new String("Повернутись до налаштування".getBytes(), StandardCharsets.UTF_8))) {
//                    sendApiMethodAsync(bd.onSettingMessage(chatId, startMesID));
//                }
            }

            // Callbacks processing
            if (update.hasCallbackQuery()) {
                String data = update.getCallbackQuery().getData();
                int messageId = update.getCallbackQuery().getMessage().getMessageId();
                switch (data) {
                    //Main callbacks
                    case "settings", "to_settings" -> {
                        try {
                            execute(bd.onSettingMessage(chatId, messageId));
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    case "get_info" -> sendApiMethodAsync(bd.createMessage(createNotifyMessage(AppRegistry.getUser(chatId)), chatId));
                }
            }
        }

        public void userNotify(User user) {
            System.out.println("userNotify() = " + user.getId() + " " + user.getName());

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(user.getId()));

            String message = createNotifyMessage(user);
            if (message != null) {
                sendMessage.setText(message);
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