#!/bin/bash
# Prerequisites
# - download platform-specific JavaFX jmod jars https://gluonhq.com/products/javafx/
#
# TODO pass platform as an argument when I will handle

# TODO filter useless modules for this project (--add-modules arg)
# TODO still doesn't work

platform=linux

/home/nelson/.jdks/openjdk-16.0.1/bin/jpackage --input ../target/ \
  --main-jar fandem-desktop-2.1.jar \
  --main-class com.tambapps.p2p.fandem.desktop.FandemDesktopApplication \
  --type deb \
  --name "Fandem Desktop" \
  --app-version 2.1 \
  --module-path ./javafx-jmods-16 \
  --add-modules java.base,javafx.controls,javafx.web,javafx.graphics,javafx.media,java.datatransfer,java.desktop,java.scripting,java.xml,jdk.jsobject,jdk.unsupported,jdk.unsupported.desktop,jdk.xml.dom,javafx.fxml,java.naming,java.sql,jdk.charsets \
  --description 'Fandem desktop allows you to share files between two devices, it also works with then Fandem Android app' \
  --linux-deb-maintainer tambapps@gmail.com \
  --linux-package-name tambapps-fandem-desktop \
  --icon $platform/icon.png \
