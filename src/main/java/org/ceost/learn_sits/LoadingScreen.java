package org.ceost.learn_sits;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import static javafx.application.Platform.exit;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;

/**
 * Example of displaying a splash page for a standalone JavaFX application
 */
public class LoadingScreen extends Application {

    //load resource for splash screen image
    File file = new File("res/processing.jpg");
    Image image = new Image(file.toURI().toString());

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private static final int SPLASH_WIDTH = 450;
    private static final int SPLASH_HEIGHT = 255;

    @Override
    public void init() {

        ImageView splash = new ImageView(image);
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH);
        progressText = new Label("Will find friends for peanuts . . .");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle(
                "-fx-padding: 1; "
                + "-fx-background-color: #E0E0E0; "
                + "-fx-border-width:0; "
                + "-fx-border-color: blue;"
        );
        splashLayout.setEffect(new DropShadow());
    }

    @Override
    public void start(final Stage initStage) throws Exception {
        final Task<ObservableList<String>> friendTask = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws InterruptedException {
                ObservableList<String> foundFriends
                        = FXCollections.<String>observableArrayList();
                ObservableList<String> availableFriends
                        = FXCollections.observableArrayList(
                                "Fili", "Kili", "Oin", "Gloin", "Thorin",
                                "Dwalin", "Balin", "Bifur", "Bofur",
                                "Bombur", "Dori", "Nori", "Ori"
                        );

                updateMessage("Processing . . . ");
                //Thread.sleep(1000);
                int size = availableFriends.size();
                for (int i = 0; i < size; i++) {
                    Thread.sleep(500);
                    updateProgress(i + 1, availableFriends.size());
                    String nextFriend = availableFriends.get(i);
                    foundFriends.add(nextFriend);
                    updateMessage("Processing . . . " + i + "/" + size);
                }
                Thread.sleep(5);
                updateMessage("Processing done!");
                Thread.sleep(500);
                return foundFriends;
            }
        };

        showSplash(
                initStage,
                friendTask,
                () -> exit()
        );

        new Thread(friendTask).start();
    }

    private void showSplash(
            final Stage initStage,
            Task<?> task,
            InitCompletionHandler initCompletionHandler
    ) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();

                initCompletionHandler.complete();
            } // todo add code to gracefully handle other task states.
        });

        Scene splashScene = new Scene(splashLayout);
        initStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.show();

    }

    public interface InitCompletionHandler {

        public void complete();
    }
}
