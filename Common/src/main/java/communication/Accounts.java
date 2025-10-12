package communication;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ListPropertyBase;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;

import java.io.Serializable;
import java.util.List;

public class Accounts implements Serializable {

    private ListProperty<String> mails = new SimpleListProperty<>(FXCollections.observableArrayList());

    public synchronized boolean authMail(String mail) {
        return mailExists(mail);
    }

    public boolean mailExists(String mail) {
        return mails.contains(mail);
    }

    public synchronized boolean addMail(String mail) {
        if (mailExists(mail)) {
            return false;
        }

        mails.add(mail);
        return true;
    }

    public void setMails(List<String> mails) {
        this.mails.addAll(mails);
    }

    public List<String> getMails() {
        return this.mails.getValue();
    }

    public void addListener(ChangeListener<List<String>> changeListener) {
        mails.addListener(changeListener);
    }

    public void removeListener(ChangeListener<List<String>> changeListener) {
        mails.removeListener(changeListener);
    }
}
