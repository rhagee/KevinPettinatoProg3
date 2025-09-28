package com.prog.models.SceneManagement;

import com.prog.models.AlertManagement.AlertManager;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class SceneManager {
    private static final String AppName = "SocketMailer";

    private final Stage stage;
    private Scene scene;
    private Pane root;
    private Pane main;
    private Pane absolute;
    public Parent ActivePage;

    private static SceneManager INSTANCE;

    public static SceneManager get()
    {
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
        });
    }

    private Scene LoadEntryScene()
    {
        root = new StackPane();
        main = new StackPane();

        absolute = new StackPane();
        absolute.setPickOnBounds(false);

        AlertManager.init(absolute).build();

        root.getChildren().addAll(main,absolute);
        return new Scene(root, 1024, 768);
    }

    public Scene getCurrentScene() {
        return scene;
    }

    public void SetWindowTitle(String title)
    {
        if(title == null || title.isBlank())
        {
            stage.setTitle(AppName);
            return;
        }
        stage.setTitle(AppName+" - "+title);
    }

    private static void runFx(Runnable r) {
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }

    public Pane getMain()
    {
        return main;
    }

    public Pane getAbsolute()
    {
        return absolute;
    }
}
