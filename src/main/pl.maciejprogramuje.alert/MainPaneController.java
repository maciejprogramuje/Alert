import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Optional;

public class MainPaneController {
    public Button deleteButton;
    public Label allMailsNumLabel;
    public Label alertMailsNumLabel;

    private SimpleIntegerProperty allMailsNum = new SimpleIntegerProperty(0);
    private SimpleIntegerProperty alertMailsNum = new SimpleIntegerProperty(0);
    private ArrayList<User> users = new ArrayList<>();

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
                    Message[] messages = StaticUtils.getMessagesReadWrite(allMailsNum);

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

                    for (int i = 0; i < tempUsers.size(); i++) {
                        //System.out.println((i + 1) + ". tempUser: " + tempUsers.get(i).getName() + ", " + tempUsers.get(i).getScoring());

                        int index;

                        if (!tempUsers.get(i).getEmail().contains("@pb.pl")
                                && !tempUsers.get(i).getEmail().contains("@bankier.pl")
                                && !tempUsers.get(i).getEmail().contains("@pulsmedycyny.pl")) {
                            if ((index = StaticUtils.isNotEmailInBase(tempUsers.get(i), users)) == -1) {
                                users.add(tempUsers.get(i));

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        alertMailsNum.setValue(alertMailsNum.getValue() + 1);
                                    }
                                });

                            } else if(StaticUtils.isNewScoringHigherThanOldScoring(tempUsers.get(i), users.get(index))) {
                                users.get(index).setScoring(tempUsers.get(i).getScoring());
                            }
                        }
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

    public void deleteEmailsOnClick() throws MessagingException {
        System.out.println("click delete");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("UWAGA");
        alert.setHeaderText("Kasowanie e-maili");
        alert.setContentText("Czy na pewno chcesz skasować wszystkie e-maile w folderze " + Main.FOLDER_NAME + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            Message[] messages = StaticUtils.getMessagesReadWrite(allMailsNum);
            for (Message m: messages) {
                m.setFlag(Flags.Flag.DELETED, true);
            }
        } else {
            alert.close();
        }


    }

//TODO - przycisk delete all messages from folder
    //TODO - czy scoring, czy data maila?
}