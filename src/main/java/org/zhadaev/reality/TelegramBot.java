package org.zhadaev.reality;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.stream.IntStream;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    public static final int MESSAGES_PER_SECOND_LIMIT = 1;
    public static final int MESSAGES_PER_MINUTE_LIMIT = 20;

    private final LinkedList<LocalDateTime> sentMessagesQueue = new LinkedList<>();

    @PostConstruct
    void init() {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(1);
        IntStream.range(0, MESSAGES_PER_MINUTE_LIMIT)
                .forEach(i -> sentMessagesQueue.addLast(startTime));
    }

    @Value("${telegram.bot.name}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    @Override
    public void onUpdateReceived(Update update) {}

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public boolean isAllowedToSendMessage() {
        return sentMessagesQueue.getFirst().isBefore(LocalDateTime.now().minusMinutes(1));
    }

    private void updateQueue() {
        sentMessagesQueue.removeFirst();
        sentMessagesQueue.addLast(LocalDateTime.now());
    }

    public void sendPhoto(InputStream photo) throws TelegramApiException {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        InputFile inputFile = new InputFile(photo, "photo");
        sendPhoto.setPhoto(inputFile);
        execute(sendPhoto);
        updateQueue();
    }

}
