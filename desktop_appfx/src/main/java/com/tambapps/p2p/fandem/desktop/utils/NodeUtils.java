package com.tambapps.p2p.fandem.desktop.utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class NodeUtils {

  public static void numberTextField(TextField textField) {
    textField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue,
                          String newValue) {
        if (!newValue.matches("\\d*")) {
          textField.setText(newValue.replaceAll("[^\\d]", ""));
        }
      }
    });
  }
}
