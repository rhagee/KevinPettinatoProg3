package com.prog.models.SceneManagement;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class SceneTransitions {
    private static final int ANIM_DURATION = 200;


    private enum HorizontalDirection
    {
        LEFT(-1),
        RIGHT(1);

        private final int value;

        HorizontalDirection(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    public static void NoTransition(SceneNames sceneName)
    {
        Parent newPage = Scenes.GetRoot(sceneName);
        if(newPage == null)
        {
            System.err.println("Can't slide root due to wrong or not found scene name");
            return;
        }

        NoTransition(newPage);
        SceneManager.get().SetWindowTitle(Scenes.GetTitle(sceneName));
    }

    public static void NoTransition(Parent newPage) {
        if(newPage == null)
        {
            System.err.println("Error Trying sliding scene , scene or root value is null");
            return;
        }

        Parent oldPage = SceneManager.get().ActivePage;
        Pane root = SceneManager.get().getMain();

        if(oldPage != null)
        {
            root.getChildren().remove(oldPage);
        }

        root.getChildren().add(newPage);
        SceneManager.get().ActivePage = newPage;
    }

    public static void SlideLeft(SceneNames sceneName)
    {
        Parent newPage = Scenes.GetRoot(sceneName);
        if(newPage == null)
        {
            System.err.println("Can't slide root due to wrong or not found scene name");
            return;
        }

        SlideLeft(newPage);
        SceneManager.get().SetWindowTitle(Scenes.GetTitle(sceneName));
    }

    public static void SlideLeft(Parent newPage) {
        SlideHorizontal(newPage,HorizontalDirection.LEFT);
    }

    public static void SlideRight(SceneNames sceneName)
    {
        Parent newPage = Scenes.GetRoot(sceneName);
        if(newPage == null)
        {
            System.err.println("Can't slide root due to wrong or not found scene name");
            return;
        }

        SlideRight(newPage);
        SceneManager.get().SetWindowTitle(Scenes.GetTitle(sceneName));
    }

    public static void SlideRight(Parent newPage) {
        SlideHorizontal(newPage,HorizontalDirection.RIGHT);
    }

    private static void SlideHorizontal(Parent newPage, HorizontalDirection dir)
    {
        Parent oldPage = SceneManager.get().ActivePage;
        Pane root = SceneManager.get().getMain();
        double width = root.getWidth();

        newPage.translateXProperty().set(-width * dir.getValue());

        StackPane container = new StackPane(oldPage, newPage);
        root.getChildren().remove(oldPage);
        root.getChildren().add(container);

        TranslateTransition oldSlide = GetHorizontalTranslate(oldPage, width * dir.getValue());
        TranslateTransition newSlide = GetHorizontalTranslate(newPage,0);


        ParallelTransition pt = new ParallelTransition(oldSlide, newSlide);
        pt.setOnFinished(e -> {
            root.getChildren().remove(container);
            root.getChildren().add(newPage);
            SceneManager.get().ActivePage = newPage;
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