#!/bin/bash
lein clean
lein uberjar

# Make sure that you're using Oracle JDK 14!
# Other JDK versions have some bugs with native library linking
JPACKAGE_PATH=/Library/Java/JavaVirtualMachines/jdk-14.0.2.jdk/Contents/Home/bin/jpackage
$JPACKAGE_PATH --main-class backend_3d_scene.core \
               --main-jar backend-3d-scene.jar \
               --input target/uberjar \
               --name scene
