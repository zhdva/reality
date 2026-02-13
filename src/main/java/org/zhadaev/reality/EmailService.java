package org.zhadaev.reality;

import com.sun.mail.imap.IMAPFolder;
import jakarta.annotation.PostConstruct;
import jakarta.mail.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
public class EmailService {

    private final Properties properties = new Properties();

    private final EmailProperties emailProperties;
    private final TelegramBot telegramBot;

    public EmailService(EmailProperties emailProperties, TelegramBot telegramBot) {
        this.emailProperties = emailProperties;
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    void setup() {
        properties.put("mail.imap.host", emailProperties.getHost());
        properties.put("mail.imap.port", emailProperties.getPort());
        properties.put("mail.imaps.ssl.trust", "*");
        properties.put("mail.imaps.partialfetch", "false");
    }

    public void checkEmail() {
        if (!telegramBot.isAllowedToSendMessage()) {
            return;
        }

        Session emailSession = Session.getDefaultInstance(properties);
        try (Store store = emailSession.getStore("imaps")) {
            store.connect(emailProperties.getHost(), emailProperties.getLogin(), emailProperties.getPassword());

            IMAPFolder inboxFolder = (IMAPFolder) store.getFolder("INBOX");
            processMessages(inboxFolder);

            for (Folder inboxSubfolder: inboxFolder.list()) {
                processMessages((IMAPFolder) inboxSubfolder);
            }

        } catch (Exception e) {
            System.out.println("Ошибка при обработке писем: " + e.getMessage());
        }
    }

    private void processMessages(IMAPFolder inboxFolder) throws MessagingException, IOException, TelegramApiException {
        inboxFolder.open(Folder.READ_WRITE);

        if (inboxFolder.getMessageCount() == 0) {
            inboxFolder.close();
            return;
        }

        for (Message message : inboxFolder.getMessages()) {
            if (!message.getFrom()[0].toString().contains(emailProperties.getLogin())) {
                continue;
            }
            InputStream photo = getPhotoFromMessage(message);
            if (photo != null) {
                telegramBot.sendPhoto(photo);
                message.setFlag(Flags.Flag.SEEN, true);
                inboxFolder.moveMessages(new Message[]{message}, inboxFolder.getStore().getFolder("Детекция движения"));
                break;
            } else {
                inboxFolder.moveMessages(new Message[]{message}, inboxFolder.getStore().getFolder("Корзина"));
            }
        }

        inboxFolder.close();
    }

    private InputStream getPhotoFromMessage(Message message) throws MessagingException, IOException {
        Multipart content = (Multipart) message.getContent();
        for (int i = 0; i < content.getCount(); i++) {
            BodyPart bodyPart = content.getBodyPart(i);
            if (bodyPart.isMimeType(MimeTypeUtils.IMAGE_JPEG_VALUE) || bodyPart.isMimeType("image/jpg")) {
                return bodyPart.getInputStream();
            }
        }
        return null;
    }

    public void removeUnnecessary() {
        Session emailSession = Session.getDefaultInstance(properties);
        try (Store store = emailSession.getStore("imaps")) {
            store.connect(emailProperties.getHost(), emailProperties.getLogin(), emailProperties.getPassword());

            IMAPFolder motionDetectionFolder = (IMAPFolder) store.getFolder("Детекция движения");

            motionDetectionFolder.open(Folder.READ_WRITE);

            int unnecessaryCount = motionDetectionFolder.getMessageCount() - emailProperties.getNecessaryCount();
            if (unnecessaryCount <= 0) {
                motionDetectionFolder.close();
                return;
            }

            Message[] unnecessaryMessages = motionDetectionFolder.getMessages(1, unnecessaryCount);
            for (Message message : unnecessaryMessages) {
                message.setFlag(Flags.Flag.DELETED, true);
            }

            motionDetectionFolder.expunge(unnecessaryMessages);

            motionDetectionFolder.close();

        } catch (Exception e) {
            System.out.println("Ошибка при удалении старых писем: " + e.getMessage());
        }
    }

}
