package com.tambapps.p2p.fandem.fandemdesktop;

import com.tambapps.p2p.fandem.fandemdesktop.fx.FandemDesktopFXApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FandemDesktopApplication {

  public static void main(String[] args) {
    Application.launch(FandemDesktopFXApplication.class, args);
  }

}
