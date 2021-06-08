#!/bin/bash

/home/nelson/.jdks/openjdk-16.0.1/bin/jpackage --input ../../target/ \
  --main-jar fandem-desktop-2.0.jar \
  --main-class com.tambapps.p2p.fandem.desktop.FandemDesktopApplication \
  --type deb \
  --name "Fandem Desktop" \
  --app-version 2.0 \
  --description 'Fandem desktop allows you to share files between two devices, it also works with then Fandem Android app' \
  --linux-deb-maintainer tambapps@gmail.com \
  --linux-package-name tambapps-fandem-desktop \
  --icon ./icon.png \
  --java-options '--enable-preview'