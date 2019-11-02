package com.tambapps.p2p.fandem.desktop;

import com.tambapps.p2p.fandem.desktop.controller.SendPaneController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("sendPane.fxml"));

        Scene scene = new Scene(loader.load());
        SendPaneController controller = loader.getController();
        controller.setStage(stage);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}