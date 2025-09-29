package com.prog.models.AlertManagement;

import com.prog.models.AlertManagement.AlertType;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.Icon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

public class AlertItem {
    private final String id;

    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty message = new SimpleStringProperty("");
    private final ObjectProperty<AlertType> type = new SimpleObjectProperty<>(AlertType.INFO);
    private final ObjectProperty<Ikon> icon = new SimpleObjectProperty<>(null);
    private final PauseTransition timer;

    public AlertItem(String id, String title, String message, AlertType type, PauseTransition timer) {
        this.id = id;
        this.title.set(title);
        this.message.set(message);
        this.type.set(type);
        this.icon.setValue(getIconCode(type));
        this.timer = timer;
    }

    private Ikon getIconCode(AlertType type)
    {
        return switch(type)
        {
            case ERROR -> MaterialDesign.MDI_CLOSE_CIRCLE;
            case SUCCESS -> MaterialDesign.MDI_CHECK_CIRCLE;
            case WARN -> MaterialDesign.MDI_ALERT;
            case INFO -> MaterialDesign.MDI_INFORMATION;
        };
    }

    public String getId() { return id; }

    public StringProperty titleProperty() { return title; }
    public StringProperty messageProperty() { return message; }
    public ObjectProperty<AlertType> typeProperty() { return type; }

    public PauseTransition getTimer() { return timer; }

    public ObjectProperty<Ikon> iconProperty() {return icon;}

}
