package com.prog.controllers.alert;

import com.prog.models.AlertManagement.AlertItem;
import com.prog.models.AlertManagement.AlertManager;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class AlertListController {
    @FXML
    private VBox alertListContainer;

    private HashMap<String, Node> idToNode = new HashMap<>();

    @FXML
    public void initialize()
    {
        AlertManager.get().getItems().addListener(OnListChange);
    }

    private final ListChangeListener<AlertItem> OnListChange = changes ->
    {
        Platform.runLater(() -> {
            while(changes.next())
            {
               if(changes.wasAdded())
               {
                   IterateChanges(changes.getAddedSubList(),this::OnAdd);
               }

               if(changes.wasRemoved())
               {
                   IterateChanges(changes.getRemoved(),this::OnDelete);
                   return;
               }

               if(changes.wasPermutated())
               {
                   OnPermutated(changes.getList());
               }
            }
        });

    };

    private void IterateChanges(List<? extends AlertItem> items, Consumer<AlertItem> action)
    {
        for(int i=0;i<items.size();i++)
        {
            action.accept(items.get(i));
        }
    }

    private void OnAdd(AlertItem item)
    {
        Node generated = GenerateNode(item);
        alertListContainer.getChildren().add(generated);
        idToNode.put(item.getId(),generated);
    }

    private void OnDelete(AlertItem item)
    {
        Node toDelete = idToNode.get(item.getId());

        if(toDelete == null)
        {
            return;
        }

        alertListContainer.getChildren().remove(toDelete);
        idToNode.remove(item.getId());
    }

    private void OnPermutated(List<? extends AlertItem> items)
    {
        var permutated = items.stream().map(item -> idToNode.get(item.getId())).toList();
        alertListContainer.getChildren().setAll(permutated);
    }

    private Node GenerateNode(AlertItem item)
    {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/prog/ui/alert/alert_item.fxml"));
            Node generated = fxml.load();
            AlertItemController controller = fxml.getController();
            controller.init(item);
            return generated;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

