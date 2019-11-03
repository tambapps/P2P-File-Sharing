package com.tambapps.p2p.fandem.desktop.utils;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionsUtils {

  // MUST IMPLEMENT RELEVANT EQUALS AND HASHCODE METHOD TO THE R TYPE
  // for now only handle add/remove
  public static <T, R> void bindMapList(ObservableList<T> list, Function<T, R> mapper,List<R> mappedList) {
    list.addListener((ListChangeListener<T>) change -> {
      while (change.next()) {
        if (change.wasAdded()) {
          mappedList.addAll(change.getAddedSubList().stream().map(mapper).collect(Collectors.toList()));
        } else if (change.wasRemoved()) {
          mappedList.remove(change.getTo());
        }
      }
    });
  }
}
