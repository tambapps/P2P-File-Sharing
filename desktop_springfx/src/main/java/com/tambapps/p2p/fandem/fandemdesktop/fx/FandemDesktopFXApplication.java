package com.tambapps.p2p.fandem.fandemdesktop.fx;

import com.tambapps.p2p.fandem.fandemdesktop.FandemDesktopApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

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
    ExecutorService executor = (ExecutorService) context.getBean("executorService");
    executor.shutdownNow(); // TODO open dialog if a task isn't finished to comfirm close
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
