package com.tambapps.p2p.fandem.model;

import com.tambapps.p2p.fandem.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileData {
  String fileName;
  long fileSize;
  // nullable
  String checksum;

  public Optional<String> getChecksum() {
    return Optional.ofNullable(checksum);
  }


  public static FileData fromFile(File file) throws IOException {
    return fromFile(file, true);
  }

  public static FileData fromFile(File file, boolean withChecksum) throws IOException {
    String checksum;
    if (withChecksum) {
      checksum = FileUtils.computeChecksum(file);
    } else {
      checksum = null;
    }
    return new FileData(file.getName(), file.length(), checksum);
  }
}
