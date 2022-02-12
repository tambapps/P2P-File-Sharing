package com.tambapps.p2p.fandem.desktop.fx;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.desktop.util.RegionLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@Component // for dependency injection
public class StageInitializer implements ApplicationListener<FandemDesktopFXApplication.StageReadyEvent> {

  @Value("classpath:/view/app.fxml")
  private Resource chartResource;
  @Value("classpath:/view/sendPane.fxml")
  private Resource sendPaneResource;
  @Value("classpath:/view/receivePane.fxml")
  private Resource receivePaneResource;
  @Value("classpath:/icon.png")
  private Resource iconResource;
  private final RegionLoader regionLoader;
  private final AtomicReference<Stage> stageReference;

  public StageInitializer(RegionLoader regionLoader, AtomicReference<Stage> stageReference) {
    this.regionLoader = regionLoader;
    this.stageReference = stageReference;
  }

  @Override
  public void onApplicationEvent(FandemDesktopFXApplication.StageReadyEvent stageReadyEvent) {
    try {
      VBox root = regionLoader.load(chartResource);
      Stage stage = stageReadyEvent.getStage();
      stageReference.set(stage);
      stage.setTitle("Fandem Desktop " + Fandem.VERSION);
      stage.getIcons().add(new Image(iconResource.getInputStream()));

      Pane panesContainer = (Pane) root.getChildren().get(0);
      initializePanes(panesContainer);

      stage.setScene(new Scene(root));
      stage.show();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void initializePanes(Pane panesContainer) throws IOException {
    Region sendPane = regionLoader.load(sendPaneResource);
    Region receivePane = regionLoader.load(receivePaneResource);

    panesContainer.getChildren().addAll(receivePane, sendPane);
  }
}
