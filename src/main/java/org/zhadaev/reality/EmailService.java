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
            IMAPFolder inboxFolder = (IMAPFolder) store.getFolder("INBOX");

            if (inboxFolder.isOpen()) {
                return;
            }

            inboxFolder.open(Folder.READ_WRITE);

            IMAPFolder toMyselfFolder = (IMAPFolder) inboxFolder.getFolder("ToMyself");
            toMyselfFolder.open(Folder.READ_WRITE);

            if (toMyselfFolder.getMessageCount() == 0) {
                return;
            }

            for (Message message : toMyselfFolder.getMessages()) {
                handleMessage(message);
            }

            deleteAllMessagesInFolder(inboxFolder);

        } catch (IOException | TelegramApiException | MessagingException e) {
            e.printStackTrace();

        } finally {
            closeAllFolders();
        }
    }

    private void handleMessage(Message message) throws MessagingException, IOException, TelegramApiException {
        message.setFlag(Flags.Flag.SEEN, true);
        Multipart content = (Multipart) message.getContent();
        for (int i = 0; i < content.getCount(); i++) {
            BodyPart bodyPart = content.getBodyPart(i);
            if (bodyPart.isMimeType(MimeTypeUtils.IMAGE_JPEG_VALUE)) {
                telegramBot.sendPhoto(bodyPart.getInputStream());
                ((IMAPFolder) message.getFolder()).moveMessages(new Message[]{message}, store.getFolder("Детекция движения"));
            }
        }
    }

    private void deleteAllMessagesInFolder(IMAPFolder folder) throws MessagingException {
        Folder trashFolder = store.getFolder("Корзина");
        folder.moveMessages(folder.getMessages(), trashFolder);
        for (Folder innerFolder: folder.list()) {
            innerFolder.open(Folder.READ_WRITE);
            ((IMAPFolder) innerFolder).moveMessages(innerFolder.getMessages(), trashFolder);
            innerFolder.close();
        }
    }

    private void closeAllFolders() {
        try {
            for (Folder folder: store.getPersonalNamespaces()[0].list()) {
                if (folder.getName().equals("INBOX")) {
                    for (Folder innerFolder: folder.list()) {
                        if (innerFolder.isOpen()) {
                            innerFolder.close();
                        }
                    }
                }
                if (folder.isOpen()) {
                    folder.close();
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
