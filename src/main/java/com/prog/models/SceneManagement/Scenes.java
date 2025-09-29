package com.prog.models.SceneManagement;

import com.prog.models.ProgApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.util.HashMap;

public class Scenes {

    private static final String UI_ROOT_PATH = "/com/prog/ui/";

    private static final HashMap<SceneNames, String> scenes = new HashMap<>();

    static
    {
        scenes.put(SceneNames.LOGIN, FromRoot("pages/login.fxml"));
        scenes.put(SceneNames.HOME, FromRoot("pages/home.fxml"));
    }

    private static final HashMap<SceneNames, String> sceneTitles = new HashMap<>();

    static
    {
        sceneTitles.put(SceneNames.LOGIN, "Login");
        sceneTitles.put(SceneNames.HOME,  "Home");
    }

    private static String FromRoot(String path)
    {
        return UI_ROOT_PATH + path;
    }

    public static String GetPath(SceneNames name)
    {
        return scenes.get(name);
    }

    public static String GetTitle(SceneNames name) { return sceneTitles.get(name); }

    public static Parent GetRoot(SceneNames name)
    {
        var path = scenes.get(name);
        try
        {
            FXMLLoader loader = new FXMLLoader(ProgApplication.class.getResource(path));
            return loader.load();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace(); //For debugging
            return null;
        }
    }

}
