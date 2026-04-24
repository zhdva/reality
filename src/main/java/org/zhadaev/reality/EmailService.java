package org.zhadaev.reality;

import com.sun.mail.imap.IMAPFolder;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import jakarta.mail.*;

import java.io.IOException;
import java.util.Properties;

@Singleton
public class EmailService {

    private final Properties properties = new Properties();

    private final EmailProperties emailProperties;
    private final TelegramSender telegramSender;

    public EmailService(EmailProperties emailProperties, TelegramSender telegramSender) {
        this.emailProperties = emailProperties;
        this.telegramSender = telegramSender;
    }

    @PostConstruct
    void setup() {
        properties.put("mail.imap.host", emailProperties.host());
        properties.put("mail.imap.port", emailProperties.port());
        properties.put("mail.imaps.ssl.trust", "*");
        properties.put("mail.imaps.partialfetch", "false");
    }

    public void checkEmail() {
        Session emailSession = Session.getDefaultInstance(properties);
        try (Store store = emailSession.getStore("imaps")) {
            store.connect(emailProperties.host(), emailProperties.login(), emailProperties.password());

            IMAPFolder inboxFolder = (IMAPFolder) store.getFolder("INBOX");
            processMessages(inboxFolder);

            for (Folder inboxSubfolder: inboxFolder.list()) {
                processMessages((IMAPFolder) inboxSubfolder);
            }

        } catch (Exception e) {
            System.out.println("Ошибка при обработке писем: " + e.getMessage());
        }
    }

    private void processMessages(IMAPFolder inboxFolder) throws MessagingException, IOException {
        inboxFolder.open(Folder.READ_WRITE);

        if (inboxFolder.getMessageCount() == 0) {
            inboxFolder.close();
            return;
        }

        for (Message message : inboxFolder.getMessages()) {
            if (!message.getFrom()[0].toString().contains(emailProperties.login())) {
                continue;
            }
            BodyPart photo = getPhotoFromMessage(message);
            if (photo != null) {
                telegramSender.sendPhoto(photo.getFileName(), photo.getInputStream(), photo.getSize());
                message.setFlag(Flags.Flag.SEEN, true);
                inboxFolder.moveMessages(new Message[]{message}, inboxFolder.getStore().getFolder("Детекция движения"));
                break;
            } else {
                inboxFolder.moveMessages(new Message[]{message}, inboxFolder.getStore().getFolder("Корзина"));
            }
        }

        inboxFolder.close();
    }

    private BodyPart getPhotoFromMessage(Message message) throws MessagingException, IOException {
        Multipart content = (Multipart) message.getContent();
        for (int i = 0; i < content.getCount(); i++) {
            BodyPart bodyPart = content.getBodyPart(i);
            if (bodyPart.isMimeType("image/jpeg") || bodyPart.isMimeType("image/jpg")) {
                return bodyPart;
            }
        }
        return null;
    }

    public void removeUnnecessary() {
        Session emailSession = Session.getDefaultInstance(properties);
        try (Store store = emailSession.getStore("imaps")) {
            store.connect(emailProperties.host(), emailProperties.login(), emailProperties.password());

            IMAPFolder motionDetectionFolder = (IMAPFolder) store.getFolder("Детекция движения");

            motionDetectionFolder.open(Folder.READ_WRITE);

            int unnecessaryCount = motionDetectionFolder.getMessageCount() - emailProperties.necessaryCount();
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
