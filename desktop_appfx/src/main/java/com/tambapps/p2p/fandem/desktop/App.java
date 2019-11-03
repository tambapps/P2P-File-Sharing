package com.tambapps.p2p.fandem.desktop;

import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import com.tambapps.p2p.fandem.desktop.service.FileSharingService;
import com.tambapps.p2p.fandem.desktop.style.Colors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JavaFX App
 */
public class App extends Application {

    public static final int MAX_SHARING_TASKS = 4;
    public static FileSharingService sharingService;
    public static final ObservableList<SharingTask> sharingTasks = FXCollections.observableArrayList();
    private static Stage stage;

    @Override
    public void start(Stage stage) throws IOException {
        App.stage = stage;
       // stage.getIcons().add(new Image(App.class.getResourceAsStream("icon.png")));

        stage.setTitle("Fandem: P2P File Sharing");

        VBox vBox = (VBox) load("app");
        vBox.setStyle(String.format("-fx-background-color: linear-gradient(to top, %s, %s)",
          Colors.GRADIENT_BOTTOM, Colors.GRADIENT_TOP));

        HBox panesContainer = (HBox) vBox.getChildren().get(0);
        panesContainer.getChildren().addAll(load("sendPane"), load("receivePane"));

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.show();
    }

    public static Region load(String name) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(name + ".fxml"));
        Region node = loader.load();
        node.setBackground(Background.EMPTY);
        return node;
    }

    public static Region loadQuietly(String name) {
        try {
            return load(name);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get region", e);
        }
    }

        public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        sharingService = new FileSharingService(executor);
        launch();
        executor.shutdownNow();
    }

    public static Stage getStage() {
        return stage;
    }
}