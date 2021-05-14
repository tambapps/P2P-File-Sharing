# Fandem P2P Library

This is where (most of) the magic happens

## Automatic peer detection
Receiver peers can detect sender peers. This is done with UDP.

The sender will [multicast](https://en.wikipedia.org/wiki/Multicast) each seconds the name of the file it wants to share, some other data, and the peer (address + port)
to connect to for the transfer

The receiver will listen to multicasted datagrams on a specific multicast address, and when it receives one 
datagram, it notifies the user

## File sharing process

The sender starts a server and wait for a connection

The receiver connects to this server and then the transfer occurs.

The sender also sends the checksum of the file so that the receiver will verify if the received file is not corrupted

## How to install
This library uses my [speer](https://github.com/tambapps/speer) library. You'll need to install it first. Then run (from `p2p_library/)

```shell
mvn install
```

This step is required if you want to run the command line or desktop app