package com.tambapps.p2p.fandem.desktop;

import com.tambapps.p2p.fandem.desktop.controller.AppController;
import com.tambapps.p2p.fandem.desktop.controller.ReceivePaneController;
import com.tambapps.p2p.fandem.desktop.controller.SendPaneController;
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
import javafx.util.Pair;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * JavaFX App
 */
public class App extends Application {

  public static final int MAX_SHARING_TASKS = 4;
  public static final ObservableList<SharingTask> sharingTasks =
      FXCollections.observableArrayList();
  public static FileSharingService sharingService;
  private static Stage stage;

  public static <N extends Region, C> Pair<N, C> load(String name) throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource(name + ".fxml"));
    N node = loader.load();
    node.setBackground(Background.EMPTY);
    return new Pair<>(node, loader.getController());
  }

  public static <N extends Region, C> Pair<N, C> loadQuietly(String name) {
    try {
      return load(name);
    } catch (IOException e) {
      throw new RuntimeException("Couldn't load " + name, e);
    }
  }

  public static void main(String[] args) {
    ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    sharingService = new FileSharingService(executor);
    launch();
    executor.shutdownNow();
  }

  public static Stage getStage() {
    return stage;
  }

  @Override
  public void start(Stage stage) throws IOException {
    App.stage = stage;
    // stage.getIcons().add(new Image(App.class.getResourceAsStream("icon.png")));

    stage.setTitle("Fandem: P2P File Sharing");

    Pair<VBox, AppController> appPair = load("app");
    VBox vBox = appPair.getKey();
    vBox.setStyle(String.format("-fx-background-color: linear-gradient(to top, %s, %s)",
        Colors.GRADIENT_BOTTOM, Colors.GRADIENT_TOP));

    Pair<Region, SendPaneController> sendPair = load("sendPane");
    Pair<Region, ReceivePaneController> receivePair = load("receivePane");
    configureControllers(appPair.getValue(), sendPair.getValue(), receivePair.getValue());

    HBox panesContainer = (HBox) vBox.getChildren().get(0);
    panesContainer.getChildren().addAll(sendPair.getKey(), receivePair.getKey());

    Scene scene = new Scene(vBox);
    stage.setScene(scene);
    stage.show();
  }

  private void configureControllers(AppController appController,
      SendPaneController sendController, ReceivePaneController receiveController) {
    Consumer<Region> taskViewConsumer = appController::addTaskView;
    sendController.setTaskViewConsumer(taskViewConsumer);
    receiveController.setTaskViewConsumer(taskViewConsumer);
  }
}