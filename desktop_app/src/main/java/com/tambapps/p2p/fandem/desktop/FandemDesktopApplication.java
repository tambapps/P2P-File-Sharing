package com.tambapps.p2p.fandem.desktop;

import com.tambapps.p2p.fandem.desktop.fx.FandemDesktopFXApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FandemDesktopApplication {

  public static void main(String[] args) {
    Application.launch(FandemDesktopFXApplication.class, args);
  }

}
