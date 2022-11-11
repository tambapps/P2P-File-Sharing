# Fandem command line

This command line tool allows to send/receive files to/from other fandem peers.

## Usage

```
Fandem command-line 3.1
Usage: <main class> [options] [command] [command options]
  Options:
    -h, --help

  Commands:
    receive      Receive file from another peer. If no peer is provided, the 
            program will look for it
      Usage: receive [options]
        Options:
          -d, --download-path
            the file/directory where the file will be downloaded
            Default: /Users/nfonkoua/workspace/P2P-File-Sharing
          -p, -peer
            the sending peer (in peer notation or hexString)

    send      Send file to another peer
      Usage: send [options] path of the file to send
        Options:
          -i, --ip
            the ip address used to send (optional)
          -p, --port
            the port used to send (optional)
          -t, --timeout
            the port used to send (optional)
            Default: 90000
```

## Getting Started

These instructions will get you a copy of the project up and running on your local machine and assume that you are in a linux environment.

### Prerequisites

The two devices used for the file transfer must be connected to the same network (same WI-FI).

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
