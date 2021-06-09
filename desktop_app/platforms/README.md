# Build platform-specific installers

This directory contains a script allowing to create a binary file that will install Fandem Desktop on your computer. You can find
these files in the [github release page](https://github.com/tambapps/P2P-File-Sharing/releases)


## Prerequisites

You'll need groovy to run this script and a Java 16 JDK, to benefit from `jpackage`

## How to run
This script should be run from this directory (`platforms/`)
This script has one required parameter

- `jar`: the path of Fandem Desktop jar

And it has some optional parameters

- `jpackage`: the path of the jpackage binary, it will be used to run the app. This is useful if Java 16 is not your default JDK
- `d`: the debug option, that will print more details of what the script is doing

### Examples

```shell
groovy build.groovy -jar ../target/fandem-desktop-2.1.jar -jpackage /home/tambapps/.jdks/openjdk-16.0.1/bin/jpackage
```
