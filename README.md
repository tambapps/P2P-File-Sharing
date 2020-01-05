# P2P File Sharing

This project aims to transfer files from one device (computer or android smartphone) to another. It works only if the two devices are on the same local network. If one of the devices is an android smartphone, you can share data with it and connect the other device to the smartphone's data.

## P2P Library

This is the library I built, used by both the Android app and the Desktop app. It is because of this library that we can also share files between android and desktop.
I first developed it in Java 7 but then decided to migrate it to Kotlin. Kotlin was a better choice for making this library compatible on both Android and Java desktop applications.

## Android app

This is the Android app that performs P2P file sharing

![alt text](https://raw.githubusercontent.com/tambapps/P2P-File-Sharing/master/screenshots/android.png)

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" alt="Get it on Google Play" height="80">](https://play.google.com/store/apps/details?id=com.tambapps.p2p.peer_transfer.android)

## Desktop app

The desktop app was developed with JavaFX and Spring Boot (for dependency injection). It was developed in Java 11.

![alt text](https://raw.githubusercontent.com/tambapps/P2P-File-Sharing/master/screenshots/desktop.png)


