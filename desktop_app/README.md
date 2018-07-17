# Desktop File Sharing Tool

This project can be used to transfer files from one computer to another if they are connected on the same wifi.


## Getting Started

These instructions will get you a copy of the project up and running on your local machine and assume that you are in a linux environment.


### Prerequisites

The two components used for the file transfer must be connected to the same wifi. Public WIFIs don't work for these transfers.


### Installing
You can run this app from your IDE or generate the jar file by running
```
mvn install
```
You may need to run the same command in the ../P2P Library/ folder before installing this project.
Add the '-DskipTests' option if you want to skip tests.

### Running
To start the program you must provide a path in which the received files will be downloaded. To do that, run
```
java -jar target/file_sharing_app-0.0.2-SNAPSHOT.jar --download.path=path/to/download/files/
```
Or if you want to run it from your IDE, add "--download.path=path/to/download/files/" in program arguments. 

Then go to your web browser at localhost:8080 and from there, you can setup the receive/send procedure

###Command line
You can also transfer files directly from the command line:

To receive a file:
```
java -jar file-sharing-command-line-jar-with-dependencies.jar receive -downloadPath=/path/to/file -peer=0.0.0.0:8081
```
To send a file:
```
java -jar file-sharing-command-line-jar-with-dependencies.jar send /path/to/file
```

You can specify the ip and the port used to send with the -ip and -port options.

## Built With

* [Maven](https://maven.apache.org/)
* [Spring Boot](https://projects.spring.io/spring-boot/)
* [Thymeleaf](https://www.thymeleaf.org/)
* [Jcommander](http://jcommander.org/)
