import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.*

/**
 * Groovy script to create platform-specific installer, using jpackage
 * To run it, you'll need Java 16
 *
 * Note that this script assumes it is being run from the 'platforms/' directory
 */

cli = new groovy.util.CliBuilder(usage: 'groovy build.groovy [options]')
cli.jar(type: File, 'The Fandem Desktop jar path (relative or absolute)')
cli.help('Print help')
cli.jpackage(type: String, "The jpackage binary path (Defaults to 'jpackage')")
cli.d('debug option')
cli.v(type: String, 'app version')

options = cli.parse(args)

if (options.help) {
  cli.usage()
  return
}
if (!options.jar) {
  println 'Arguments jar and ... are required'
  return
}
if (!options.v) {
  println 'You must provide the jar version'
  return
}

File jarFile = options.jar
if (!jarFile.exists()) {
  println "Jar file $jarFile doesn't exists"
  return
}

File directory = new File(".${File.separator}temp")
// will delete temp ONLY IF it didn't exist before
boolean deleteTemp = !directory.exists()
if (!directory.exists() && !directory.mkdir()) {
  println "Couldn't create temp directory"
  return
}
String osName = System.getProperty('os.name').toLowerCase()
String platform
if (osName.contains('linux')) {
  platform = 'linux'
} else if (osName.contains('windows')) {
  platform = 'windows'
}else if (osName.contains('mac')) {
  platform = 'mac'
  println 'I would be happy to handle Mac OS, if you buy me one of those'
  return
}

DEBUG = options.d
try {
  execute(jarFile, directory, platform, options.jpackage ?: 'jpackage', options.v)
} finally {
  if (deleteTemp) {
    directory.deleteDir()
  }
}

void execute(File jarFile , File directory, String platform, String jpackage, String version) {
  File jmodsDirectory = new File(".${File.separator}javafx-jmods-16")
  if (!jmodsDirectory.isDirectory()) {
    debugPrint 'javaFX jmods not found'
    downloadJavaFxJmods(jmodsDirectory, platform)
  }

  // copy jar in directory
  Files.copy(jarFile.toPath(), new File(directory, jarFile.name).toPath(), StandardCopyOption.REPLACE_EXISTING)

  List<String> command = [
      jpackage,
      '--input' , directory.path,
      // Yep, jar built with spring boot plugin uses this as main class
      '--main-class' , 'org.springframework.boot.loader.JarLauncher',
      // this path is relative to the input directory
      "--main-jar", ".${File.separator}${jarFile.name}",
      '--name', 'Fandem Desktop',
      '--app-version' , version,
      // will be downloaded if not present
      '--module-path', ".${File.separator}javafx-jmods-16",
      "--description", "Fandem desktop allows you to share files between two devices, it also works with then Fandem Android app",
  ]

  String binarySuffix = ''
  switch (platform) {
    case 'linux':
      println 'Platform: Linux'
      command.addAll([
          '--type', 'deb',
          '--linux-deb-maintainer', 'tambapps@gmail.com',
          '--linux-package-name', 'tambapps-fandem-desktop',
          '--icon', ".${File.separator}$platform${File.separator}icon.png"
      ])
      binarySuffix = '.deb'
      break
    case 'windows':
      println 'Platform: Windows'
      command.addAll([
              '--type', 'exe',
              '--win-menu',
              '--win-shortcut',
              "--icon", ".${File.separator}$platform${File.separator}icon.ico"
      ])
      binarySuffix = '.exe'
  }

  debugPrint 'Will run jpackage'
  debugPrint command.join(' ')
  Process proc  = command.execute()
  def sout = new StringBuilder()
  proc.waitForProcessOutput(sout, sout)
  println sout

  def binaryFile = new File(".${File.separator}").listFiles()
      .find {it.name.endsWith(binarySuffix) }
  if (binaryFile) {
    println "Built file " + binaryFile.toPath().normalize()
  }
}


void downloadJavaFxJmods(File outputDirectory, String platform) {
  File outputZipFile = new File(outputDirectory.parentFile, outputDirectory.name + '.zip')
  URL url = new URL("https://gluonhq.com/download/javafx-16-jmods-$platform/")
  debugPrint "Downloading javaFX jmods from $url"
  url.newInputStream().with {
    outputZipFile << it
  }

  debugPrint 'Unzipping javaFX jmods'
  // unzipping in current directory
  unzip(outputZipFile, new File("."))
  outputZipFile.delete()
}

void debugPrint(Object o) {
  if (DEBUG) {
    println o
  }
}

// thanks Baeldung
static void unzip(File file, File destDir) {
  byte[] buffer = new byte[1024];
  try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
    ZipEntry zipEntry = zis.getNextEntry()
    while (zipEntry != null) {
      File newFile = newFile(destDir, zipEntry)
      if (zipEntry.isDirectory()) {
        if (!newFile.isDirectory() && !newFile.mkdirs()) {
          throw new IOException("Failed to create directory " + newFile);
        }
      } else {
        // fix for Windows-created archives
        File parent = newFile.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
          throw new IOException("Failed to create directory " + parent);
        }

        // write file content
        FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while ((len = zis.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
      }
      zipEntry = zis.getNextEntry();
    }
  }
}
static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
  File destFile = new File(destinationDir, zipEntry.getName());

  String destDirPath = destinationDir.getCanonicalPath();
  String destFilePath = destFile.getCanonicalPath();

  if (!destFilePath.startsWith(destDirPath + File.separator)) {
    throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
  }

  return destFile;
}