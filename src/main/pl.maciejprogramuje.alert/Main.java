import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    // ****************************************************************************
    // *** IF *********************************************************************
    // *** sun.security.validator.ValidatorException: PKIX path building failed ***
    // *** TURN OFF ANTIVIRUS SOFTWARE ********************************************
    // ****************************************************************************
    // ****************************************************************************

    //Set mail properties and configure accordingly
    public static final String USERNAME_ZIMBRA = "";
    public static final String PASSWORD_ZIMBRA = "";
    public static final String ALERT_TITLE = "Alert SALESmanago: Wizyta - sklep.pb.pl m.szymczyk@pb.pl";
    public static final String FOLDER_NAME = "SalesManago ALERT";

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("/fxml/mainPane.fxml"));
        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sales Alert");
        //preloaderStage.getIcons().add(new Image("images/gazele.png"));
        primaryStage.show();
    }
}
