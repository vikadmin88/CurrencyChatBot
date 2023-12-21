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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

    /**
    * MVC: Controller
    * @author ViktorK viktork8888@gmail.com
    */
    public class ChatBot extends TelegramLongPollingBot {
        private static final Logger logger = LoggerFactory.getLogger(ChatBot.class);

        private final String appName;
        private final String botName;
        private final String botToken;

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

        private User addUser(Long chatId, Update update) {
            User user = new User(chatId, update.getMessage().getFrom().getFirstName(), update.getMessage().getFrom().getUserName());
            user.addBank(AppRegistry.getConfBank());
            user.addBank("NBU");
            user.addCurrency(AppRegistry.getConfCurrency());
            user.setCountLastDigits(AppRegistry.getConfCountLastDigits());
            user.setNotifyTime(AppRegistry.getConfNotifyTime());
            user.setNotifyStatus(AppRegistry.getConfNotifyStatus());
            AppRegistry.addUser(user);
            Scheduler.addUserSchedule(chatId, user, AppRegistry.getConfNotifyTime());
            return user;
        }

        private void saveUser(Long userId) {
            UserLoader.save(AppRegistry.getUser(userId));
        }
        @Override
        public void onUpdateReceived(Update update) {
            Long chatId = getChatId(update);
            BotDialogHandler dh = new BotDialogHandler(chatId);

            // Messages processing
            if (update.hasMessage()) {

                logger.info(update.getMessage().getText(), update);
                String msgCommand = update.getMessage().getText();

                // Start
                if (msgCommand.equals("/start")) {
                    if (!doCommandStart(chatId, update)) {
                        sendNotFound(chatId);
                    }
                }

                // Stop / Disable notify
                if (msgCommand.equals("/stop")) {
                    if (!doCommandStop(chatId, update)) {
                        sendNotFound(chatId);
                    }
                }
                if (msgCommand.equals("Вимкнути сповіщення")) {
                    if (!doCommandNotifyOff(chatId, update)) {
                        sendNotFound(chatId);
                    }
                }
                // Set Notify Time
                if (msgCommand.endsWith(":00")) {
                    if (!doCommandNotifySetTime(chatId, update)) {
                        sendNotFound(chatId);
                    }
                }
            }


//            EditMessageText ms2 = dh.onBankMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
//            sendMessage(ms2);
//            EditMessageText ms3 = dh.onCurrencyMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
//            sendMessage(ms3);

            // Callbacks processing
            if (update.hasCallbackQuery()) {
                String[] btnCommand = update.getCallbackQuery().getData().split("_");

                switch (btnCommand[0].toUpperCase()) {
                    case "BANK" -> doCommandBank(chatId, update, btnCommand);
                    case "CURRENCY" -> doCommandCurrency(chatId, update, btnCommand);
                }

//                String btnCommandValue = update.getCallbackQuery().getData().split("_")[1];
//                String[] cmdArr = update.getCallbackQuery().getData().split("_");
                System.out.println("Arrays.toString(cmdArr) = " + Arrays.toString(btnCommand));
//
//                System.out.println("btnCommand = " + btnCommand);
//                EditMessageText ms = dh.onBankMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
//                System.out.println("ms = " + ms);
//                sendMessage(ms);
//                EditMessageText ms = dh.onCurrencyMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
//                System.out.println("ms = " + ms);
//                sendMessage(ms);

                if (update.getCallbackQuery().getData().equals("level_1_task")) {
//                    sendMessage(chatId);
                }
            }
        }

        public boolean doCommandStart(Long chatId, Update update) {
            BotDialogHandler dh = new BotDialogHandler(chatId);
            SendMessage ms = dh.createSettingsMessage(chatId);
            sendMessage(ms);

            if (!AppRegistry.hasUser(chatId)) {
                addUser(chatId, update);
                saveUser(chatId);
            }

            return true;
        }

        public boolean doCommandStop(Long chatId, Update update) {
            if (AppRegistry.getUser(chatId) == null) {return false;}
            BotDialogHandler dh = new BotDialogHandler(chatId);
            SendMessage ms = dh.createMessage("""
                                Ви відписалися від розсилки. Щоб підписатися наново введіть команду /start 
                                і в налаштуваннях оберіть час розсилки.
                                """, chatId);
            sendMessage(ms);

            Scheduler.getUserScheduler(chatId).cancel(true);
            AppRegistry.getUser(chatId).setNotifyOff();
            saveUser(chatId);
            return true;
        }
        public boolean doCommandNotifyOff(Long chatId, Update update) {
            if (AppRegistry.getUser(chatId) == null) {return false;}
            BotDialogHandler dh = new BotDialogHandler(chatId);
            SendMessage ms = dh.createMessage("Сповіщення вимкнуті!", chatId);
            sendMessage(ms);

            Scheduler.getUserScheduler(chatId).cancel(true);
            AppRegistry.getUser(chatId).setNotifyOff();

            saveUser(chatId);
            return true;
        }
        public boolean doCommandNotifySetTime(Long chatId, Update update) {
            if (AppRegistry.getUser(chatId) == null) {return false;}
            BotDialogHandler dh = new BotDialogHandler(chatId);
            String msgCommand = update.getMessage().getText();
            SendMessage ms = dh.createMessage("Час сповіщень змінений на " + msgCommand, chatId);
            sendMessage(ms);

            int hour = Integer.parseInt(msgCommand.split(":")[0]);
            AppRegistry.getUser(chatId).setNotifyTime(hour);
            AppRegistry.getUser(chatId).setNotifyOn();

            Scheduler.getUserScheduler(chatId).cancel(true);
            Scheduler.addUserSchedule(chatId, AppRegistry.getUser(chatId), AppRegistry.getUser(chatId).getNotifyTime());

            saveUser(chatId);
            return true;
        }
        public void sendNotFound(Long chatId) {
            BotDialogHandler dh = new BotDialogHandler(chatId);
            SendMessage ms = dh.createMessage("Command Not Found!", chatId);
            sendMessage(ms);
        }

        private void doCommandBank(Long chatId, Update update, String[] command) {
            if (command.length > 1) {
                List<String> userBanks = AppRegistry.getUser(chatId).getBanks();
                String toggleBank = command[1];
                if (userBanks.contains(toggleBank.toUpperCase())) {
                    userBanks.remove(toggleBank);
                } else {userBanks.add(toggleBank);}
                saveUser(chatId);
            }
            BotDialogHandler dh = new BotDialogHandler(chatId);
            EditMessageText ms = dh.onBankMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
            sendMessage(ms);
        }
        public void doCommandCurrency(Long chatId, Update update, String[] command) {
            if (command.length > 1) {
                List<String> userCurrency = AppRegistry.getUser(chatId).getCurrency();
                String toggleCurrency = command[1];
                if (userCurrency.contains(toggleCurrency.toUpperCase())) {
                    userCurrency.remove(toggleCurrency);
                } else {userCurrency.add(toggleCurrency);}
                saveUser(chatId);
                System.out.println("AppRegistry.getUser(chatId) = " + AppRegistry.getUser(chatId));
            }
            BotDialogHandler dh = new BotDialogHandler(chatId);
            EditMessageText ms = dh.onCurrencyMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
            sendMessage(ms);
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

        public void sendMessage(SendMessage message) {
            if (message != null) {
                try {
                    execute(message);
                    logger.info("sendMessage()", message);
                } catch (TelegramApiException e) {
                    logger.error("sendMessage()", message);
                }
            }
        }
        public void sendMessage(EditMessageText message) {
            if (message != null) {
                try {
                    execute(message);
                    logger.info("sendMessage()", message);
                } catch (TelegramApiException e) {
                    logger.error("sendMessage()", message);
                }
            }
        }
    }