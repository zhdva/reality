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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class EmailService {

    @Value("${email.host}")
    private String host;

    @Value("${email.port}")
    private String port;

    @Value("${email.login}")
    private String login;

    @Value("${email.password}")
    private String password;

    private Store store;

    private TelegramBot telegramBot;

    public EmailService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    void setup() throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);
        properties.put("mail.imaps.ssl.trust", "*");
        Session emailSession = Session.getDefaultInstance(properties);
        store = emailSession.getStore("imaps");
        store.connect(host, login, password);
    }

    public void checkEmail() {
        try {
            IMAPFolder toMyselfFolder = (IMAPFolder) store.getFolder("INBOX/ToMyself");

            if (toMyselfFolder.isOpen()) {
                return;
            }

            toMyselfFolder.open(Folder.READ_WRITE);

            if (toMyselfFolder.getMessageCount() == 0) {
                toMyselfFolder.close();
                return;
            }

            List<Message> messagesWithPhoto = new ArrayList<>();
            List<Message> messagesWithoutPhoto = new ArrayList<>();
            for (Message message : toMyselfFolder.getMessages()) {
                InputStream photo = getPhotoFromMessage(message);
                if (photo != null) {
                    messagesWithPhoto.add(message);
                    telegramBot.sendPhoto(photo);
                } else {
                    messagesWithoutPhoto.add(message);
                }
            }

            toMyselfFolder.moveMessages(messagesWithPhoto.toArray(new Message[]{}), store.getFolder("Детекция движения"));
            toMyselfFolder.moveMessages(messagesWithoutPhoto.toArray(new Message[]{}), store.getFolder("Корзина"));

            toMyselfFolder.close();

        } catch (IOException | TelegramApiException | MessagingException e) {
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

}
