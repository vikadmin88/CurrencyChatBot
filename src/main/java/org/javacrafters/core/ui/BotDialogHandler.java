package org.javacrafters.core.ui;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.nio.charset.StandardCharsets;
/**
 * MVC: View
 * @author AlekseyB belovmladshui@gmail.com
 */
public class BotDialogHandler {
    private static final String URL_MEDIA = "https://epowhost.com/currency_chat_bot";
    private final Long chatId;
    private final ButtonFactory bf = new ButtonFactory();

    public BotDialogHandler(Long chatId) {
        this.chatId = chatId;
    }
    // Стартовое сообщение
    public SendPhoto createWelcomeMessage() {
        String text = """
                <b>Ласкаво просимо.</b> \nЦей бот допоможе відслідковувати актуальні курси валют 
                і присилати їх вам у зручний для вас час, який ви оберете в конфігурації бота.
                (За замовчуванням ви отримуватимете курс USD з банку Приват Банк кожну добу о 9:00)
                 
                Ви можете настроїти наступні чотири парамерти:
                Банки: Приват Банк, Моно Банк, НБУ
                Валюти: USD, EUR, GBP, PLN
                Знаків після коми: 2, 3, 4 
                (точність для курсів валют)
                Час отримання повідомлень: 9:00 - 18:00 
                (приходитимуть раз на добу)
                """;
        SendPhoto photoMessage = createPhotoMessage(URL_MEDIA + "/welcome_message.jpg");
        photoMessage.setCaption(new String(text.getBytes(), StandardCharsets.UTF_8));
        photoMessage.setParseMode(ParseMode.HTML);

        ReplyKeyboardMarkup keyboardMarkup = bf.getPermanentKeyboard();
        photoMessage.setReplyMarkup(keyboardMarkup);

        return photoMessage;
    }
    public SendPhoto createAboutUsMessage(){
        //нужно написать текст
        String text = """
                Розробник: <b>JavaCrafters Team</b>
                Репозиторій проєкту: https://github.com/vikadmin88/CurrencyChatBot 
                """;
        //можно заменить фото
        SendPhoto photoMessage = createPhotoMessage(URL_MEDIA + "/about_us.jpg");
        photoMessage.setCaption(new String(text.getBytes(), StandardCharsets.UTF_8));
        photoMessage.setParseMode(ParseMode.HTML);

        ReplyKeyboardMarkup keyboardMarkup = bf.getPermanentKeyboard();
        photoMessage.setReplyMarkup(keyboardMarkup);

        return photoMessage;
    }
    public SendMessage createUserSettingsMessage(String text) {
        SendMessage message = createMessage(text);
        message.setText(new String(text.getBytes(), StandardCharsets.UTF_8));
        message.setParseMode(ParseMode.HTML);
        message.setChatId(chatId);

        ReplyKeyboardMarkup keyboardMarkup = bf.getPermanentKeyboard();
        message.setReplyMarkup(keyboardMarkup);

        return message;
    }

    public SendMessage createCustomMessage(String textMessage) {
        String text = "" + textMessage;
        SendMessage message = createMessage(text);
        message.setReplyMarkup(bf.getPermanentKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }
    //Сообщение с настройками
    public  SendMessage createSettingsMessage(){
        String text = "⚙   <b>Налаштування</b>";
        SendMessage message = createMessage(text);
        message.setReplyMarkup(bf.createSettingsButtons());
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    //Сообщение время уведомления
    public SendMessage createSetNotifyMessage(){
        String text = "⏰  <b>Виберіть час сповіщення</b>";
        SendMessage message = createMessage(text);
        message.setReplyMarkup(bf.getTimeKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }
    public EditMessageText onDecimalMessage(Integer messageId) {
        String text = "<b>Знаків після коми</b>";
        String emoji = "\uD83D\uDD22";
        return createEditMessage(messageId, emoji, text, BT.DEC_BUT);
    }
    public EditMessageText onBankMessage(Integer messageId) {
        String text = "<b>Банки</b>";
        String emoji = "\uD83C\uDFE6";
        return createEditMessage(messageId, emoji, text, BT.BAN_BUT);
    }
    public EditMessageText onCurrencyMessage(Integer messageId) {
        String text = "<b>Валюти</b>";
        String emoji = "\uD83D\uDCB5";
        return createEditMessage(messageId, emoji, text, BT.CUR_BUT);
    }
    public EditMessageText onAboutUsMessage(Integer messageId) {
        String text = "<b>Про нас</b>";
        String emoji = "\uD83D\uDC40";
        return createEditMessage(messageId, emoji, text, BT.ABOUT_BUT);
    }
    public SendPhoto createPhotoMessage(String imageUrl) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId.toString());
        photo.setPhoto(new InputFile(imageUrl));
        return photo;
    }

    // Метод для добавления кнопок к разделам настроек (без emoji)
    private EditMessageText createEditMessage(Integer messageId, String messageText, BT buttonType) {
        return createEditMessage(messageId, "", messageText, buttonType);
    }

    // Метод для добавления кнопок к разделам настроек (с emoji)
    private EditMessageText createEditMessage(Integer messageId, String emoji, String messageText, BT buttonType) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(String.valueOf(chatId));
        newMessage.setMessageId(messageId);
        newMessage.setText(emoji.isEmpty() ? new String(messageText.getBytes(), StandardCharsets.UTF_8) : emoji + "  " + new String(messageText.getBytes(), StandardCharsets.UTF_8));
        newMessage.setParseMode(ParseMode.HTML);

        InlineKeyboardMarkup replyMarkup = switch (buttonType) {
            case BAN_BUT -> bf.createBankButtons(chatId);
            case CUR_BUT -> bf.createCurrencyButtons(chatId);
            case DEC_BUT -> bf.createDecimalButtons(chatId);
            case SETTINGS -> bf.createSettingsButtons();
            case ABOUT_BUT -> null;
            default -> bf.createMainMenuButtons();
        };

        newMessage.setReplyMarkup(replyMarkup);
        return newMessage;
    }
    //Создание сообщения
    public SendMessage createMessage(String text) {
        SendMessage message = new SendMessage();
        message.setText(new String(text.getBytes(), StandardCharsets.UTF_8));
        message.setParseMode(ParseMode.HTML);
        message.setChatId(chatId);
        return message;
    }
}