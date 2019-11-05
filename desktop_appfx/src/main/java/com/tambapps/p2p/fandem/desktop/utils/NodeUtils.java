package com.tambapps.p2p.fandem.desktop.utils;

import javafx.scene.control.TextField;

public class NodeUtils {

  public static void numberTextField(TextField textField) {
    textField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.matches("\\d*")) {
        textField.setText(newValue.replaceAll("[^\\d]", ""));
      }
    });
  }
}
