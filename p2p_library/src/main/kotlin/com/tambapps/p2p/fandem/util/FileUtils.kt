package com.tambapps.p2p.fandem.util

import com.tambapps.p2p.fandem.task.FileProvider
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*

object FileUtils {
  /**
   * Display size of bytes with a appropriate unit
   * @param bytes the bytes to display
   * @return a String representing the bytes in the appropriate unit
   */
  @JvmStatic
  fun bytesToString(bytes: Long): String {
    val units = "kMG"
    var denominator: Long = 1
    var i = 0
    while (bytes / (denominator * 1024) > 0 && i < units.length) {
      denominator *= 1024
      i++
    }
    return String.format(Locale.US, "%.1f %sB", bytes.toFloat() / denominator.toFloat(),
        if (i == 0) "" else units[i - 1])
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
  @JvmStatic
  @Throws(IOException::class)
  fun newAvailableFile(directory: File?, name: String): File {
    var file = File(directory, name)
    if (file.exists()) { //searching available file name
      var i = 0
      while (file.exists() && i < 999) {
        val number = StringBuilder(i.toString())
        while (number.length < 3) {
          number.insert(0, '0')
        }
        var fileName: String
        fileName = if (name.contains(".")) {
          val dotIndex = name.indexOf('.')
          name.substring(0, dotIndex) + '_' + number + name.substring(dotIndex)
        } else {
          name + '_' + number
        }
        file = File(directory, fileName)
        i++
      }
    }
    if (!file.createNewFile()) {
      throw IOException("Couldn't create new file")
    }
    return file
  }

  @JvmStatic
  fun availableFileInDirectoryProvider(directory: File): FileProvider {
    return object : FileProvider {
      override fun newFile(name: String): File {
        return newAvailableFile(directory, name)
      }
    }
  }

  @JvmStatic
  fun staticFileProvider(file: File): FileProvider {
    return object : FileProvider {
      override fun newFile(name: String): File {
        return file
      }
    }
  }

  @JvmStatic
  @Throws(IOException::class)
  fun newAvailableFile(directory: String, name: String): File {
    return newAvailableFile(File(directory), name)
  }

  @JvmStatic
  @Throws(UnsupportedEncodingException::class)
  fun decodePath(path: String): String {
    return URLDecoder.decode(path, StandardCharsets.UTF_8.name())
  }
}