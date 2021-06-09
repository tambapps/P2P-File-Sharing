# Fandem desktop

The desktop app with UI, built with JavaFX and Spring Boot.
The App was written in Java 16, in order to benefit of `jpackage`, a wonderful tool to build 
platform-specific executable files


## Install the app

You can find binary files to install Fandem Desktop on your computer in the [github release page](https://github.com/tambapps/P2P-File-Sharing/releases)
### Linux
Download the `.deb` file and then click on it to install it, or directly install it from a terminal:
```shell
sudo dpkg -i tambapps-fandem-desktop_2.1-1_amd64.deb
```


To uninstall run

```shell
sudo apt-get remove tambapps-fandem-desktop
```

### Windows
TODO


### Mac
TODO(nt)


## Running with java

Fandem Desktop was developped in Java 16 (mainly to benefit of jpackage, a wonderful tool to build platform-specific installers/executables)


These instructions will get you a copy of the project up and running on your local machine and assume that you are in a linux environment.

### Prerequisites

- You must have installed [speer](https://github.com/tambapps/speer) and the [p2p_library](https://github.com/tambapps/P2P-File-Sharing/blob/master/p2p_library/README.md)

- The two devices used for the file transfer must be connected to the same network (same WI-FI).

### Installing
You can run this app from your IDE or generate the jar file by running (from `desktop_app/`)
```
mvn install
```

### Running

The desktop app was built with Java 16. Simply run
```
java -jar target/fandem-desktop-0.1.0-SNAPSHOT.jar
```
