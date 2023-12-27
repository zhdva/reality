package org.zhadaev.reality;

import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import javax.annotation.PostConstruct;
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
    }

    public synchronized void checkEmail() {
        Session emailSession = Session.getDefaultInstance(properties);
        try (Store store = emailSession.getStore("imaps")) {
            store.connect(emailProperties.getHost(), emailProperties.getLogin(), emailProperties.getPassword());

            IMAPFolder inboxFolder = (IMAPFolder) store.getFolder("INBOX");

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
                    inboxFolder.moveMessages(new Message[]{message}, store.getFolder("Детекция движения"));
                } else {
                    inboxFolder.moveMessages(new Message[]{message}, store.getFolder("Корзина"));
                }
            }

            inboxFolder.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InputStream getPhotoFromMessage(Message message) throws MessagingException, IOException {
        message.setFlag(Flags.Flag.SEEN, true);
        Multipart content = (Multipart) message.getContent();
        for (int i = 0; i < content.getCount(); i++) {
            BodyPart bodyPart = content.getBodyPart(i);
            if (bodyPart.isMimeType(MimeTypeUtils.IMAGE_JPEG_VALUE)) {
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
            e.printStackTrace();
        }
    }

}
