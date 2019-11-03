package com.tambapps.p2p.fandem.desktop.utils;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionsUtils {

  public static <T, R> void bindMapList(ObservableList<T> list, Function<T, R> mapper,List<R> mappedList) {
    list.addListener((ListChangeListener<T>) change -> {
      while (change.next()) {
        if (change.wasAdded()) {
          mappedList.addAll(change.getAddedSubList().stream().map(mapper).collect(Collectors.toList()));
        } else if (change.wasRemoved()) {
          for (T object : change.getRemoved()) {
            int index = list.indexOf(object);
            mappedList.remove(index);
          }
        }
      }
    });
  }
}
