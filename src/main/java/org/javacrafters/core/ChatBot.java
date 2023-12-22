package org.javacrafters.core;

import org.javacrafters.banking.CurrencyHolder;
import org.javacrafters.banking.NormalizeCurrencyPair;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

            // Messages processing
            if (update.hasMessage()) {

                logger.info(update.getMessage().getText(), update);
                String msgCommand = update.getMessage().getText();

                // Start
                if (msgCommand.equals("/start")) {
                    doCommandStart(chatId, update);
                }
                // Stop / Disable notify
                if (msgCommand.equals("/stop") || msgCommand.endsWith("Стоп")) {
                    doCommandStop(chatId, update);
                }
                if (msgCommand.equals("Вимкнути сповіщення")) {
                    doCommandNotifyOff(chatId, update);
                }
                // Set Notify Time
                if (msgCommand.endsWith(":00")) {
                    doCommandNotifySetTime(chatId, update);
                }
                // Settings
                if (msgCommand.endsWith("Налаштування")) {
                    doCommandSettings(chatId, update);
                }
                //
                if (msgCommand.endsWith("Курси валют")) {
                    userNotify(AppRegistry.getUser(chatId));
                }
            }

            // Callbacks processing
            if (update.hasCallbackQuery()) {
                String[] btnCommand = update.getCallbackQuery().getData().split("_");

                switch (btnCommand[0].toUpperCase()) {
                    case "BANK" -> doCallBackBank(chatId, update, btnCommand);
                    case "CURRENCY" -> doCallBackCurrency(chatId, update, btnCommand);
                    case "NOTIFICATION" -> doCallBackNotification(chatId, update, btnCommand);
                    case "DECIMAL" -> doCallBackDecimal(chatId, update, btnCommand);
                    case "SETTINGS" -> doCallBackSettings(chatId, update, btnCommand);
                }

            }
        }

        /*
         *
         * Message Commands
         * */
        public void doCommandStart(Long chatId, Update update) {
            BotDialogHandler dh = new BotDialogHandler(chatId);
            SendMessage ms = dh.createWelcomeMessage(chatId);
            sendMessage(ms);

            if (!AppRegistry.hasUser(chatId)) {
                addUser(chatId, update);
                saveUser(chatId);
            }
            userNotify(AppRegistry.getUser(chatId));
        }
        public void doCommandStop(Long chatId, Update update) {
            BotDialogHandler dh = new BotDialogHandler(chatId);
            SendMessage ms = dh.createMessage("""
                                ❗Вашу підписку на отримання курсів валют деактивовано!❗ 
                                Якщо ви бажаєте активувати її наново, будь ласка введіть або натисніть на команду /start 
                                Також в налаштуваннях ви маєте обрати зручний для вас час розсилки курсів валют.
                                """, chatId);
            sendMessage(ms);

            Scheduler.getUserScheduler(chatId).cancel(true);
            AppRegistry.getUser(chatId).setNotifyOff();
            saveUser(chatId);
        }
        public void doCommandSettings(Long chatId, Update update) {
            BotDialogHandler dh = new BotDialogHandler(chatId);
            SendMessage ms = dh.createSettingsMessage(chatId);
            sendMessage(ms);
        }
        public void doCommandNotifyOff(Long chatId, Update update) {
            BotDialogHandler dh = new BotDialogHandler(chatId);
            SendMessage ms = dh.createCustomMessage(chatId,"⚠\uFE0F  Сповіщення вимкнено!");
            sendMessage(ms);

            Scheduler.getUserScheduler(chatId).cancel(true);
            AppRegistry.getUser(chatId).setNotifyOff();

            saveUser(chatId);
        }
        public void doCommandNotifySetTime(Long chatId, Update update) {
            BotDialogHandler dh = new BotDialogHandler(chatId);
            String msgCommand = update.getMessage().getText();
            SendMessage ms = dh.createCustomMessage(chatId,"\uD83D\uDEC8  Час сповіщень змінено на " + msgCommand);
            sendMessage(ms);

            int hour = Integer.parseInt(msgCommand.split(":")[0]);
            AppRegistry.getUser(chatId).setNotifyTime(hour);
            AppRegistry.getUser(chatId).setNotifyOn();

            Scheduler.getUserScheduler(chatId).cancel(true);
            Scheduler.addUserSchedule(chatId, AppRegistry.getUser(chatId), AppRegistry.getUser(chatId).getNotifyTime());

            saveUser(chatId);
        }
        public void sendNotFound(Long chatId) {
            BotDialogHandler dh = new BotDialogHandler(chatId);
            SendMessage ms = dh.createMessage("Command Not Found!", chatId);
            sendMessage(ms);
        }

        /*
        *
        * Call Back Commands
        * */
        private void doCallBackBank(Long chatId, Update update, String[] command) {
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
        public void doCallBackCurrency(Long chatId, Update update, String[] command) {
            if (command.length > 1) {
                List<String> userCurrency = AppRegistry.getUser(chatId).getCurrency();
                String toggleCurrency = command[1];
                if (userCurrency.contains(toggleCurrency.toUpperCase())) {
                    userCurrency.remove(toggleCurrency);
                } else {userCurrency.add(toggleCurrency);}
                saveUser(chatId);
            }
            BotDialogHandler dh = new BotDialogHandler(chatId);
            EditMessageText ms = dh.onCurrencyMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
            sendMessage(ms);
        }
        public void doCallBackNotification(Long chatId, Update update, String[] command) {
            AnswerCallbackQuery close = AnswerCallbackQuery.builder()
                    .callbackQueryId(update.getCallbackQuery().getId()).build();
            try {
                execute(close);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }

            BotDialogHandler dh = new BotDialogHandler(chatId);
            SendMessage ms = dh.createSetNotifyMessage(chatId);
            sendMessage(ms);

        }
        public void doCallBackDecimal(Long chatId, Update update, String[] command) {
            if (command.length > 1) {
                String num = command[1];
                AppRegistry.getUser(chatId).setCountLastDigits(Integer.parseInt(num));
                saveUser(chatId);
            }
            BotDialogHandler dh = new BotDialogHandler(chatId);
            EditMessageText ms = dh.onDecimalMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
            sendMessage(ms);
        }
        public void doCallBackSettings(Long chatId, Update update, String[] command) {
            BotDialogHandler dh = new BotDialogHandler(chatId);
            EditMessageText ms = dh.onSettingMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
            sendMessage(ms);
        }
        public void userNotify(User user) {
            System.out.println("userNotify() = " + user.getId() + " " + user.getName());

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(user.getId()));

            BotDialogHandler dh = new BotDialogHandler(user.getId());
            SendMessage ms = dh.createMessage(Objects.requireNonNull(createNotifyMessage(user)), user.getId());
            sendMessage(ms);
        }

        private String createNotifyMessage(User user) {
            // {"PB" => {"USD" => {"USD", "36.95000", "37.45000"}}}
            Map<String, Map<String, NormalizeCurrencyPair>> currencyRates = CurrencyHolder.getRates();

            if (currencyRates == null || currencyRates.isEmpty()) {
                return null;
            }
            StringBuilder sb = new StringBuilder("\uD83C\uDFA2  Поточні курси валют:\n");

            for (String bankLocalName : user.getBanks()) {

                String bankName = AppRegistry.getBank(bankLocalName).getName();
                StringBuilder sbSub = new StringBuilder();

                // {"USD" => {"USD", "36.95000", "37.45000"}}
                Map<String, NormalizeCurrencyPair> currencySet = currencyRates.get(bankLocalName);

                for (String currency : user.getCurrency()) {

                    // {"USD", "36.95000", "37.45000"}
                    NormalizeCurrencyPair curCurrency = currencySet.get(currency);

                    if (curCurrency != null && user.getCurrency().contains(curCurrency.getName())) {
                        sbSub.append(curCurrency.getName()).append("\n");
                        sbSub.append("\tКупівля: ");
                        String format = "%." + user.getCountLastDigits() + "f";
                        sbSub.append(String.format(format, Float.valueOf(curCurrency.getBuy()))).append("\n");
                        sbSub.append("\tПродаж: ");
                        sbSub.append(String.format(format, Float.valueOf(curCurrency.getSale()))).append("\n");
                    }
                }
                if (!sbSub.toString().isEmpty()) {
                    sb.append("\n\uD83C\uDFE6  " + bankName).append("\n").append(sbSub);
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