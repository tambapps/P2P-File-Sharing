package com.tambapps.p2p.fandem.desktop.fx;

import com.tambapps.p2p.fandem.desktop.FandemDesktopApplication;
import com.tambapps.p2p.fandem.desktop.service.PeerSniffingService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public class FandemDesktopFXApplication extends Application {
  // spring context
  private ConfigurableApplicationContext context;

  @Override
  public void init() {
    context = new SpringApplicationBuilder(FandemDesktopApplication.class).run();
  }

  @Override
  public void start(Stage stage) {
    context.publishEvent(new StageReadyEvent(stage));
  }

  @Override
  public void stop() {
    Map<String, ExecutorService> executors = context.getBeansOfType(ExecutorService.class);
    PeerSniffingService sniffService = context.getBean(PeerSniffingService.class);
    sniffService.stop();
    executors.values().forEach(ExecutorService::shutdown);
    context.stop();
    Platform.exit();
  }

  static class StageReadyEvent extends ApplicationEvent {
    public StageReadyEvent(Stage stage) {
      super(stage);
    }

    public Stage getStage() {
      return (Stage) getSource();
    }
  }

}
