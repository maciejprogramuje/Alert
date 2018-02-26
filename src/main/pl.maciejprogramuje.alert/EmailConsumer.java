import javafx.beans.property.SimpleStringProperty;

public class EmailConsumer {
    SimpleStringProperty email;

    public EmailConsumer(String em) {
        this.email = new SimpleStringProperty(em);
    }

    public String getEmail() {
        return email.get();
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }
}
