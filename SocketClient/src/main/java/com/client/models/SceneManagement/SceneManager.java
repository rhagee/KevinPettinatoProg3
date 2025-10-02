package com.client.models.SceneManagement;

import com.client.models.AlertManagement.AlertManager;
import com.client.models.BackendManagement.BackendManager;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/*
UI STRUCTURE

     STAGE-----+
               |
               ROOT-----+
                        |
                        MAIN ------------+
                        |                |
                        |                Actual showed page
                        |
                        ABSOLUTE --------+
                                         |
                                         Alerts
                                         |
                                         Modals
*/


/*
 Considerations :

 I think this can be improved by keeping "old scenes" referenced in a dictionary and just re-call an Initialize method on their controllers
 that can extends an actual abstract class (so we call Initialize on the Abstract).
 This would improve performance since we don't ALWAYS re-load the DOM elements from file but keep them.

 This should be done considering Ram usage since a lot of mapped pages might end up using too much Ram space while not-used.
 Might work as an LRU Cache with fixed size to optimize navigation time.

 Decision : Will not implement any of that since might be out of the project's scope but might be interesting.
            Definitely something I would investigate for scalability on a big software.
 */

//A Singleton class that will handle the "root" level navigation and keep track of elements in the main UI setup.
public class SceneManager {
    private static final String AppName = "SocketMailer";

    private final Stage stage;
    private Scene scene;
    private Pane root;
    private Pane main;
    private Pane absolute;
    public Parent ActivePage;

    private static SceneManager INSTANCE;

    public static SceneManager get() {
        if (INSTANCE == null) throw new IllegalStateException("SceneManager not initialized");
        return INSTANCE;
    }

    private SceneManager(Stage stage) {
        this.stage = stage;
    }

    public static SceneManager init(Stage stage) {
        if (INSTANCE != null) throw new IllegalStateException("SceneManager already initialized");
        if (stage == null) throw new IllegalArgumentException("stage is null");
        INSTANCE = new SceneManager(stage);
        return INSTANCE;
    }

    public void start() {
        runFx(() -> {
            scene = LoadEntryScene();
            SceneTransitions.NoTransition(SceneNames.LOGIN);
            SetWindowTitle("Login");
            stage.setScene(scene);
            stage.show();

            //Start it after UI is ready to give feedbacks
            StartBackendThread();
        });
    }

    private Scene LoadEntryScene() {
        root = new StackPane();
        main = new StackPane();

        absolute = new StackPane();
        absolute.setPickOnBounds(false);

        AlertManager.init(absolute).build();

        root.getChildren().addAll(main, absolute);
        return new Scene(root, 1024, 768);
    }

    private void StartBackendThread() {
        new Thread(BackendManager.INSTANCE::CreateConnection).start();
    }

    public Scene getCurrentScene() {
        return scene;
    }

    public void SetWindowTitle(String title) {
        if (title == null || title.isBlank()) {
            stage.setTitle(AppName);
            return;
        }
        stage.setTitle(AppName + " - " + title);
    }

    private static void runFx(Runnable r) {
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }

    public Pane getMain() {
        return main;
    }

    public Pane getAbsolute() {
        return absolute;
    }
}
