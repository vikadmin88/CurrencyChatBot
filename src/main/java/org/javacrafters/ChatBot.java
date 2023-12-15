package org.javacrafters;

import org.javacrafters.banking.NormalizeCurrencyPair;
import org.javacrafters.banking.PrivatBank;
import org.javacrafters.networkclient.NetworkStreamReader;
import org.javacrafters.user.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;



    public class ChatBot extends TelegramLongPollingBot {

        private String appName;
        private String botName;
        private String botToken;
        private Map<Long, User> users;
        private Map<Long, Integer> levels = new HashMap<>();
        private final Map<Integer, String> messages = new HashMap<>();
        private final Map<Integer, Map<String, String>> buttonMessages = new HashMap<>();

        public ChatBot() {

        }

        public ChatBot(Map<Long, User> users, String appName, String botName, String botToken) {
            this.appName = appName;
            this.botName = botName;
            this.botToken = botToken;
            this.users = users;
        }

        @Override
        public String getBotUsername() {
            return this.botName;
        }

        @Override
        public String getBotToken() {

            return this.botToken;
        }

        public void botRun() {
            TelegramBotsApi api = null;
            try {
                api = new TelegramBotsApi(DefaultBotSession.class);
                api.registerBot(new ChatBot(users, appName, botName, botToken));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        public void addUser(User user) {
            System.out.println("test addUser" + user);
            users.put(user.getId(), user);
        }

        public User getUser(Long id) {
            return users.get(id);
        }

        public Map<Long, User> getUsers() {
            return users;
        }

        public void userNotify(User user) {
            System.out.println("userNotify() = " + user.getId() + " " + user.getName());

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(user.getId()));

            String message = createNotifyMessage(user);
            if (message != null) {
                sendMessage.setText(message);
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {

            }

        }

        private String createNotifyMessage(User user) {
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

        @Override
        public void onUpdateReceived(Update update) {
            Long chatId = getChatId(update);

        /*
        System.out.println(update);
        Update(updateId=11446157,
        message=Message(messageId=21, messageThreadId=null,

        from=User(id=501447751, firstName=Viktor, isBot=false, lastName=k, userName=Viktork8888,
        languageCode=ru, canJoinGroups=null, canReadAllGroupMessages=null, supportInlineQueries=null,
        isPremium=null, addedToAttachmentMenu=null),
        date=1691445694,

        chat=Chat(id=501447751, type=private, title=null, firstName=Viktor, lastName=k, userName=Viktork8888,
        photo=null, description=null, inviteLink=null, pinnedMessage=null, stickerSetName=null, canSetStickerSet=null,
        permissions=null, slowModeDelay=null, bio=null, linkedChatId=null, location=null, messageAutoDeleteTime=null,
        hasPrivateForwards=null, HasProtectedContent=null, joinToSendMessages=null, joinByRequest=null,
        hasRestrictedVoiceAndVideoMessages=null, isForum=null, activeUsernames=null, emojiStatusCustomEmojiId=null),

        forwardFrom=null, forwardFromChat=null, forwardDate=null, text=ц, entities=null, captionEntities=null,
        audio=null, document=null, photo=null, sticker=null, video=null, contact=null, location=null, venue=null,
        animation=null, pinnedMessage=null, newChatMembers=[], leftChatMember=null, newChatTitle=null,
        newChatPhoto=null, deleteChatPhoto=null, groupchatCreated=null, replyToMessage=null, voice=null,
        caption=null, superGroupCreated=null, channelChatCreated=null, migrateToChatId=null, migrateFromChatId=null,
        editDate=null, game=null, forwardFromMessageId=null, invoice=null, successfulPayment=null, videoNote=null,
        authorSignature=null, forwardSignature=null, mediaGroupId=null, connectedWebsite=null, passportData=null,
        forwardSenderName=null, poll=null, replyMarkup=null, dice=null, viaBot=null, senderChat=null,
        proximityAlertTriggered=null, messageAutoDeleteTimerChanged=null, isAutomaticForward=null,
        hasProtectedContent=null, webAppData=null, videoChatStarted=null, videoChatEnded=null,
        videoChatParticipantsInvited=null, videoChatScheduled=null, isTopicMessage=null, forumTopicCreated=null,
        forumTopicClosed=null, forumTopicReopened=null),
        inlineQuery=null, chosenInlineQuery=null, callbackQuery=null, editedMessage=null, channelPost=null,
        editedChannelPost=null, shippingQuery=null, preCheckoutQuery=null, poll=null, pollAnswer=null,
        myChatMember=null, chatMember=null, chatJoinRequest=null)
         */

            // Messages processing
            if (update.hasMessage()) {
                if (update.getMessage().getText().equals("/start")) {
//                    sendMessage(chatId);
                    System.out.println("update.getMessage() = " + update.getMessage());
                    System.out.println("getUser(chatId) = " + getUser(chatId));
                    if (getUser(chatId) == null) {
                        System.out.println("before user");
                        User user = new User(chatId, update.getMessage().getFrom().getFirstName(), update.getMessage().getFrom().getUserName());
                        user.setBank(new PrivatBank(new NetworkStreamReader()));
                        user.addCurrency("USD");
                        user.addCurrency("EUR");
                        System.out.println("after user");
                        addUser(user);
                        System.out.println("getUser(chatId) = " + getUser(chatId));
                        System.out.println("user" + user);
                        System.out.println("getUser(chatId).toString() = " + getUser(chatId));
                    }
                }
            }

            // Callbacks processing
            if (update.hasCallbackQuery()) {

                if (update.getCallbackQuery().getData().equals("level_1_task")) {
                    sendMessage(chatId);
                }
            }
        }

        public Long getChatId(Update update) {
            if (update.hasMessage()) {
                return update.getMessage().getFrom().getId();
            }
            if (update.hasCallbackQuery()) {
                return update.getCallbackQuery().getFrom().getId();
            }
            return null;
        }

        public SendMessage createMessage(String text) {
            SendMessage message = new SendMessage();
            message.setText(new String(text.getBytes(), StandardCharsets.UTF_8));
            message.setParseMode("markdown");
            return message;
        }

        public void attachButtons(SendMessage message, Map<String, String> buttons) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            for (String buttonName : buttons.keySet()) {
                String buttonValue = buttons.get(buttonName);
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(new String(buttonName.getBytes(), StandardCharsets.UTF_8));
                button.setCallbackData(buttonValue);
                keyboard.add(Arrays.asList(button));
            }

            markup.setKeyboard(keyboard);
            message.setReplyMarkup(markup);
        }

//        public void sendImage(String name, Long chatId) {
//            SendAnimation animation = new SendAnimation();
//            InputFile inputFile = new InputFile();
//            inputFile.setMedia(new File("images/" + name + ".gif"));
//            animation.setAnimation(inputFile);
//            animation.setChatId(chatId);
//            executeAsync(animation);
//        }

        {
            messages.put(1, "*Джавелін твій. Повний вперед!*");
        }

        {
            buttonMessages.put(1, Map.of(
                    "Купити Джавелін (50 монет)", "level_4_task"
            ));
        }

        public void sendMessage(Long chatId) {
            SendMessage message = createMessage(messages.get(chatId));
            message.setChatId(chatId);

            Map<String, String> messageCommand = new HashMap<>();
            messageCommand.put("Налаштування", "get_conf");
            messageCommand.put("Отримати інформацію", "get_info");
            attachButtons(message, messageCommand);

            sendApiMethodAsync(message);
        }


//    private final Map<Integer, Map<String, String>> buttonMessages = new HashMap<>();
//    {
//            buttonMessages.put(1, Map.of(
//            "Сплести маскувальну сітку (+15 монет)", "level_1_task",
//            "Зібрати кошти патріотичними піснями (+15 монет)", "level_1_task",
//            "Вступити в Міністерство Мемів України (+15 монет)", "level_1_task",
//            "Запустити волонтерську акцію (+15 монет)","level_1_task",
//            "Вступити до лав тероборони (+15 монет)","level_1_task"
//            ));

    }