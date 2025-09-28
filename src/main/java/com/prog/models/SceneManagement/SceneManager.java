package com.prog.models.SceneManagement;

import com.prog.models.ProgApplication;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

//THIS IS A SINGLETON CLASS
public class SceneManager {
    private static final String AppName = "SocketMailer";

    private Scene scene;
    private final Stage stage;

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
            if (scene == null) throw new IllegalStateException("Failed to load entry scene");
            SetWindowTitle("Login");
            SetAndShow();
        });
    }

    private Scene LoadEntryScene ()
    {
        String path = Scenes.GetPath(SceneNames.LOGIN);
        URL url = ProgApplication.class.getResource(path);

        if(url == null)
        {
            System.err.println("Can't find URL for path "+path);
            return null;
        }

        try
        {
            FXMLLoader loader = new FXMLLoader(url);
            return new Scene(loader.load(), 1024, 768);
        }
        catch(IOException e)
        {
            System.err.println("IOException LoadEntryScene : Impossibile caricare la scena al path "+path+" , URL ottenuto : "+url);
            e.printStackTrace();
            return null;
        }
    }

    private void SetAndShow()
    {
        stage.setScene(scene);
        stage.show();
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
}
