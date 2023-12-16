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
        message.setReplyMarkup(getButtons(Arrays.asList("get_info", "settings")));
        return message;
    }
    //Текст уведомления
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
//    public SendMessage getInfo(Long chatId){
//        String text = "Ласкаво просимо. Цей бот допоможе відслідковувати актуальні курси валют";
//        SendMessage message = new SendMessage();
//        message.setChatId(String.valueOf(chatId));
//        message.setText(new String(text.getBytes(), StandardCharsets.UTF_8));
//        message.setReplyMarkup();
//        return message;
//    }
    public SendMessage getSettingMessage(Long chatId){
        String text = "Ваші налаштування";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getButtons(Arrays.asList("bank", "currencies", "decimal_places", "notification_time")));
        return message;
    }

    //Метод для выбора нужных кнопок под сообщение
    private InlineKeyboardMarkup getButtons(List<String> buttonsNames){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (String buttonName : buttonsNames) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            switch (buttonName) {
                //MAIN BUTTONS
                case "get_info":
                    button.setText(new String("Отримати інфо".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("get_info");
                    break;
                case "settings":
                    button.setText(new String("Налаштування".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("settings");
                    break;
                case "to_main":
                    button.setText(new String("На головну".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("to_main");
                    break;
                //SETTINGS BUTTONS
                case "decimal_places":
                    button.setText(new String("Кількість знаків після коми".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("decimal_places");
                    break;
                case "bank":
                    button.setText(new String("Банк".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("bank");
                    break;
                case "currencies":
                    button.setText(new String("Валюти".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("currencies");
                    break;
                case "notification_time":
                    button.setText(new String("Час сповіщення".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("notification_time");
                    break;
                //DECIMAL PLACES BUTTONS
                case "2":
                    button.setText(new String("2".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("2");
                    break;
                case "3":
                    button.setText(new String("3".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("3");
                    break;
                case "4":
                    button.setText(new String("4".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("4");
                    break;
                //CURRENCIES BUTTONS
                case "usd":
                    button.setText(new String("USD".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("usd");
                    break;
                case "eur":
                    button.setText(new String("EUR".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("eur");
                    break;
                //BANKS BUTTONS
                case "nbu":
                    button.setText(new String("Національний банк України".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("nbu");
                    break;
                case "mono":
                    button.setText(new String("МоноБанк".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("mono");
                    break;
                case "privat":
                    button.setText(new String("ПриватБанк".getBytes(), StandardCharsets.UTF_8));
                    button.setCallbackData("currencies");
                    break;
            }
            row.add(button);
        }
        keyboard.add(row);
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

