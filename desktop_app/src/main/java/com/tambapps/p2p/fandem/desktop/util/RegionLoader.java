package com.tambapps.p2p.fandem.desktop.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;
import javafx.util.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RegionLoader {

  private final ApplicationContext context;

  public RegionLoader(ApplicationContext context) {
    this.context = context;
  }

  public <R extends Region> R load(Resource fxmlResource) throws IOException {
    FXMLLoader loader = new FXMLLoader(fxmlResource.getURL());
    loader.setControllerFactory(context::getBean);
    return loader.load();
  }

  public <R extends Region, T> Pair<R, T> loadWithController(Resource fxmlResource) throws IOException {
    FXMLLoader loader = new FXMLLoader(fxmlResource.getURL());
    loader.setControllerFactory(context::getBean);
    R region = loader.load();
    return new Pair<>(region, loader.getController());
  }

}
