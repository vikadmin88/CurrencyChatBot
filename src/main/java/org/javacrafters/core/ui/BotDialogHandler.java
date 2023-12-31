package org.javacrafters.core.ui;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.*;
import java.nio.charset.StandardCharsets;
/**
 * MVC: View
 * @author AlekseyB belovmladshui@gmail.com
 */
public class BotDialogHandler {
    private static final String URL_MEDIA = "https://epowhost.com/currency_chat_bot";
    private final Long chatId;
    private final MessageFactory messageFactory;
    private final ButtonFactory buttonFactory;

    public BotDialogHandler(Long chatId) {
        this.chatId = chatId;
        this.messageFactory = new MessageFactory(chatId);
        this.buttonFactory = new ButtonFactory();
    }

    // Стартовое сообщение
    public SendPhoto createWelcomeMessage() {
        String caption = "<b>Ласкаво просимо.</b> \nЦей бот допоможе відслідковувати актуальні курси валют...";
        SendPhoto photoMessage = MessageFactory.createPhotoMessage(URL_MEDIA + "/welcome_message.jpg", caption);
        photoMessage.setReplyMarkup(ButtonFactory.getReplyKeyboardMarkup());
        return photoMessage;
    }

    public SendPhoto createAboutUsMessage() {
        String caption = "Розробник: <b>JavaCrafters Team</b>\nРепозиторій проєкту: https://github.com/vikadmin88/CurrencyChatBot";
        SendPhoto photoMessage = MessageFactory.createPhotoMessage(URL_MEDIA + "/about_us.jpg", caption);
        photoMessage.setReplyMarkup(ButtonFactory.getReplyKeyboardMarkup());
        return photoMessage;
    }

    public SendMessage createSettingsMessage() {
        String text = "⚙ <b>Налаштування</b>";
        SendMessage message = MessageFactory.createMessage(chatId, text);
        message.setReplyMarkup(ButtonFactory.getInlineKeyboardMarkup(getSettingsOptions(), "settings", new ArrayList<>()));
        return message;
    }

    public SendMessage createSetNotifyMessage() {
        String text = "⏰ <b>Виберіть час сповіщення</b>";
        SendMessage message = MessageFactory.createMessage(chatId, text);
        message.setReplyMarkup(ButtonFactory.getTimeKeyboard());
        return message;
    }

    public EditMessageText onDecimalMessage(Integer messageId) {
        String text = "<b>Знаків після коми</b>";
        return MessageFactory.editMessage(messageId, text);
    }

    // Другие методы...

    private Map<String, String> getSettingsOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("bank", "Банки");
        options.put("currency", "Валюти");
        options.put("decimal", "Знаків після коми");
        options.put("notification", "Час сповіщення");
        return options;
    }
}
