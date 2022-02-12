package com.tambapps.p2p.fandem.desktop.configuration;

import static com.tambapps.p2p.fandem.desktop.util.PrefKeys.RECEIVE_FOLDER;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.desktop.FandemDesktopApplication;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.fandem.desktop.controller.AppController;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import com.tambapps.p2p.speer.datagram.DatagramPeer;
import com.tambapps.p2p.speer.datagram.DatagramSupplier;
import com.tambapps.p2p.speer.datagram.service.MulticastReceiverService;
import com.tambapps.p2p.speer.datagram.service.PeriodicMulticastService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


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
  public MulticastReceiverService<List<SenderPeer>> senderPeersSupplier(
      @Qualifier("multicastExecutor") ExecutorService executorService) {
    return Fandem.senderPeersReceiverService(executorService, null);
  }

  @Bean
  public Consumer<List<File>> sendTaskLauncher(AppController appController) {
    return appController::sendTask;
  }

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(maxSharingTasks);
  }

  @Bean
  public ExecutorService multicastExecutor() {
    return Executors.newSingleThreadExecutor();
  }

  @Bean
  public Supplier<File> directoryChooser(AtomicReference<Stage> stageReference) {
    return () -> new DirectoryChooser().showDialog(stageReference.get());
  }

  @Bean
  public Supplier<List<File>> fileChooser(AtomicReference<Stage> stageReference) {
    return () -> new FileChooser().showOpenMultipleDialog(stageReference.get());
  }

  @Bean
  public DatagramPeer datagramPeer() throws SocketException {
    return new DatagramPeer();
  }

  @Bean
  public List<SenderPeer> senderPeers() {
    return Collections.synchronizedList(new ArrayList<>());
  }

  @Bean
  public ScheduledExecutorService scheduledExecutorService() {
    return Executors.newSingleThreadScheduledExecutor();
  }

  @Bean
  public PeriodicMulticastService<List<SenderPeer>> multicastService(
      ScheduledExecutorService scheduledExecutorService) {
    return Fandem.multicastService(scheduledExecutorService);
  }

  @Bean
  public ObjectProperty<File> folderProperty(Preferences preferences) {
    SimpleObjectProperty<File> property = new SimpleObjectProperty<>();
    String receiveFolderPath = preferences.get(RECEIVE_FOLDER, null);
    if (receiveFolderPath != null) {
      File receiveFolder = new File(receiveFolderPath);
      if (receiveFolder.isDirectory()) {
        property.set(receiveFolder);
      }
    }
    property.addListener((observableValue, oldValue, newValue) -> {
      if (newValue != null) {
        preferences.put(RECEIVE_FOLDER, newValue.getAbsolutePath());
        try {
          preferences.flush();
        } catch (BackingStoreException e) { }
      }
    });
    return property;
  }

  @Bean
  public Preferences preferences() {
    return Preferences.userNodeForPackage(FandemDesktopApplication.class);
  }
}
