package com.client.models.SceneManagement;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

//A class to make actual smooth transitions (+ NoTransition for instant change)
//This might be the only entry point to access the SceneManager and change showed page
public class SceneTransitions {
    private static final int ANIM_DURATION = 200;


    private enum HorizontalDirection {
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

    public static void NoTransition(SceneNames sceneName) {
        Parent newPage = Scenes.GetRoot(sceneName);
        if (newPage == null) {
            System.err.println("Can't slide root due to wrong or not found scene name");
            return;
        }

        NoTransition(newPage);
        SceneManager.get().SetWindowTitle(Scenes.GetTitle(sceneName));
    }

    public static void NoTransition(Parent newPage) {
        if (newPage == null) {
            System.err.println("Error Trying sliding scene , scene or root value is null");
            return;
        }

        Parent oldPage = SceneManager.get().ActivePage;
        Pane root = SceneManager.get().getMain();

        if (oldPage != null) {
            root.getChildren().remove(oldPage);
        }

        root.getChildren().add(newPage);
        SceneManager.get().ActivePage = newPage;
    }

    public static void SlideLeft(SceneNames sceneName) {
        Parent newPage = Scenes.GetRoot(sceneName);
        if (newPage == null) {
            System.err.println("Can't slide root due to wrong or not found scene name");
            return;
        }

        SlideLeft(newPage);
        SceneManager.get().SetWindowTitle(Scenes.GetTitle(sceneName));
    }


    //Wrapper to call the slide horizontal with the direction
    public static void SlideLeft(Parent newPage) {
        SlideHorizontal(newPage, HorizontalDirection.LEFT);
    }


    public static void SlideRight(SceneNames sceneName) {
        Parent newPage = Scenes.GetRoot(sceneName);
        if (newPage == null) {
            System.err.println("Can't slide root due to wrong or not found scene name");
            return;
        }

        SlideRight(newPage);
        SceneManager.get().SetWindowTitle(Scenes.GetTitle(sceneName));
    }

    //Wrapper to call the slide horizontal with the direction
    public static void SlideRight(Parent newPage) {
        SlideHorizontal(newPage, HorizontalDirection.RIGHT);
    }

    private static void SlideHorizontal(Parent newPage, HorizontalDirection dir) {
        //Get oldPage and root of the scene manager (where all pages will be attached after being there)
        Parent oldPage = SceneManager.get().ActivePage;
        Pane root = SceneManager.get().getMain();

        //Get width of the root container
        double width = root.getWidth();

        //Set the translateX property at width multiplied for the opposite of the direction we want it to slide
        //Right Slide -> Page starts from -width (left)
        //Left Slide -> Page starts from +width (right)
        newPage.translateXProperty().set(width * -dir.getValue());

        //Create a container for the swap
        StackPane container = new StackPane(oldPage, newPage);

        //Swap children on the root so we can make the transition happen in the container (that will have both pages)
        root.getChildren().remove(oldPage);
        root.getChildren().add(container);

        //Old slide will go out on width * actual direction (1,-1) so if dir is right it will go at +width , while left -width
        TranslateTransition oldSlide = GetHorizontalTranslate(oldPage, width * dir.getValue());
        //New slide will slide in at x value 0
        TranslateTransition newSlide = GetHorizontalTranslate(newPage, 0);


        //And here we declare a parallel transition so both TranslateTransition happens at the same time
        ParallelTransition pt = new ParallelTransition(oldSlide, newSlide);

        //When transition finish we have a callback
        //Here we remove the temporary container, add the newPage as final children and set it active page in the manager
        pt.setOnFinished(e -> {
            root.getChildren().remove(container);
            root.getChildren().add(newPage);
            SceneManager.get().ActivePage = newPage;
        });

        //Play parallelTransition to make it animate + trigger callback when finishes
        pt.play();
    }

    //Define a TranslateTransition that will happen for ANIM_DURATION on the Node or Parent element
    //Here we define what is the final X using setToX and also the Interpolation function (EASE_BOTH seems the most appropriate to me)
    //And then return the TranslateTransition object to the caller
    private static TranslateTransition GetHorizontalTranslate(Parent element, double xValue) {
        var slide = new TranslateTransition(Duration.millis(ANIM_DURATION), element);
        slide.setToX(xValue);
        slide.setInterpolator(Interpolator.EASE_BOTH);
        return slide;
    }

}