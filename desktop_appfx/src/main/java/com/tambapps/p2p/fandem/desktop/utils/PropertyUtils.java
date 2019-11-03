package com.tambapps.p2p.fandem.desktop.utils;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Objects;
import java.util.function.Function;

public class PropertyUtils {

  public static <T, R> Property<R> mapProperty(Property<T> property, Function<T, R> mapper) {
    Property<R> mapped;
    mapped = new SimpleObjectProperty<>(mapper.apply(property.getValue()));
    property.addListener((observableValue, oldValue, newValue) -> mapped.setValue(mapper.apply(newValue)));
    return mapped;
  }

  public static <T> Property<String> nullableToStringProperty(Property<T> property, Function<T, String> mapper) {
    return mapProperty(property, o -> o == null ? "" : mapper.apply(o));
  }

  public static <T> Property<String> nullableToStringProperty(Property<T> property) {
    return nullableToStringProperty(property, Objects::toString);
  }
}
