package org.javacrafters.core;

import org.javacrafters.banking.Bank;
import org.javacrafters.banking.NormalizeCurrencyPair;
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
        message.setReplyMarkup(getSectionButtons(Arrays.asList(BT.GET_INFO, BT.SETTINGS)));
        return message;
    }

    //Сообщение настроек
    public SendMessage createSettingMessage(Long chatId){
        String text = "Ваші налаштування";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getSectionButtons(Arrays.asList(BT.BANK, BT.CURRENCIES, BT.DECIMAL_PLACES, BT.NOTIFICATION_TIME, BT.TO_MAIN)));
        return message;
    }


    //Сообщение банк
    public SendMessage createBankMessage(Long chatId){
        String text = "Виберіть банк";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getAdditionalButtons(Arrays.asList(BT.NBU, BT.MB, BT.PB, BT.TO_SETTINGS)));
        return message;
    }
    //Сообщение знаки после запятой
    public SendMessage createDecimalMessage(Long chatId){
        String text = "Кількість знаків після коми";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getAdditionalButtons(Arrays.asList(BT.TWO_DIGITS, BT.THREE_DIGITS, BT.FOUR_DIGITS, BT.TO_SETTINGS)));
        return message;
    }
    //Сообщение вылюты
    public SendMessage createCurrencyMessage(Long chatId){
        String text = "Виберіть валюту";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getAdditionalButtons(Arrays.asList(BT.USD, BT.EUR, BT.TO_SETTINGS)));
        return message;
    }
    //Сообщение время уведомления
    public SendMessage createSetNotifyMessage(Long chatId){
        String text = "Виберіть час сповіщення";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getTimeKeyboard());
        return message;
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
    //Метод для выбора доп кнопок под сообщение
    private InlineKeyboardMarkup getAdditionalButtons(List<BT> buttonsTypes) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        int currentNumOfDigits = AppRegistry.getConfCountLastDigits();
        List<String> availableCurrencies = AppRegistry.getCurrency();
        Map<String, Bank> banks = AppRegistry.getBanks();

        for (BT bt : buttonsTypes) {
            String buttonText = "";
            switch (bt) {
                case TO_SETTINGS -> buttonText = "Повернутись до налаштування";
                case TWO_DIGITS -> buttonText = "2" + (currentNumOfDigits == 2 ? " ✅" : "");
                case THREE_DIGITS -> buttonText = "3" + (currentNumOfDigits == 3 ? " ✅" : "");
                case FOUR_DIGITS -> buttonText = "4" + (currentNumOfDigits == 4 ? " ✅" : "");
                case USD -> buttonText = "USD" + (availableCurrencies.contains("USD") ? " ✅" : "");
                case EUR -> buttonText = "EUR" + (availableCurrencies.contains("EUR") ? " ✅" : "");
                case NBU -> buttonText = "Національний банк України" + (banks.containsKey("NBU") ? " ✅" : "");
                case MB -> buttonText = "МоноБанк" + (banks.containsKey("MB") ? " ✅" : "");
                case PB -> buttonText = "ПриватБанк" + (banks.containsKey("PB") ? " ✅" : "");
            }
            buttons.add(createButton(buttonText, bt.name().toLowerCase()));
        }
        return buildInlineKeyboard(buttons);
    }
    //метод для выбора кнопок разделов
    private InlineKeyboardMarkup getSectionButtons(List<BT> buttonsTypes) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        for (BT bt : buttonsTypes) {
            String buttonText = "";
            switch (bt) {
                //MAIN BUTTONS
                case GET_INFO -> buttonText = "Отримати інформацію";
                case SETTINGS -> buttonText = "Налаштування";
                case TO_MAIN -> buttonText = "На головну";
                //SETTING BUTTONS
                case DECIMAL_PLACES -> buttonText = "Кількість знаків після коми";
                case BANK -> buttonText = "Банки";
                case CURRENCIES -> buttonText = "Валюти";
                case NOTIFICATION_TIME -> buttonText = "Час сповіщення";

            }
            buttons.add(createButton(buttonText, bt.name().toLowerCase()));
        }
        return buildInlineKeyboard(buttons);
    }
//    public EditMessageText updateBankSelectionMessage(Long chatId, Integer messageId, User user) {
//        return createEditMessage(chatId, messageId, "Виберіть банк", user,
//                Arrays.asList(BT.NBU, BT.MB, BT.PB, BT.TO_SETTINGS));
//    }
//    public EditMessageText updateCurrencySelectionMessage(Long chatId, Integer messageId, User user) {
//        return createEditMessage(chatId, messageId, "Виберіть валюту", user,
//                Arrays.asList(BT.USD, BT.EUR, BT.TO_SETTINGS));
//    }
//    public EditMessageText updateNumOfDigitsSelectionMessage(Long chatId, Integer messageId, User user) {
//        return createEditMessage(chatId, messageId, "Кількість знаків після коми", user,
//                Arrays.asList(BT.TWO_DIGITS, BT.THREE_DIGITS, BT.FOUR_DIGITS, BT.TO_SETTINGS));
//    }

    private EditMessageText createEditMessage(Long chatId, Integer messageId, String messageText, List<BT> buttonsTypes) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(String.valueOf(chatId));
        newMessage.setMessageId(messageId);
        newMessage.setText(new String(messageText.getBytes(), StandardCharsets.UTF_8));
        newMessage.setReplyMarkup(getAdditionalButtons(buttonsTypes));
        return newMessage;
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
}