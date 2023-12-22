package org.javacrafters.core.ui;

import org.javacrafters.banking.Bank;

import org.javacrafters.core.AppRegistry;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class BotDialogHandler {

    private final Long chatId;


    public BotDialogHandler(Long chatId) {
        this.chatId = chatId;
    }
    // Стартовое сообщение
    public SendMessage createWelcomeMessage(Long chatId) {
        String text = "<b>Ласкаво просимо.</b> \nЦей бот допоможе відслідковувати актуальні курси валют!";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getPermanentKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }
    public SendMessage createCustomMessage(Long chatId, String textMessage) {
        String text = "" + textMessage;
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getPermanentKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }
    //Сообщение с настройками
    public  SendMessage createSettingsMessage(Long chatId){
        String text = "⚙  <b>Налаштування</b>";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(createSettingsButtons());
//        message.setReplyMarkup(getPermanentKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    //Сообщение время уведомления
    public SendMessage createSetNotifyMessage(Long chatId){
        String text = "⏰  <b>Виберіть час сповіщення</b>";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getTimeKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    public EditMessageText onSettingMessage(Long chatId, Integer messageId) {
        String text = "⚒  <b>Налаштування</b>";
        return createEditMessage(chatId, messageId, text, BT.SETTINGS);
    }
    public EditMessageText onDecimalMessage(Long chatId, Integer messageId) {
        String text = "\uD83D\uDD22  <b>Кількість знаків після коми</b>";
        return createEditMessage(chatId, messageId, text, BT.DEC_BUT);
    }
    public EditMessageText onBankMessage(Long chatId, Integer messageId) {
        String text = "\uD83C\uDFE6  <b>Банки</b>";
        return createEditMessage(chatId, messageId, text, BT.BAN_BUT);
    }
    public EditMessageText onCurrencyMessage(Long chatId, Integer messageId) {
        String text = "\uD83D\uDCB5  <b>Валюти</b>";
        return createEditMessage(chatId, messageId, text, BT.CUR_BUT);
    }

    //метод для добавления кнопок к разделам настроек
    private EditMessageText createEditMessage(Long chatId, Integer messageId, String messageText, BT buttonType) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(String.valueOf(chatId));
        newMessage.setMessageId(messageId);
        newMessage.setText(new String(messageText.getBytes(), StandardCharsets.UTF_8));
        newMessage.setParseMode(ParseMode.HTML);

        InlineKeyboardMarkup replyMarkup = switch (buttonType) {
            case BAN_BUT -> createBankButtons(chatId);
            case CUR_BUT -> createCurrencyButtons(chatId);
            case DEC_BUT -> createDecimalButtons(chatId);
            case SETTINGS -> createSettingsButtons();
            default -> createMainMenuButtons();
        };

        newMessage.setReplyMarkup(replyMarkup);
        return newMessage;
    }
    // Метод для создания клавиатуры пользователя с выбором времени уведомлений
    private ReplyKeyboardMarkup getTimeKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        // Добавление кнопок с временем
        for (int hour = 9; hour <= 18; hour++) {
            row.add(hour + ":00");
            if ((hour - 8) % 3 == 0) { // Разбиваем на ряды по 3 кнопки
                keyboard.add(row);
                row = new KeyboardRow();
            }
        }
        row.add(new String("Вимкнути сповіщення".getBytes(), StandardCharsets.UTF_8));
//        row.add(new String("Налаштування".getBytes(), StandardCharsets.UTF_8));
        // Добавляем последний ряд, если он не пустой
        if (!row.isEmpty()) {
            keyboard.add(row);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }
    //создание кнопок - сколько знаков после запятой
    private InlineKeyboardMarkup createDecimalButtons(Long chatId) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        int userNumOfDigits = AppRegistry.getUser(chatId).getCountLastDigits();

        for (int i = 2; i <= 4; i++) {
            String buttonText = (userNumOfDigits == i ? "✅  " : "") + i;
            buttons.add(createButton(buttonText, "decimal_" + i));
        }
        return buildInlineKeyboard(buttons);
    }
    //создание кнопок - валюты
    private InlineKeyboardMarkup createCurrencyButtons(Long chatId) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        List<String> availableCurrencies = AppRegistry.getCurrency();
        List<String> userCurrency = AppRegistry.getUser(chatId).getCurrency();

        for (String currency : availableCurrencies) {
            String buttonText = (userCurrency.contains(currency) ? "✅  " : "") + currency;
            buttons.add(createButton(buttonText, "currency_" + currency));
        }
        return buildInlineKeyboard(buttons);
    }
    //создание кнопок - банки
    private InlineKeyboardMarkup createBankButtons(Long chatId) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        Map<String, Bank> allBanks = AppRegistry.getBanks();
        List<String> userBanks = AppRegistry.getUser(chatId).getBanks();

        for (Map.Entry<String, Bank> entry : allBanks.entrySet()) {
            String bankName = entry.getValue().getName();
            String bankLocalName = entry.getKey();
            String buttonText = userBanks.contains(bankLocalName) ? "✅  " + bankName : bankName;
            buttons.add(createButton(buttonText, "bank_" + bankLocalName));
        }
        return buildInlineKeyboard(buttons);
    }
    //создание кнопок - сколько знаков после запятой
    private InlineKeyboardMarkup createMainMenuButtons() {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        // Добавление кнопок для главного меню
        buttons.add(createButton("\uD83C\uDFA2 Курси валют", "get_info"));
        buttons.add(createButton("⚙ Налаштування", "settings"));

        return buildInlineKeyboard(buttons);
    }

    private InlineKeyboardMarkup createSettingsButtons() {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        // Добавление кнопок для настроек
        buttons.add(createButton("\uD83C\uDFE6 Банки", "bank"));
        buttons.add(createButton("\uD83D\uDCB5 Валюти", "currency"));
        buttons.add(createButton("\uD83D\uDD22 Кількість знаків після коми", "decimal"));
        buttons.add(createButton("⏰ Час сповіщення", "notification"));

        return buildInlineKeyboard(buttons);
    }
    //Метод для создания кнопки
    private InlineKeyboardButton createButton(String buttonText, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(new String(buttonText.getBytes(), StandardCharsets.UTF_8));
        button.setCallbackData(callbackData);
        return button;
    }
    //Метод для создания клавиатур для сообщения из кнопок
    private InlineKeyboardMarkup buildInlineKeyboard(List<InlineKeyboardButton> buttons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (InlineKeyboardButton button : buttons) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
        }
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }
    //Создание сообщения
    public SendMessage createMessage(String text, Long chatId) {
        SendMessage message = new SendMessage();
        message.setText(new String(text.getBytes(), StandardCharsets.UTF_8));
        message.setParseMode("markdown");
        message.setChatId(chatId);
        return message;
    }

    //клавиатура пользователя
    public ReplyKeyboardMarkup getPermanentKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Создаем один ряд кнопок
        KeyboardRow row = new KeyboardRow();
        row.add(new String("\uD83C\uDFA2 Курси валют".getBytes(), StandardCharsets.UTF_8));
        row.add(new String("❌ Стоп".getBytes(), StandardCharsets.UTF_8));
        row.add(new String("⚙ Налаштування".getBytes(), StandardCharsets.UTF_8));

        keyboard.add(row);

        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true); // Делаем клавиатуру подгоняемой по размеру
        replyKeyboardMarkup.setOneTimeKeyboard(false); // Клавиатура будет постоянной
        return replyKeyboardMarkup;
    }
}