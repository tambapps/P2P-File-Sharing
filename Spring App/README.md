# Spring File Sharing

This project can be used to transfer files from one computer to another if they are connected on the same wifi.


## Getting Started

These instructions will get you a copy of the project up and running on your local machine and assume that you are in a linux environment.


### Prerequisites

The two components used for the file transfer must be connected to the same wifi


### Installing
You must set up the ip that will be used for the file transfer (if you are the sender). To see the ip to set up, you can type this command:

```
ifconfig
```

And pick the right one (it is an inet adr, and it isn't 127.0.0.1) and put it in the application.properties, in the server.address field

After that, you cloud directly run it from your IDE or generate a jar file by running
```
mvn install -DskipTests
```

## Running
To start the program you must provide a path to download files in when you receive file. If you generate a jar file, run
```
java -jar target/File-Sharing-0.0.1-SNAPSHOT.jar --download.ath=path/to/download/files/
```
Or if you want to run it from your IDE, add "--download.ath=path/to/download/files/" in program arguments. Then go to your web browser at localhost:8080 and from there, you can setup the receive/send procedure


## Built With

* [Maven](https://maven.apache.org/)
* [Spring Boot](https://projects.spring.io/spring-boot/)
* [Thymeleaf](https://www.thymeleaf.org/)
