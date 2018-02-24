import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.ArrayList;

public class StaticUtils {
    static boolean isNewScoringHigherThanOldScoring(User newUser, User oldUser) {
        if(newUser.getScoring() > oldUser.getScoring()) {
            return true;
        }
        return false;
    }

    static int isNotEmailInBase(User user, ArrayList<User> users) {
        for (int i = 0; i < users.size(); i++) {
            if(users.get(i).getEmail().equals(user.getEmail())) {
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
}
