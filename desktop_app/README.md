# Fandem desktop

The desktop app with UI, built with JavaFX and Spring Boot.
The App was written in Java 16, in order to benefit of `jpackage`, a wonderful tool to build 
platform-specific executable files

## Getting Started

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

The desktop app was built with Java 11. Simply run
```
java -jar target/fandem-desktop-0.1.0-SNAPSHOT.jar
```
