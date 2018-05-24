# Spring File Sharing

This project can be used to transfer files from one computer to another if they are connected on the same wifi.


## Getting Started

These instructions will get you a copy of the project up and running on your local machine and assume that you are in a linux environment.


### Prerequisites

The two components used for the file transfer must be connected to the same wifi


### Installing
You can run this app from your IDE or generate the jar file by running
```
mvn install
```
Add the '-DskipTests' option if you want to skip tests.

### Running
To start the program you must provide a path in which the received files will be downloaded. If you have generated the jar file, run
```
java -jar target/File-Sharing-0.0.1-SNAPSHOT.jar --download.path=path/to/download/files/
```
Or if you want to run it from your IDE, add "--download.path=path/to/download/files/" in program arguments. Then go to your web browser at localhost:8080 and from there, you can setup the receive/send procedure

###Command line
You can also transfer files directly from the command line:

To receive a file:
```
java -jar File-Sharing-0.0.1-SNAPSHOT.jar -receive -downloadPath=/path/to/file -peer=0.0.0.0:8081
```
To send a file:
```
java -jar File-Sharing-0.0.1-SNAPSHOT.jar -send -filePath=/path/to/file
```
## Built With

* [Maven](https://maven.apache.org/)
* [Spring Boot](https://projects.spring.io/spring-boot/)
* [Thymeleaf](https://www.thymeleaf.org/)
