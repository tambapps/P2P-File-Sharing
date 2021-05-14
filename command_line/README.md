# Fandem command line

This command line tool allows to send/receive files to/from other fandem peers.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine and assume that you are in a linux environment.

### Prerequisites

- You must have installed [speer](https://github.com/tambapps/speer) and the [p2p_library](https://github.com/tambapps/P2P-File-Sharing/blob/master/p2p_library/README.md)
- The two devices used for the file transfer must be connected to the same network (same WI-FI).

The two components used for the file transfer must be connected to the same wifi. Public WIFIs don't work for these transfers.

### Installing
You can run this app from your IDE or generate the jar file by running (from `command_line/`)
```
mvn install
```

### Running

#### Receive a file
```
java -jar target/file-sharing-command-line-jar-with-dependencies.jar receive -d=/path/to/file -p=0.0.0.0:8081
```

If you don't want to specify the peer, and let the app find it itself, just omit the last argument 
```
java -jar target/file-sharing-command-line-jar-with-dependencies.jar receive -d=/path/to/file
```
The app will propose you sender peers it finds.

#### Send a file

```
java -jar target/file-sharing-command-line-jar-with-dependencies.jar send /path/to/file
```

You can specify the ip and the port used to send with the -ip and -port options.
