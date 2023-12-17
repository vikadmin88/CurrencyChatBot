package org.javacrafters.core;

import org.javacrafters.banking.NormalizeCurrencyPair;
import org.javacrafters.user.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

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
        message.setReplyMarkup(getButtons(Arrays.asList(BT.GET_INFO, BT.SETTINGS)));
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
        message.setReplyMarkup(getButtons(Arrays.asList(BT.TO_MAIN)));
        return message;
    }
    //Сообщение настроек
    public SendMessage createSettingMessage(Long chatId){
        String text = "Ваші налаштування";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getButtons(Arrays.asList(BT.BANK, BT.CURRENCIES, BT.DECIMAL_PLACES, BT.NOTIFICATION_TIME, BT.TO_MAIN)));
        return message;
    }


    //Сообщение банк
    public SendMessage createBankMessage(Long chatId){
        String text = "Виберіть банк";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getButtons(Arrays.asList(BT.NBU, BT.MONO, BT.PRIVAT, BT.TO_MAIN)));
        return message;
    }
    //Сообщение знаки после запятой
    public SendMessage createDecimalMessage(Long chatId){
        String text = "Виберіть кількість знаків після коми";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getButtons(Arrays.asList(BT.TWO_DIGITS, BT.THREE_DIGITS, BT.FOUR_DIGITS, BT.TO_MAIN)));
        return message;
    }
    //Сообщение вылюты
    public SendMessage createCurrencyMessage(Long chatId){
        String text = "Виберіть валюту";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getButtons(Arrays.asList(BT.USD, BT.EUR, BT.TO_MAIN)));
        return message;
    }
    //Сообщение время уведомления
//    public SendMessage createNotifyMessage(Long chatId){
//        String text = "Виберіть кількість знаків після коми";
//        SendMessage message = createMessage(text, chatId);
//        message.setReplyMarkup(getButtons(Arrays.asList(BT.TWO_DIGITS, BT.THREE_DIGITS, BT.FOUR_DIGITS, BT.TO_MAIN)));
//        return message;
//    }

    //Метод для выбора нужных кнопок под сообщение
    private InlineKeyboardMarkup getButtons(List<BT> buttonsTypes) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (BT bt : buttonsTypes) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            switch (bt) {
                //MAIN BUTTONS
                case GET_INFO -> {
                    button.setText(new String("Отримати інфо".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("get_info");
                }
                case SETTINGS -> {
                    button.setText(new String("Налаштування".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("settings");
                }
                case TO_MAIN -> {
                    button.setText(new String("На головну".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("to_main");
                }
                //SETTING BUTTONS
                case DECIMAL_PLACES -> {
                    button.setText(new String("Кількість знаків після коми".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("decimal_places");
                }
                case BANK -> {
                    button.setText(new String("Банк".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("bank");
                }
                case CURRENCIES -> {
                    button.setText(new String("Валюти".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("currencies");
                }
                case NOTIFICATION_TIME -> {
                    button.setText(new String("Час сповіщення".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("notification_time");
                }
                //DECIMAL BUTTONS
                case TWO_DIGITS -> {
                    button.setText(new String("2".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("two_digits");
                }
                case THREE_DIGITS -> {
                    button.setText(new String("3".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("three_digits");
                }
                case FOUR_DIGITS -> {
                    button.setText(new String("4".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("four_digits");
                }
                //CURRENCIES BUTTONS
                case USD -> {
                    button.setText(new String("USD".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("usd");
                }
                case EUR -> {
                    button.setText(new String("EUR".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("eur");
                }
                //BANK BUTTONS
                case NBU -> {
                    button.setText(new String("Національний банк України".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("nbu");
                }
                case MONO -> {
                    button.setText(new String("МоноБанк".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("mono");
                }
                case PRIVAT -> {
                    button.setText(new String("ПриватБанк".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("privat");
                }
            }
            List<InlineKeyboardButton> row = new ArrayList<>(); // Создает новый ряд для каждой кнопки
            row.add(button); // Добавляет кнопку в ряд
            keyboard.add(row); // Добавляет ряд в клавиатуру
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

