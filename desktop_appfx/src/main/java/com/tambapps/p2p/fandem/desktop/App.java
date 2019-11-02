package com.tambapps.p2p.fandem.desktop;

import com.tambapps.p2p.fandem.desktop.controller.SendPaneController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("app.fxml"));
        VBox vBox = loader.load();
        HBox panesContainer = (HBox) vBox.getChildren().get(0);
        panesContainer.getChildren().add(loadSendPane(stage));
        Scene scene = new Scene(vBox);

     //   SendPaneController controller = loader.getController();
       // controller.setStage(stage);
        stage.setScene(scene);
        stage.show();
    }

    private Pane loadSendPane(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("sendPane.fxml"));
        Pane pane = loader.load();
        SendPaneController controller = loader.getController();
        controller.setStage(stage);
        return pane;
    }
    public static void main(String[] args) {
        launch();
    }

}