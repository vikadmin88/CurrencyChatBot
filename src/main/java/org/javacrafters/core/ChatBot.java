package org.javacrafters.core;

import org.javacrafters.banking.CurrencyHolder;
import org.javacrafters.banking.NormalizeCurrencyPair;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.util.*;



    public class ChatBot extends TelegramLongPollingBot {

        private final String appName;
        private final String botName;
        private final String botToken;

        public ChatBot(String appName, String botName, String botToken) {
            this.appName = appName;
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
                api.registerBot(new ChatBot(appName, botName, botToken));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
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

        public void addUser(Long chatId, Update update) {
            User user = new User(chatId, update.getMessage().getFrom().getFirstName(), update.getMessage().getFrom().getUserName());
            user.addBank(AppRegistry.getConfVal("USER_DEF_BANK"));
            user.addBank("NBU");
            user.addBank("MB");
            user.addCurrency(AppRegistry.getConfVal("USER_DEF_CURRENCY"));
            user.addCurrency("EUR");
            user.setNumOfDigits(Integer.parseInt(AppRegistry.getConfVal("USER_DEF_COUNT_DIGITS")));
            user.setNotifyTime(Integer.parseInt(AppRegistry.getConfVal("USER_DEF_NOTIFY_TIME")));
            user.setNotifyOn();
            // for prod
//            user.setScheduledTask(new Scheduler().schedule(this, user, Integer.parseInt(AppRegistry.getConfVal("USER_DEF_NOTIFY_TIME"))));
            // for test
            user.setScheduledTask(new Scheduler().userSchedule(this, user,15));
            AppRegistry.addUser(user);
        }

        @Override
        public void onUpdateReceived(Update update) {
            Long chatId = getChatId(update);

            // Messages processing
            if (update.hasMessage()) {
                if (update.getMessage().getText().equals("/start")) {
//                    sendMessage(chatId);

                    if (!AppRegistry.hasUser(chatId)) {
                        addUser(chatId, update);
                    }
                    // while testing
                    userNotify(AppRegistry.getUser(chatId));
                }
            }

            // Callbacks processing
            if (update.hasCallbackQuery()) {

                if (update.getCallbackQuery().getData().equals("level_1_task")) {
//                    sendMessage(chatId);
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
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {

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
                        sb.append("Покупка: ");
                        String format = "%." + user.getNumOfDigits() + "f";
                        sb.append(String.format(format, Float.valueOf(curCurrency.getBuy()))).append("\n");
                        sb.append("Продаж: ");
                        sb.append(String.format(format, Float.valueOf(curCurrency.getSale()))).append("\n\n");
                    }
                }
            }
            return !sb.toString().isEmpty() ? sb.toString() : null;
        }

    }