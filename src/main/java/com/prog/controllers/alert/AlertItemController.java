package com.prog.controllers.alert;

import com.prog.models.AlertManagement.AlertItem;
import com.prog.utils.Colors;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class AlertItemController {

    @FXML private HBox root;
    @FXML private Label title, message;
    @FXML private FontIcon icon;

    private AlertItem item;

    public void init(AlertItem item)
    {
        this.item = item;
        title.textProperty().bind(item.titleProperty());
        message.textProperty().bind(item.messageProperty());
        root.styleProperty().bind(
                Bindings.createStringBinding(() -> switch (item.typeProperty().get()) {
                            case ERROR -> GetBackgroundColor(Colors.RED);
                            case SUCCESS -> GetBackgroundColor(Colors.GREEN);
                            case WARN -> GetBackgroundColor(Colors.YELLOW);
                            case INFO -> GetBackgroundColor(Colors.BLUE);
                            case WARMUP -> "-fx-opacity : 0;";
                        },
                        item.typeProperty()
                ));
       icon.iconCodeProperty().bind(item.iconProperty());
    }

    private String GetBackgroundColor(Colors color)
    {
        return "-fx-background-color:"+color.getValue()+";";
    }
}
