package com.client.models.AlertManagement;

import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AlertItem {
    private final String id;

    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty message = new SimpleStringProperty("");
    private final ObjectProperty<AlertType> type = new SimpleObjectProperty<>(AlertType.INFO);
    private PauseTransition timer;

    public AlertItem(String id, String title, String message, AlertType type, PauseTransition timer) {
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

    public void setTimer(PauseTransition timer) {
        this.timer = timer;
    }

    public void StopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void StartTimer() {
        if (timer != null) {
            timer.play();
        }
    }

    public void RestartTimer() {
        StopTimer();
        StartTimer();
    }

}
