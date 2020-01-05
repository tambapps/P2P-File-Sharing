package com.tambapps.p2p.fandem.fandemdesktop.util;

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

import java.util.function.Function;

public class PropertyUtils {

  public static <T, R> void bindMapProperty(Property<T> property, Function<T, R> mapper,
                                            Property<R> mappedProperty) {
    property.addListener(
        (observable, oldValue, newValue) -> mappedProperty.setValue(mapper.apply(newValue)));
  }

  public static <T> void bindMapToStringProperty(Property<T> property, Function<T, String> mapper,
                                                 StringProperty stringProperty) {
    property.addListener(
        (observable, oldValue, newValue) -> stringProperty.setValue(mapper.apply(newValue)));
  }

  public static <T> void bindMapNullableToStringProperty(Property<T> property,
                                                         Function<T, String> mapper, StringProperty stringProperty) {
    property.addListener((observable, oldValue, newValue) -> stringProperty
        .setValue(newValue == null ? "" : mapper.apply(newValue)));
  }
}
