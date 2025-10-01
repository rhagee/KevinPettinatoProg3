package com.client.controllers.alert;

import com.client.models.AlertManagement.AlertItem;
import com.client.models.AlertManagement.AlertType;
import com.client.utils.Colors;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.List;

public class AlertItemController {

    @FXML
    private HBox root;
    @FXML
    private Label title, message;
    @FXML
    private FontIcon icon;
    @FXML
    private ProgressIndicator loader;

    private AlertItem item;

    public void init(AlertItem item) {
        this.item = item;
        title.textProperty().bind(item.titleProperty());
        message.textProperty().bind(item.messageProperty());
        item.typeProperty().addListener(OnTypeChange);
        SetupUI(item.typeProperty().get());
    }

    private final ChangeListener<AlertType> OnTypeChange = (_, oldValue, newValue) ->
    {
        SetupUI(newValue);
    };

    private void SetupUI(AlertType type) {
        //Setto Colore Root
        root.setStyle(getRootStyle(type));
        List<Node> children = root.getChildren();
        children.remove(icon);
        children.remove(loader);
        if (type == AlertType.LOADING) {
            children.addFirst(loader);
        } else {
            children.addFirst(icon);
            icon.setIconCode(getIconCode(type));
        }
    }

    private String getRootStyle(AlertType type) {
        return switch (type) {
            case ERROR -> GetBackgroundColor(Colors.RED);
            case SUCCESS -> GetBackgroundColor(Colors.GREEN);
            case WARN -> GetBackgroundColor(Colors.YELLOW);
            case INFO -> GetBackgroundColor(Colors.BLUE);
            case LOADING -> GetBackgroundColor(Colors.WHITE);
            case WARMUP -> "-fx-opacity : 0;";
        };
    }

    private Ikon getIconCode(AlertType type) {
        return switch (type) {
            case ERROR -> MaterialDesign.MDI_CLOSE_CIRCLE;
            case SUCCESS -> MaterialDesign.MDI_CHECK_CIRCLE;
            case WARN -> MaterialDesign.MDI_ALERT;
            case INFO -> MaterialDesign.MDI_INFORMATION;
            case WARMUP -> MaterialDesign.MDI_BORDER_NONE;
            case LOADING -> MaterialDesign.MDI_BORDER_NONE;
        };
    }


    private String GetBackgroundColor(Colors color) {
        return "-fx-background-color:" + color.getValue() + ";";
    }
}
