package com.client.models.EmailManagement;

import com.client.models.AlertManagement.AlertType;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EmailItem {
    private final String id;

    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty message = new SimpleStringProperty("");
    private final ObjectProperty<AlertType> type = new SimpleObjectProperty<>(AlertType.INFO);
    private final PauseTransition timer;

    public EmailItem(String id, String title, String message, AlertType type, PauseTransition timer) {
        this.id = id;
        this.title.set(title);
        this.message.set(message);
        this.type.set(type);
        this.timer = timer;
    }

    public String getId() {
        return id;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty messageProperty() {
        return message;
    }

    public ObjectProperty<AlertType> typeProperty() {
        return type;
    }

    public PauseTransition getTimer() {
        return timer;
    }

}
