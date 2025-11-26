package com.client.models.AlertManagement;

import com.client.models.ProgApplication;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class AlertManager {
    private final double DEFAULT_ALERT_TIME = 5;
    private final Pane root;
    private static AlertManager INSTANCE;
    private final ObservableList<AlertItem> items = FXCollections.observableArrayList();

    private Parent failedModal;
    private Parent retryModal;

    private HashMap<String, AlertItem> itemsMap = new HashMap<>();

    public ObservableList<AlertItem> getItems() {
        return items;
    }

    public static AlertManager get() {
        if (INSTANCE == null) throw new IllegalStateException("AlertManager not initialized");
        return INSTANCE;
    }

    private AlertManager(Pane root) {
        this.root = root;
    }

    public static AlertManager init(Pane root) {
        if (INSTANCE != null) throw new IllegalStateException("SceneManager already initialized");
        INSTANCE = new AlertManager(root);
        return INSTANCE;
    }

    public void build() {
        try {
            FXMLLoader loader = new FXMLLoader(ProgApplication.class.getResource("/com/prog/ui/alert/alert_list.fxml"));
            Parent listRoot = loader.load();
            root.getChildren().add(listRoot);

            WarmupAlertItem();

            FXMLLoader overlayLoader = new FXMLLoader(ProgApplication.class.getResource("/com/prog/ui/alert/connection_state_overlay.fxml"));
            Parent overlayRoot = overlayLoader.load();
            root.getChildren().add(overlayRoot);

        } catch (IOException e) {
            System.err.println("Errore durante il caricamento dell'alertList");
            e.printStackTrace();
        }
    }


    public void OnConnectionFailed() {
        Platform.runLater(() -> {
            CloseRetryModal();
            LoadFailedModal();
        });

    }

    public void OnConnectionDropped() {
        Platform.runLater(() -> {
            CloseFailedModal();
            LoadRetryModal();
        });
    }

    public void OnConnectionSuccessful() {
        Platform.runLater(() -> {
            CloseFailedModal();
            CloseRetryModal();
        });
    }

    private void LoadFailedModal() {
        if (failedModal != null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(ProgApplication.class.getResource("/com/prog/ui/alert/failed_modal.fxml"));
            failedModal = loader.load();
            root.getChildren().add(failedModal);
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento del fail modal");
            e.printStackTrace();
        }
    }

    private void CloseFailedModal() {
        root.getChildren().remove(failedModal);
        failedModal = null;
    }


    private void LoadRetryModal() {
        if (retryModal != null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(ProgApplication.class.getResource("/com/prog/ui/alert/connection_state_overlay.fxml"));
            retryModal = loader.load();
            root.getChildren().add(retryModal);
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento del fail modal");
            e.printStackTrace();
        }
    }

    private void CloseRetryModal() {
        root.getChildren().remove(retryModal);
        retryModal = null;
    }

    private void WarmupAlertItem() {
        Platform.runLater(() -> {
            String id = add("", "", AlertType.WARMUP);
            Platform.runLater(() -> {
                forceRemove(id);
            });
        });
    }

    public String add(String title, String msg, AlertType type) {
        return add(title, msg, type, DEFAULT_ALERT_TIME);
    }

    public String add(String title, String msg, AlertType type, double time) {
        String id = UUID.randomUUID().toString();

        PauseTransition timer;
        if (type != AlertType.LOADING) {
            timer = getPauseTransition(id, time);
        } else {
            timer = null;
        }

        AlertItem item = new AlertItem(id, title, msg, type, timer);

        Platform.runLater(() -> {
            items.add(item);
            itemsMap.put(id, item);
            item.StartTimer();
        });
        return id;
    }

    private PauseTransition getPauseTransition(String alertId, double seconds) {
        PauseTransition delay = new PauseTransition(Duration.seconds(seconds));
        delay.setOnFinished(event -> remove(alertId));
        return delay;
    }

    private void restartDismiss(AlertItem item) {
        Platform.runLater(item::RestartTimer);
    }


    public void forceRemove(String id) {
        AlertItem item = itemsMap.get(id);
        item.StopTimer();

        remove(id);
    }

    public void remove(String id) {
        AlertItem item = itemsMap.remove(id);
        if (item == null) {
            return;
        }
        Platform.runLater(() -> {
            items.remove(item);
        });
    }

    public void update(String id, Consumer<AlertItem> mutator) {
        AlertItem item = itemsMap.get(id);
        if (item == null) {
            return;
        }

        Platform.runLater(() -> {
            mutator.accept(item);
            restartDismiss(item);
        });
    }

    public void SetType(String id, AlertType type) {
        update(id, item -> item.typeProperty().setValue(type));
    }

    public void setTitle(String id, String text) {
        update(id, item -> item.titleProperty().setValue(text));
    }

    public void SetMessage(String id, String text) {
        update(id, item -> item.messageProperty().setValue(text));
    }

    public void UpdateItem(String id, String title, String message, AlertType type) {
        UpdateItem(id, title, message, type, DEFAULT_ALERT_TIME);
    }


    public void UpdateItem(String id, String title, String message, AlertType type, double time) {
        update(id, item -> {
            item.titleProperty().setValue(title);
            item.messageProperty().setValue(message);
            item.typeProperty().setValue(type);
            if (type != AlertType.LOADING) {
                PauseTransition timer = getPauseTransition(id, time);
                item.setTimer(timer);
            }
        });
    }

}
