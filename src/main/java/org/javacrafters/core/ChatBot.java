package org.javacrafters.core;

import org.javacrafters.banking.CurrencyHolder;
import org.javacrafters.banking.NormalizeCurrencyPair;
import org.javacrafters.core.ui.BotDialogHandler;
import org.javacrafters.scheduler.Scheduler;
import org.javacrafters.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
* Bot Controller (MVC)
* @author ViktorK viktork8888@gmail.com
*/
public class ChatBot extends TelegramLongPollingBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatBot.class);

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
        if (chatId == null) {
            return null;
        }
        String firstName = "";
        String userName = "";
        if (update.hasMessage()) {
            firstName = update.getMessage().getFrom().getFirstName();
            userName = update.getMessage().getFrom().getLastName();
        }
        if (update.hasCallbackQuery()) {
            firstName = update.getCallbackQuery().getFrom().getFirstName();
            userName = update.getCallbackQuery().getFrom().getLastName();
        }
        User user = new User(chatId, firstName, userName);
        user.addBank(AppRegistry.getConfBank());
        user.addCurrency(AppRegistry.getConfCurrency());
        user.setDecimalPlaces(AppRegistry.getConfDecimalPlaces());
        user.setNotifyTime(AppRegistry.getConfNotifyTime());
        user.setNotifyStatus(AppRegistry.getConfNotifyStatus());
        LOGGER.info("addUser: {} {}", chatId, firstName);
        AppRegistry.addUserCompletely(user);
        return user;
    }

    private void saveUser(Long userId) {
        UserLoader.save(AppRegistry.getUser(userId));
    }
    private void removeUser(Long userId) {
        LOGGER.info("removeUser: {}", userId);
        AppRegistry.removeUserCompletely(userId);
    }
    private void checkOrAddUser(Long chatId, Update update) {
        if (!AppRegistry.hasUser(chatId)) {
            addUser(chatId, update);
            saveUser(chatId);
        }
    }

    private BotDialogHandler getDH(Long chatId) {
        return new BotDialogHandler(chatId);
    }
    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = getChatId(update);
        boolean isCommandPerformed = false;
        // check or add user
        checkOrAddUser(chatId, update);

        // Messages processing
        if (update.hasMessage()) {

        String msgCommand = update.getMessage().getText();
        LOGGER.info("onUpdate: msgCommand: {}  User: {} {}", msgCommand, chatId, AppRegistry.getUser(chatId).getName());

            // Start
            if (msgCommand.equals("/start")) {
                isCommandPerformed = true;
                doCommandStart(chatId, update);
            }
            // Stop / Disable notify
            if (msgCommand.equals("/stop") || msgCommand.endsWith(new String("Вийти".getBytes(), StandardCharsets.UTF_8))) {
                isCommandPerformed = true;
                doCommandStop(chatId, update);
            }
            if (msgCommand.equals(new String("Вимкнути сповіщення".getBytes(), StandardCharsets.UTF_8))) {
                isCommandPerformed = true;
                doCommandNotifyOff(chatId, update);
            }
            // Set Notify Time
            if (msgCommand.endsWith(new String(":00".getBytes(), StandardCharsets.UTF_8))) {
                isCommandPerformed = true;
                doCommandNotifySetTime(chatId, update);
            }
            // Settings
            if (msgCommand.equals("/settings") || msgCommand.endsWith(new String("Налаштування".getBytes(), StandardCharsets.UTF_8))) {
                isCommandPerformed = true;
                doCommandSettings(chatId, update);
            }
            // Settings
            if (msgCommand.equals("/mysettings")) {
                isCommandPerformed = true;
                doCommandUserSettingsMessage(chatId);
            }
            //
            if (msgCommand.equals("/cnow") || msgCommand.endsWith(new String("Курси валют".getBytes(), StandardCharsets.UTF_8))) {
                isCommandPerformed = true;
                userNotify(AppRegistry.getUser(chatId));
            }
            // if no command
            if (!isCommandPerformed) {
                sendErrorMessage(chatId);
            }
        }

        // Callbacks processing
        if (update.hasCallbackQuery()) {
            String[] btnCommand = update.getCallbackQuery().getData().split("_");
            LOGGER.info("btnCommand: {} btnCommand[] {}  User: {} {}", update.getCallbackQuery().getData(),
                    Arrays.toString(btnCommand), chatId, AppRegistry.getUser(chatId).getName());

            switch (btnCommand[0].toUpperCase()) {
                case "WELCOME" -> doCommandStart(chatId, update);
                case "BANK" -> doCallBackBank(chatId, update, btnCommand);
                case "CURRENCY" -> doCallBackCurrency(chatId, update, btnCommand);
                case "DECIMAL" -> doCallBackDecimal(chatId, update, btnCommand);
                case "NOTIFICATION" -> doCallBackNotification(chatId, update, btnCommand);
                case "USERSETTINGS" -> doCallBackUserSettingsMessage(chatId, update, btnCommand);
                case "SETTINGS" -> doCallBackSettings(chatId, update);
                case "ABOUT"-> doCallBackAboutUs(chatId, update);
            }

        }
    }

    /*
     *
     * Message Commands
     * */
    public void doCommandStart(Long chatId, Update update) {
        SendPhoto ms = getDH(chatId).createWelcomeMessage();
        try {
            execute(ms);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    public void doCommandStop(Long chatId, Update update) {
        SendMessage ms = getDH(chatId).createMessage("""
                            ❗Вашу підписку на отримання курсів валют деактивовано!❗ 
                            Якщо ви бажаєте активувати її наново, будь ласка введіть або натисніть на команду /start
                            Також в налаштуваннях ви маєте обрати зручний для вас час отримання повідомлень з курсами валют.
                            """);
        sendMessage(ms);
        removeUser(chatId);
    }
    public void doCommandSettings(Long chatId, Update update) {
        SendMessage ms = getDH(chatId).createSettingsMessage();
        sendMessage(ms);
    }
    public void doCommandUserSettingsMessage(Long chatId) {
        SendMessage ms = getDH(chatId).createUserSettingsMessage(Objects.requireNonNull(createUserCurrentSettings(chatId)));
        sendMessage(ms);
    }
    public void doCommandNotifyOff(Long chatId, Update update) {
        SendMessage ms = getDH(chatId).createCustomMessage("⚠  Сповіщення вимкнено!");
        sendMessage(ms);

        Scheduler.removeUserScheduler(chatId);
        AppRegistry.getUser(chatId).setNotifyOff();

        saveUser(chatId);
    }
    public void doCommandNotifySetTime(Long chatId, Update update) {
        String msgCommand = update.getMessage().getText();
        SendMessage ms = getDH(chatId).createCustomMessage("⏰  Час сповіщень змінено на " + msgCommand);
        sendMessage(ms);

        int hour = Integer.parseInt(msgCommand.split(":")[0]);
        AppRegistry.getUser(chatId).setNotifyTime(hour);
        AppRegistry.getUser(chatId).setNotifyOn();

        if (Scheduler.getUserScheduler(chatId) != null) {
            Scheduler.getUserScheduler(chatId).cancel(true);
        }
        Scheduler.addUserSchedule(chatId, AppRegistry.getUser(chatId), AppRegistry.getUser(chatId).getNotifyTime());

        saveUser(chatId);
    }
    public void sendErrorMessage(Long chatId) {
        SendMessage ms = getDH(chatId).createMessage("❗ Command not found!");
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
        EditMessageText ms = getDH(chatId).onBankMessage(update.getCallbackQuery().getMessage().getMessageId());
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
        EditMessageText ms = getDH(chatId).onCurrencyMessage(update.getCallbackQuery().getMessage().getMessageId());
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

        SendMessage ms = getDH(chatId).createSetNotifyMessage();
        sendMessage(ms);

    }
    public void doCallBackDecimal(Long chatId, Update update, String[] command) {
        if (command.length > 1) {
            String num = command[1];
            AppRegistry.getUser(chatId).setDecimalPlaces(Integer.parseInt(num));
            saveUser(chatId);
        }
        EditMessageText ms = getDH(chatId).onDecimalMessage(update.getCallbackQuery().getMessage().getMessageId());
        sendMessage(ms);
    }
    private void doCallBackAboutUs(Long chatId, Update update) {
        // Переходим на раздел
        EditMessageText aboutUsMessage = getDH(chatId).onAboutUsMessage(update.getCallbackQuery().getMessage().getMessageId());
        sendMessage(aboutUsMessage);
        // Отправляем текст+фото
        SendPhoto photoMessage = getDH(chatId).createAboutUsMessage();
        sendPhoto(photoMessage);
    }
    public void doCallBackSettings(Long chatId, Update update) {
        AnswerCallbackQuery close = AnswerCallbackQuery.builder()
                .callbackQueryId(update.getCallbackQuery().getId()).build();
        try {
            execute(close);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        SendMessage ms = getDH(chatId).createSettingsMessage();
        sendMessage(ms);
    }
    public void doCallBackUserSettingsMessage(Long chatId, Update update, String[] command) {
        AnswerCallbackQuery close = AnswerCallbackQuery.builder()
                .callbackQueryId(update.getCallbackQuery().getId()).build();
        try {
            execute(close);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        doCommandUserSettingsMessage(chatId);
//        SendMessage ms = getDialogHandler(chatId).createUserSettingsMessage(Objects.requireNonNull(createUserCurrentSettings(chatId)), chatId);
//        sendMessage(ms);
    }
    private String createUserCurrentSettings (Long chatId) {
        String userName = AppRegistry.getUser(chatId).getName();
        StringBuilder sb = new StringBuilder("❓ <b>").append(userName).append(", ваші налаштування:</b>\n");
        sb.append("<b>Банки:</b>\n");
        for (String bankLocalName : AppRegistry.getUser(chatId).getBanks()) {
            sb.append("  ").append(AppRegistry.getBank(bankLocalName).getName()).append("\n");
        }
        sb.append("<b>Валюти:</b>\n");
        for (String currencyName : AppRegistry.getUser(chatId).getCurrency()) {
            sb.append("  ").append(currencyName).append("\n");
        }
        sb.append("<b>Знаків після коми:</b> ").append(AppRegistry.getUser(chatId).getDecimalPlaces()).append("\n");
        String bulToStr = AppRegistry.getUser(chatId).isNotifyOn() ? "Активовані" : "Деактивовані";
        sb.append("<b>Сповіщення:</b> ").append(bulToStr).append("\n");
        sb.append("<b>Час сповіщення:</b> ").append(AppRegistry.getUser(chatId).getNotifyTime()).append(":00").append("\n");
        return sb.toString();
    }

    public void userNotify(User user) {
        LOGGER.info("userNotify() = {} {}", user.getId(), user.getName());
        SendMessage ms = getDH(user.getId()).createMessage(Objects.requireNonNull(createNotifyMessage(user)));
        sendMessage(ms);
    }

    private String createNotifyMessage(User user) {

        String userName = user.getName();

        // {"PB" => {"USD" => {"USD", "36.95000", "37.45000"}}}
        Map<String, Map<String, NormalizeCurrencyPair>> currencyRates = CurrencyHolder.getRates();

        if (currencyRates.isEmpty()) {
            return userName + ", нажаль системі не вдалося отримати курси валют від банків.";
        }
        StringBuilder sb = new StringBuilder("⚡  <b>Поточні курси валют:</b>\n");
        boolean noCurrency = true;

        for (String bankLocalName : user.getBanks()) {

            String bankName = AppRegistry.getBank(bankLocalName).getName();
            StringBuilder sbSub = new StringBuilder();

            // {"USD" => {"USD", "36.95000", "37.45000"}}
            Map<String, NormalizeCurrencyPair> currencySet = currencyRates.get(bankLocalName);

            for (String currency : user.getCurrency()) {

                // {"USD", "36.95000", "37.45000"}
                NormalizeCurrencyPair curCurrency = currencySet.get(currency);

                if (curCurrency != null && user.getCurrency().contains(curCurrency.getName())) {
                    sbSub.append("<b>").append(curCurrency.getName()).append("</b>").append("\n");

                    String format = "%." + user.getDecimalPlaces() + "f";
                    if (curCurrency.getBuy() != null) {
                        sbSub.append("\tКупівля:   ");
                        sbSub.append(String.format(format, Float.valueOf(curCurrency.getBuy()))).append("\n");
                    }
                    if (curCurrency.getSale() != null) {
                        sbSub.append("\tПродаж:   ");
                        sbSub.append(String.format(format, Float.valueOf(curCurrency.getSale()))).append("\n");
                    }
                    if (curCurrency.getCross() != null) {
                        sbSub.append("\tКрос-курс:   ");
                        sbSub.append(String.format(format, Float.valueOf(curCurrency.getCross()))).append("\n");
                    }
                }
            }
            if (!sbSub.toString().isEmpty()) {
                noCurrency = false;
//                sb.append("\n").append("✔  ").append("<b>").append(bankName).append("</b>").append("\n").append(sbSub);
                sb.append("\n").append("\u27A1  ").append("<b>").append(bankName).append("</b>").append("\n").append(sbSub);
            }
        }
        if (noCurrency) {
            sb.append("\n").append(userName).append(", обрані вами банки не надають обмінні курси по обраним вами валютам.");
        }
        return sb.toString();
    }
    public void sendPhoto(SendPhoto photo) {
        if (photo != null) {
            try {
                execute(photo);
            } catch (TelegramApiException e) {
                LOGGER.error("Can't send photo", e);
            }
        }
    }

    public void sendMessage(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                if (e.getMessage().contains("[403] Forbidden")) {
                    LOGGER.error("Can't sent sendMessage() Error message: {}", e.getMessage());
                    LOGGER.info("User {} left chat, removing...", message.getChatId());
                    removeUser(Long.parseLong(message.getChatId()));
                } else {
                    LOGGER.error("Can't sendMessage() sendMessage", e);
                }
            }
        }
    }

    public void sendMessage(EditMessageText message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                LOGGER.error("Can't sendMessage() EditMessageText", e);
            }
        }
    }
}