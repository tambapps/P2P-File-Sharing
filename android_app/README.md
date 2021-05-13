# Peer Transfer

This is the source code of the android app Peer Transfer.
 
 Link: [https://play.google.com/store/apps/details?id=com.tambapps.p2p.peer_transfer.android](https://play.google.com/store/apps/details?id=com.tambapps.p2p.peer_transfer.android)

You can send/receive file to another device (desktop/Android), as long as they are on the same local network.

## Android 11 file permission changes

Since Android 11 (sdk version 30), an app can't access all files of the android device 
(actually it can but your app must be approved by Google for this and this is really difficult).

So for devices running on Android 11+, the files are downloaded in an app private directory instead of
the standard `Download` folder. You will then be able to move received files to any public directory you like
from the `Managed Received Files` screen, that will only appear for Android 11+ devices