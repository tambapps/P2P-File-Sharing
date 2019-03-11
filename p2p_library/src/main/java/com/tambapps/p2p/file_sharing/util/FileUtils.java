package com.tambapps.p2p.file_sharing.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class FileUtils {

  public static String bytesToString(long bytes) {
    String units = "kMG";
    long denominator = 1;
    int i = 0;

    while (bytes / (denominator * 1024) > 0 && i < units.length()) {
      denominator *= 1024;
      i++;
    }
    return String.format(Locale.US, "%.1f %sB", ((float)bytes)/((float)denominator),
      i == 0 ? "" : units.charAt(i - 1));
  }

  public static File newAvailableFile(File directory, String name) throws IOException {
    File file = new File(directory, name);
    if (file.exists()) { //searching available file name
      for (int i = 0; file.exists() && i < 999; i++) {
        StringBuilder number = new StringBuilder(String.valueOf(i));
        while (number.length() < 3) {
          number.insert(0, '0');
        }
        String fileName;
        if (name.contains(".")) {
          int dotIndex = name.indexOf('.');
          fileName = name.substring(0, dotIndex) + '_' + number + name.substring(dotIndex);
        } else {
          fileName = name + '_' + number;
        }
        file = new File(directory, fileName);
      }
    }
    if (!file.createNewFile()) {
      throw new IOException("Couldn't create new file");
    }
    return file;
  }

  public static File newAvailableFile(String directory, String name) throws IOException {
    return newAvailableFile(new File(directory), name);
  }

  public static String decodePath(String path) throws UnsupportedEncodingException {
    return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
  }

}
