package com.tambapps.p2p.fandem.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RecordingFileProvider implements FileProvider {
  @Getter
  private final List<File> files = new ArrayList<>();
  private final FileProvider baseProvider;
  @Override
  public File newFile(String name) throws IOException {
    File file = baseProvider.newFile(name);
    files.add(file);
    return file;
  }
}
