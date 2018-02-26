import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;


public class StaticUtils {
    static Message[] getMessagesReadWrite(SimpleIntegerProperty allMailsNum) throws MessagingException {
        Store store = getMailStore();
        Folder folder = getFolder(store);
        folder.open(Folder.READ_WRITE);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    allMailsNum.setValue(folder.getMessageCount());
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        });

        return folder.getMessages();
    }

    private static Folder getFolder(Store store) throws MessagingException {
        store.connect("poczta.pb.pl", Main.USERNAME_ZIMBRA, Main.PASSWORD_ZIMBRA);
        return store.getFolder("INBOX/" + Main.FOLDER_NAME);
    }

    private static Store getMailStore() throws NoSuchProviderException {
        Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", "imaps");
        properties.setProperty("mail.imaps.host", "poczta.pb.pl");
        properties.setProperty("mail.imaps.port", "993");
        properties.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imaps.socketFactory.fallback", "false");
        Session session = Session.getInstance(properties);

        return session.getStore("imaps");
    }

    static boolean isNewScoringHigherThanOldScoring(User newUser, User oldUser) {
        if (newUser.getScoring() > oldUser.getScoring()) {
            return true;
        }
        return false;
    }

    static int isNotEmailInBase(User user, ArrayList<User> users) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equals(user.getEmail())) {
                return i;
            }
        }
        return -1;
    }

    static User getUserFromContent(String content) {
        String name = content.substring(content.indexOf("Nazwa:") + 7, content.indexOf("E-mail:")).replace("\n", "").replace("\r", "");
        String email = content.substring(content.indexOf("E-mail:") + 8, content.indexOf("Telefon:")).replace("\n", "").replace("\r", "");
        String phone = content.substring(content.indexOf("Telefon:") + 9, content.indexOf("Przejdź")).replace("\n", "").replace("\r", "");
        int scoring = Integer.valueOf(content.substring(content.indexOf("Scoring:") + 9, content.indexOf("Opis:")).replace("\n", "").replace("\r", "").replaceAll("\\p{Z}", ""));
        String details = content.substring(content.indexOf("Wizyty kontaktu:"), content.indexOf("Zaloguj się na swoje konto"));

        return new User(name, email, phone, scoring, details);
    }

    static String formatContent(Message tempMessage) throws MessagingException, IOException {
        String result = "";
        if (tempMessage.isMimeType("text/plain")) {
            result = tempMessage.getContent().toString();
        } else if (tempMessage.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) tempMessage.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }

    public static void sendEmail(String mailTo, String messageText) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "poczta.pb.pl");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(Main.USERNAME_ZIMBRA, Main.PASSWORD_ZIMBRA);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("m.szymczyk@pb.pl"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
        message.setSubject("SM Alert");
        message.setText(messageText);

        Transport.send(message);

        //System.out.println("Done - sent to: " + mailTo);
    }

    public static ArrayList<String> readMailToList() throws IOException {
        ArrayList<String> mailToArr = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(Main.MAIL_TO_LIST_FILE_NAME));
        String line;
        while ((line = br.readLine()) != null) {
            mailToArr.add(line);
        }

        return mailToArr;
    }

    public static ObservableList<EmailConsumer> readFileMailToList() throws IOException {
        ObservableList<EmailConsumer> emailConsumers = FXCollections.observableArrayList();

        BufferedReader br = new BufferedReader(new FileReader(Main.MAIL_TO_LIST_FILE_NAME));
        String line;
        while ((line = br.readLine()) != null) {
            emailConsumers.add(new EmailConsumer(line));
        }

        return emailConsumers;
    }
}
