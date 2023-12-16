package org.javacrafters;

import org.javacrafters.banking.NormalizeCurrencyPair;
import org.javacrafters.user.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BotDialogHandler{

    // Стартовое сообщение
    public SendMessage createWelcomeMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Ласкаво просимо. Цей бот допоможе відслідковувати актуальні курси валют");
        message.setReplyMarkup(getMainMenuKeyboard());
        return message;
    }

    // Главное меню
    private InlineKeyboardMarkup getMainMenuKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Отримати інфо");
        button.setCallbackData("get_info");
        row.add(button);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Налаштування");
        button2.setCallbackData("settings");
        row.add(button2);

        keyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        return inlineKeyboardMarkup;
    }

    public String createNotifyMessage(User user) {
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

    public SendMessage createMessage(String text) {
        SendMessage message = new SendMessage();
        message.setText(new String(text.getBytes(), StandardCharsets.UTF_8));
        message.setParseMode("markdown");
        return message;
    }


}

