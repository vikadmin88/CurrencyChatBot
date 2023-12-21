package org.javacrafters.core;

import org.javacrafters.banking.Bank;

import org.javacrafters.user.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class BotDialogHandler {


    // Стартовое сообщение
    public SendMessage createWelcomeMessage(Long chatId) {
        String text = "Ласкаво просимо. Цей бот допоможе відслідковувати актуальні курси валют";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getPermanentKeyboard());
        return message;
    }
    public  SendMessage createSettingsMessage(Long chatId){
        String text = "Налаштування";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(createSettingsButtons());
        return message;
    }

    //Сообщение время уведомления
    public SendMessage createSetNotifyMessage(Long chatId){
        String text = "Виберіть час сповіщення";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getTimeKeyboard());
        return message;
    }

    public EditMessageText onSettingMessage(Long chatId, Integer messageId) {
        String text = "Налаштування";
        return createEditMessage(chatId, messageId, text, BT.SETTINGS);
    }
    public EditMessageText onDecimalMessage(Long chatId, Integer messageId) {
        String text = "Кількість знаків після коми";
        return createEditMessage(chatId, messageId, text, BT.DEC_BUT);
    }
    public EditMessageText onBankMessage(Long chatId, Integer messageId) {
        String text = "Банки";
        return createEditMessage(chatId, messageId, text, BT.BAN_BUT);
    }
    public EditMessageText onCurrencyMessage(Long chatId, Integer messageId) {
        String text = "Валюти";
        return createEditMessage(chatId, messageId, text, BT.CUR_BUT);
    }

    private EditMessageText createEditMessage(Long chatId, Integer messageId, String messageText, BT buttonType) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(String.valueOf(chatId));
        newMessage.setMessageId(messageId);
        newMessage.setText(new String(messageText.getBytes(), StandardCharsets.UTF_8));

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
        row.add(new String("До налаштувань".getBytes(), StandardCharsets.UTF_8));
        // Добавляем последний ряд, если он не пустой
        if (!row.isEmpty()) {
            keyboard.add(row);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private InlineKeyboardMarkup createDecimalButtons(Long chatId) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        int currentNumOfDigits = AppRegistry.getConfCountLastDigits();
        int userNumOfDigits = AppRegistry.getUser(chatId).getCountLastDigits();

        for (int i = 2; i <= 4; i++) {
            String buttonText = (userNumOfDigits == i ? " ✅" : "") + i;
            buttons.add(createButton(buttonText, "decimal_" + i));
        }
        return buildInlineKeyboard(buttons);
    }

    private InlineKeyboardMarkup createCurrencyButtons(Long chatId) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        List<String> availableCurrencies = AppRegistry.getCurrency();
        List<String> userCurrency = AppRegistry.getUser(chatId).getCurrency();

        for (String currency : availableCurrencies) {
            String buttonText = (userCurrency.contains(currency) ? "✅ " : "") + currency;
            buttons.add(createButton(buttonText, "currency_" + currency));
        }
        return buildInlineKeyboard(buttons);
    }
    private InlineKeyboardMarkup createBankButtons(Long chatId) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        Map<String, Bank> allBanks = AppRegistry.getBanks();
        List<String> userBanks = AppRegistry.getUser(chatId).getBanks();

        for (Map.Entry<String, Bank> entry : allBanks.entrySet()) {
            String bankName = entry.getValue().getName();
            String bankLocalName = entry.getKey();
            String buttonText = userBanks.contains(bankLocalName) ? "✅ " + bankName : bankName;
            buttons.add(createButton(buttonText, "bank_" + bankLocalName));
        }
        return buildInlineKeyboard(buttons);
    }


    private InlineKeyboardMarkup createMainMenuButtons() {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        // Добавление кнопок для главного меню
        buttons.add(createButton("Отримати інформацію", "get_info"));
        buttons.add(createButton("Налаштування", "settings"));

        return buildInlineKeyboard(buttons);
    }

    private InlineKeyboardMarkup createSettingsButtons() {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        // Добавление кнопок для настроек
        buttons.add(createButton("Кількість знаків після коми", "decimal_places"));
        buttons.add(createButton("Банки", "bank"));
        buttons.add(createButton("Валюти", "currencies"));
        buttons.add(createButton("Час сповіщення", "notification_time"));

        return buildInlineKeyboard(buttons);
    }

    private InlineKeyboardButton createButton(String buttonText, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(new String(buttonText.getBytes(), StandardCharsets.UTF_8));
        button.setCallbackData(callbackData);
        return button;
    }

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
        row.add(new String("Отримати інформацію".getBytes(), StandardCharsets.UTF_8)); // Добавляем кнопку "Отримати інформацію"
        row.add(new String("Стоп".getBytes(), StandardCharsets.UTF_8)); // Добавляем кнопку "Стоп"
        row.add(new String("Налаштування".getBytes(), StandardCharsets.UTF_8)); // Добавляем кнопку "Налаштування"

        keyboard.add(row); // Добавляем ряд в клавиатуру

        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true); // Делаем клавиатуру подгоняемой по размеру
        replyKeyboardMarkup.setOneTimeKeyboard(false); // Клавиатура будет постоянной

        return replyKeyboardMarkup;
    }
}