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
/**
 * MVC: View
 * @author AlekseyB belovmladshui@gmail.com
 */
public class BotDialogHandler {

    private final Long chatId;


    public BotDialogHandler(Long chatId) {
        this.chatId = chatId;
    }
    // Стартовое сообщение
    public SendMessage createWelcomeMessage() {
        String text = "<b>Ласкаво просимо.</b> \nЦей бот допоможе відслідковувати актуальні курси валют!";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getPermanentKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }
    public SendMessage createCustomMessage(String textMessage) {
        String text = "" + textMessage;
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getPermanentKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }
    //Сообщение с настройками
    public  SendMessage createSettingsMessage(){
        String text = "⚙   <b>Налаштування</b>";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(createSettingsButtons());
//        message.setReplyMarkup(getPermanentKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    //Сообщение время уведомления
    public SendMessage createSetNotifyMessage(){
        String text = "⏰  <b>Виберіть час сповіщення</b>";
        SendMessage message = createMessage(text, chatId);
        message.setReplyMarkup(getTimeKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    public EditMessageText onSettingMessage(Integer messageId) {
        String text = "⚒  <b>Налаштування</b>";
        return createEditMessage(chatId, messageId, text, BT.SETTINGS);
    }
    public EditMessageText onDecimalMessage(Integer messageId) {
        String text = "<b>Кількість знаків після коми</b>";
        String emoji = "\uD83D\uDD22";
        return createEditMessage(chatId, messageId, emoji, text, BT.DEC_BUT);
    }
    public EditMessageText onBankMessage(Integer messageId) {
        String text = "<b>Банки</b>";
        String emoji = "\uD83C\uDFE6";
        return createEditMessage(chatId, messageId, emoji, text, BT.BAN_BUT);
    }
    public EditMessageText onCurrencyMessage(Integer messageId) {
        String text = "<b>Валюти</b>";
        String emoji = "\uD83D\uDCB5";
        return createEditMessage(chatId, messageId, emoji, text, BT.CUR_BUT);
    }

    // Метод для добавления кнопок к разделам настроек (без emoji)
    private EditMessageText createEditMessage(Long chatId, Integer messageId, String messageText, BT buttonType) {
        return createEditMessage(chatId, messageId, "", messageText, buttonType);
    }

    // Метод для добавления кнопок к разделам настроек (с emoji)
    private EditMessageText createEditMessage(Long chatId, Integer messageId, String emoji, String messageText, BT buttonType) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(String.valueOf(chatId));
        newMessage.setMessageId(messageId);
        newMessage.setText(emoji.isEmpty() ? new String(messageText.getBytes(), StandardCharsets.UTF_8) : emoji + "  " + new String(messageText.getBytes(), StandardCharsets.UTF_8));
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
        // Добавляем последний ряд, если он не пустой
        if (!row.isEmpty()) {
            keyboard.add(row);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }
    // создание кнопок - сколько знаков после запятой
    private InlineKeyboardMarkup createDecimalButtons(Long chatId) {
        Map<String, String> decimalOptions = new HashMap<>();
        for (int i = 2; i <= 4; i++) {
            decimalOptions.put(String.valueOf(i), String.valueOf(i));
        }
        List<InlineKeyboardButton> buttons = createButtonsList(decimalOptions, "decimal", List.of(String.valueOf(AppRegistry.getUser(chatId).getCountLastDigits())));
        return buildInlineKeyboard(buttons);
    }

    // создание кнопок - валюты
    private InlineKeyboardMarkup createCurrencyButtons(Long chatId) {
        Map<String, String> currencyOptions = new HashMap<>();
        for (String currency : AppRegistry.getCurrency()) {
            currencyOptions.put(currency, currency);
        }
        List<InlineKeyboardButton> buttons = createButtonsList(currencyOptions, "currency", AppRegistry.getUser(chatId).getCurrency());
        return buildInlineKeyboard(buttons);
    }

    // создание кнопок - банки
    private InlineKeyboardMarkup createBankButtons(Long chatId) {
        Map<String, String> bankOptions = new HashMap<>();
        for (Map.Entry<String, Bank> entry : AppRegistry.getBanks().entrySet()) {
            bankOptions.put(entry.getKey(), entry.getValue().getName());
        }
        List<InlineKeyboardButton> buttons = createButtonsList(bankOptions, "bank", AppRegistry.getUser(chatId).getBanks());
        return buildInlineKeyboard(buttons);
    }

    //создание кнопок - сколько знаков после запятой
    private InlineKeyboardMarkup createMainMenuButtons() {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        // Добавление кнопок для главного меню
        buttons.add(createButton("\uD83C\uDFA2", "Курси валют", "get_info"));
        buttons.add(createButton("⚙ Налаштування", "settings"));

        return buildInlineKeyboard(buttons);
    }

    private InlineKeyboardMarkup createSettingsButtons() {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        // Добавление кнопок для настроек
        buttons.add(createButton("\uD83C\uDFE6", "Банки", "bank"));
        buttons.add(createButton("\uD83D\uDCB5", "Валюти", "currency"));
        buttons.add(createButton("\uD83D\uDD22", "Кількість знаків після коми", "decimal"));
        buttons.add(createButton("⏰ Час сповіщення", "notification"));

        return buildInlineKeyboard(buttons);
    }

    // Унифицированный метод для создания кнопки (emoji опционально)
    private InlineKeyboardButton createButton(String buttonText, String callbackData) {
        return createButton("", buttonText, callbackData);
    }

    // Метод для создания кнопки с указанным emoji (или без него)
    private InlineKeyboardButton createButton(String emoji, String buttonText, String callbackData) {
        String fullText = emoji.isEmpty() ? new String(buttonText.getBytes(), StandardCharsets.UTF_8)
                : emoji + " " + new String(buttonText.getBytes(), StandardCharsets.UTF_8);
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(fullText);
        button.setCallbackData(callbackData);
        return button;
    }
    //метод создания клавиатуры кнопок под разделы
    private List<InlineKeyboardButton> createButtonsList(Map<String, String> items, String prefix, List<String> userSelection) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (Map.Entry<String, String> item : items.entrySet()) {
            String buttonText = userSelection.contains(item.getKey()) ? "✅  " + item.getValue() : item.getValue();
            buttons.add(createButton(buttonText, prefix + "_" + item.getKey()));
        }
        return buttons;
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
        message.setParseMode(ParseMode.HTML);
        message.setChatId(chatId);
        return message;
    }

    //клавиатура пользователя
    public ReplyKeyboardMarkup getPermanentKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Создаем один ряд кнопок
        KeyboardRow row = new KeyboardRow();
        row.add("\uD83C\uDFA2" + new String(" Курси валют".getBytes(), StandardCharsets.UTF_8));
        row.add(new String("❌ Стоп".getBytes(), StandardCharsets.UTF_8));
        row.add(new String("⚙ Налаштування".getBytes(), StandardCharsets.UTF_8));

        keyboard.add(row);

        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true); // Делаем клавиатуру подгоняемой по размеру
        replyKeyboardMarkup.setOneTimeKeyboard(false); // Клавиатура будет постоянной
        return replyKeyboardMarkup;
    }
}