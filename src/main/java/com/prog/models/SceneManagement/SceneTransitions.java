package com.prog.models.SceneManagement;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class SceneTransitions {
    private static final int ANIM_DURATION = 200;

    public static void SlideLeft(SceneNames sceneName)
    {
        SlideLeft(SceneManager.get().getCurrentScene(),sceneName);
    }

    public static void SlideLeft(Scene scene , SceneNames sceneName)
    {
        Parent newRoot = Scenes.GetRoot(sceneName);
        if(newRoot == null)
        {
            System.err.println("Can't slide root due to wrong or not found scene name");
            return;
        }

        SlideLeft(scene,newRoot);
    }

    public static void SlideLeft(Scene scene, Parent newRoot) {
        if(scene == null || newRoot == null)
        {
            System.err.println("Error Trying sliding scene , scene or root value is null");
            return;
        }
        Parent oldRoot = scene.getRoot();
        double width = scene.getWidth();

        // Start newRoot off-screen (to the right)
        newRoot.translateXProperty().set(width);

        // Container holding both old and new
        StackPane container = new StackPane(oldRoot, newRoot);
        scene.setRoot(container);

        // Transition: old slides left, new slides in
        TranslateTransition oldSlide = GetHorizontalTranslate(oldRoot, -width);
        TranslateTransition newSlide = GetHorizontalTranslate(newRoot,0);


        ParallelTransition pt = new ParallelTransition(oldSlide, newSlide);
        pt.setOnFinished(e -> {
            // After animation, set newRoot as the real root
            container.getChildren().clear();
            scene.setRoot(newRoot);
        });

        pt.play();
    }

    public static void SlideRight(SceneNames sceneName)
    {
        SlideRight(SceneManager.get().getCurrentScene(),sceneName);
    }

    public static void SlideRight(Scene scene , SceneNames sceneName)
    {
        Parent newRoot = Scenes.GetRoot(sceneName);
        if(newRoot == null)
        {
            System.err.println("Can't slide root due to wrong or not found scene name");
            return;
        }

        SlideRight(scene,newRoot);
    }

    public static void SlideRight(Scene scene, Parent newRoot) {
        Parent oldRoot = scene.getRoot();
        double width = scene.getWidth();

        newRoot.translateXProperty().set(-width);

        StackPane container = new StackPane(oldRoot, newRoot);
        scene.setRoot(container);

        TranslateTransition oldSlide = GetHorizontalTranslate(oldRoot,width);
        TranslateTransition newSlide = GetHorizontalTranslate(newRoot,0);

        ParallelTransition pt = new ParallelTransition(oldSlide, newSlide);
        pt.setOnFinished(e -> {
            container.getChildren().clear();
            scene.setRoot(newRoot);
        });

        pt.play();
    }

    private static TranslateTransition GetHorizontalTranslate(Parent element, double xValue)
    {
        var slide = new TranslateTransition(Duration.millis(ANIM_DURATION), element);
        slide.setToX(xValue);
        slide.setInterpolator(Interpolator.EASE_BOTH);
        return slide;
    }

}