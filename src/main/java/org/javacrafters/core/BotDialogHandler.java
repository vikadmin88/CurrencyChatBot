package org.javacrafters.core;

import org.javacrafters.banking.NormalizeCurrencyPair;
import org.javacrafters.user.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BotDialogHandler{

    // Стартовое сообщение
    public SendMessage createWelcomeMessage(Long chatId) {
        String text = "Ласкаво просимо. Цей бот допоможе відслідковувати актуальні курси валют";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getSectionButtons(Arrays.asList(BT.GET_INFO, BT.SETTINGS)));
        return message;
    }
    //Текст уведомления, а также текст курса валют
    public String getCurrencyRate(User user) {
        StringBuilder sb = new StringBuilder("Поточні курси валют:\n");
        sb.append(user.getBank().getName()).append("\n");

        Map<String, NormalizeCurrencyPair> currencyRates = user.getBank().getRates();

        for (String currency : user.getCurrencies()) {
            NormalizeCurrencyPair curCurrency = currencyRates.get(currency);
            if (currency.equals(curCurrency.getName())) {
                sb.append(curCurrency.getName()).append("\n");
                sb.append("Покупка: ");
                sb.append(curCurrency.getBuy()).append("\n");
                sb.append("Продаж: ");
                sb.append(curCurrency.getSale()).append("\n\n");
            }
        }
        return !sb.toString().isEmpty() ? sb.toString() : null;
    }
    //Сообщение информации
    public SendMessage createInfoMessage(User user, Long chatId){
        String text = getCurrencyRate(user);
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getSectionButtons(Arrays.asList(BT.TO_MAIN)));
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
    public SendMessage createBankMessage(User user, Long chatId){
        String text = "Виберіть банк";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getAdditionalButtons(user, Arrays.asList(BT.NBU, BT.MONO, BT.PRIVAT, BT.TO_SETTINGS)));
        return message;
    }
    //Сообщение знаки после запятой
    public SendMessage createDecimalMessage(User user, Long chatId){
        String text = "Виберіть кількість знаків після коми";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getAdditionalButtons(user, Arrays.asList(BT.TWO_DIGITS, BT.THREE_DIGITS, BT.FOUR_DIGITS, BT.TO_SETTINGS)));
        return message;
    }
    //Сообщение вылюты
    public SendMessage createCurrencyMessage(User user, Long chatId){
        String text = "Виберіть валюту";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getAdditionalButtons(user, Arrays.asList(BT.USD, BT.EUR, BT.TO_SETTINGS)));
        return message;
    }
    //Сообщение время уведомления
    public SendMessage createSetNotifyMessage(Long chatId){
        String text = "Виберіть час сповіщення";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getTimeKeyboard());
        return message;
    }
    // Метод для создания клавиатуры пользователя с выбором времени
    private ReplyKeyboardMarkup getTimeKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        // Добавление кнопок с временем
        for (int hour = 9; hour <= 18; hour++) {
            row.add(Integer.toString(hour) + ":00");
            if ((hour - 8) % 3 == 0) { // Например, разбиваем на ряды по 3 кнопки
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
    private InlineKeyboardMarkup getAdditionalButtons(User user, List<BT> buttonsTypes) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        int currentNumOfDigits = user.getNumOfDigits(); // Текущее количество знаков после запятой
        List<String> curr = user.getCurrencies();
        String bank = user.getBank().getLocalName();

        for (BT bt : buttonsTypes) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            String buttonText = "";
            switch (bt) {
                case TO_SETTINGS -> buttonText = "До налаштувань";
                case TWO_DIGITS -> buttonText = "2" + (currentNumOfDigits == 2 ? " ✅" : "");
                case THREE_DIGITS -> buttonText = "3" + (currentNumOfDigits == 3 ? " ✅" : "");
                case FOUR_DIGITS -> buttonText = "4" + (currentNumOfDigits == 4 ? " ✅" : "");
                case USD -> buttonText = "USD" + (curr.contains("USD") ? " ✅" : "");
                case EUR -> buttonText = "EUR" + (curr.contains("EUR") ? " ✅" : "");
                case NBU -> buttonText = "Національний банк України" + (bank.equals("NBU") ? " ✅" : "");
                case MONO -> buttonText = "МоноБанк" + (bank.equals("MB") ? " ✅" : "");
                case PRIVAT -> buttonText = "ПриватБанк" + (bank.equals("PB") ? " ✅" : "");

            }
            button.setText(new String(buttonText.getBytes(), StandardCharsets.UTF_8));
            button.setCallbackData(bt.name().toLowerCase());
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
        }

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }
    //метод для выбора кнопок разделов
    private InlineKeyboardMarkup getSectionButtons(List<BT> buttonsTypes) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (BT bt : buttonsTypes) {
            InlineKeyboardButton button = new InlineKeyboardButton();
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
            button.setText(new String(buttonText.getBytes(), StandardCharsets.UTF_8));
            button.setCallbackData(bt.name().toLowerCase());
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

