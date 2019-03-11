package com.tambapps.p2p.fandem.util;

import com.tambapps.p2p.fandem.task.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class FileUtils {

  /**
   * Display size of bytes with a appropriate unit
   * @param bytes the bytes to display
   * @return a String representing the bytes in the appropriate unit
   */
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

  /**
   * provide a non-existing file in the given directory with the given name if it does not exists.
   * If a file with the given name exits, it will look for a non-existing file with the following name
   * fileName_xxx with xxx a number
   * @param directory the directory in which to get the file
   * @param name the name wanted of the file
   * @return an non-existing file in the given directory with a name starting with to the given one
   * @throws IOException
   */
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

  public static FileProvider newAvailableFileProvider(File directory) {
    return name -> newAvailableFile(directory, name);
  }

  public static File newAvailableFile(String directory, String name) throws IOException {
    return newAvailableFile(new File(directory), name);
  }

  public static String decodePath(String path) throws UnsupportedEncodingException {
    return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
  }

}
