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

    private Properties properties = new Properties();

    private TelegramBot telegramBot;

    public EmailService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    void setup() {
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);
        properties.put("mail.imaps.ssl.trust", "*");
    }

    public synchronized void checkEmail() {
        Session emailSession = Session.getDefaultInstance(properties);
        try (Store store = emailSession.getStore("imaps")) {
            store.connect(host, login, password);

            IMAPFolder inboxFolder = (IMAPFolder) store.getFolder("INBOX");

            inboxFolder.open(Folder.READ_WRITE);

            if (inboxFolder.getMessageCount() == 0) {
                inboxFolder.close();
                return;
            }

            for (Message message : inboxFolder.getMessages()) {
                if (!message.getFrom()[0].toString().contains(login)) {
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
