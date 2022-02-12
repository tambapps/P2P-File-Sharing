package com.tambapps.p2p.fandem.util;

import com.tambapps.p2p.fandem.FileSharer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class FileUtils {

  /**
   * Display size of bytes with a appropriate unit
   *
   * @param bytes the bytes to display
   * @return a String representing the bytes in the appropriate unit
   */
  public static String toFileSize(long bytes) {
    String units = "kMG";
    long denominator = 1;
    int i = 0;

    while (bytes / (denominator * 1024) > 0 && i < units.length()) {
      denominator *= 1024;
      i++;
    }
    float fileSize = ((float) bytes) / ((float) denominator);
    int fileSizeInt = (int) fileSize;
    return String.format(Locale.US, "%s %s",
        fileSizeInt == fileSize ? fileSizeInt : String.format("%.1f", fileSize),
        i == 0 ? "bytes" : units.charAt(i - 1) + "B");
  }

  /**
   * provide a non-existing file in the given directory with the given name if it does not exists.
   * If a file with the given name exits, it will look for a non-existing file with the following name
   * fileName_xxx with xxx a number
   *
   * @param directory the directory in which to get the file
   * @param name      the name wanted of the file
   * @return an non-existing file in the given directory with a name starting with to the given one
   * @throws IOException in case of I/O error
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

  public static FileProvider availableFileInDirectoryProvider(File directory) {
    return name -> newAvailableFile(directory, name);
  }

  public static File newAvailableFile(String directory, String name) throws IOException {
    return newAvailableFile(new File(directory), name);
  }

  public static String decodePath(String path) {
    try {
      return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (int i = 0; i < hash.length; i++) {
      String hex = Integer.toHexString(0xff & hash[i]);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  public static String computeChecksum(File file) throws IOException {
    try (FileInputStream inputStream = new FileInputStream(file)) {
      return computeChecksum(inputStream);
    }
  }

  public static MessageDigest getSha256MessageDigest() throws IOException {
    MessageDigest digest = null;
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IOException("Couldn't find MD5 algorithm", e);
    }
  }

  public static String computeChecksum(InputStream inputStream) throws IOException {
    byte[] buffer = new byte[FileSharer.DEFAULT_BUFFER_SIZE];
    int count;
    MessageDigest digest = getSha256MessageDigest();
    while ((count = inputStream.read(buffer)) > 0) {
      digest.update(buffer, 0, count);
    }
    byte[] hash = digest.digest();
    return bytesToHex(hash);
  }
}
