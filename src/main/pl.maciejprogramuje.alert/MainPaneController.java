import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javax.mail.*;
import java.util.ArrayList;
import java.util.Properties;

public class MainPaneController {
    private SimpleIntegerProperty allMailsNum = new SimpleIntegerProperty(0);
    private SimpleIntegerProperty alertMailsNum = new SimpleIntegerProperty(0);
    private ArrayList<User> users = new ArrayList<>();

    public Label allMailsNumLabel;
    public Label alertMailsNumLabel;

    @FXML
    public void initialize() {
        System.out.println("Start Alert!");

        allMailsNumLabel.textProperty().bind(allMailsNum.asString());
        alertMailsNumLabel.textProperty().bind(alertMailsNum.asString());

        // Calling checkMailBox method to check received emails
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Properties properties = new Properties();
                    properties.setProperty("mail.store.protocol", "imaps");
                    properties.setProperty("mail.imaps.host", "poczta.pb.pl");
                    properties.setProperty("mail.imaps.port", "993");
                    properties.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    properties.setProperty("mail.imaps.socketFactory.fallback", "false");
                    Session session = Session.getInstance(properties);

                    Store store = session.getStore("imaps");
                    store.connect("poczta.pb.pl", Main.USERNAME_ZIMBRA, Main.PASSWORD_ZIMBRA);

                    Folder folder = store.getFolder("INBOX/" + Main.FOLDER_NAME);
                    folder.open(Folder.READ_ONLY);


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

                    // all messages
                    Message[] messages = folder.getMessages();

                    // only unread messages
                    //Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));


                    ArrayList<User> tempUsers = new ArrayList<>();
                    for (Message message : messages) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                allMailsNum.setValue(allMailsNum.getValue() - 1);
                            }
                        });

                        String content = "Content: " + StaticUtils.formatContent(message);
                        tempUsers.add(StaticUtils.getUserFromContent(content));
                    }

                    folder.close(false);
                    store.close();

                    for (int i = 0; i < tempUsers.size(); i++) {
                        //System.out.println((i + 1) + ". tempUser: " + tempUsers.get(i).getName() + ", " + tempUsers.get(i).getScoring());

                        int index;

                        if (!tempUsers.get(i).getEmail().contains("@pb.pl")
                                && !tempUsers.get(i).getEmail().contains("@bankier.pl")
                                && !tempUsers.get(i).getEmail().contains("@pulsmedycyny.pl")) {
                            if ((index = StaticUtils.isNotEmailInBase(tempUsers.get(i), users)) == -1) {
                                users.add(tempUsers.get(i));
                            } else if(StaticUtils.isNewScoringHigherThanOldScoring(tempUsers.get(i), users.get(index))) {
                                users.get(index).setScoring(tempUsers.get(i).getScoring());
                            }
                        }


                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                alertMailsNum.setValue(alertMailsNum.getValue() + 1);
                            }
                        });
                    }

                    System.out.println("==========================================");

                    for (int i = 0; i < users.size(); i++) {
                        System.out.println((i + 1) + ". user: " + users.get(i).getName() + ", mail: " + users.get(i).getEmail() + ", sc=" + users.get(i).getScoring());
                    }
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        }).start();
    }

//TODO - przycisk delete all messages from folder
}