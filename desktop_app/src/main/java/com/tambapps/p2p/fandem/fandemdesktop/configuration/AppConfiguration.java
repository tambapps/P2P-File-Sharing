package com.tambapps.p2p.fandem.fandemdesktop.configuration;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.fandemdesktop.controller.AppController;
import com.tambapps.p2p.fandem.fandemdesktop.model.SharingTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Configuration
public class AppConfiguration {

  @Value("${sharing.tasks.max}")
  private int maxSharingTasks;

  @Bean
  public AtomicReference<Stage> stageReference() {
    return new AtomicReference<>();
  }

  @Bean
  ObservableList<SharingTask> sharingTasks() {
    return FXCollections.observableArrayList();
  }

  @Bean
  public Supplier<Boolean> canAddTaskSupplier(ObservableList<SharingTask> sharingTasks) {
    return () -> sharingTasks.size() < maxSharingTasks;
  }

  @Bean
  public BiConsumer<File, Peer> receiveTaskLauncher(AppController appController) {
    return appController::receiveTask;
  }

  @Bean
  public Consumer<File> sendTaskLauncher(AppController appController) {
    return appController::sendTask;
  }

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(maxSharingTasks);
  }

  @Bean
  public Supplier<File> directoryChooser(AtomicReference<Stage> stageReference) {
    return () -> new DirectoryChooser().showDialog(stageReference.get());
  }

  @Bean
  public Supplier<File> fileChooser(AtomicReference<Stage> stageReference) {
    return () -> new FileChooser().showOpenDialog(stageReference.get());
  }
}
