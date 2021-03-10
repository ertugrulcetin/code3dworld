#!/bin/bash

echo "Building 3D Scene..."

cd backend-3d-scene
lein clean
lein uberjar

cd target
rm -rf classes native stale backend-3d-scene-0.1.0-SNAPSHOT.jar
cd ..

echo "Make sure that you're using Oracle JDK 14!"
# Make sure that you're using Oracle JDK 14!
# Other JDK versions have some bugs with native library linking
JPACKAGE_PATH=/Library/Java/JavaVirtualMachines/jdk-14.0.2.jdk/Contents/Home/bin/jpackage
$JPACKAGE_PATH --main-class backend_3d_scene.core \
               --main-jar backend-3d-scene.jar \
               --input target \
               --name scene
hdiutil attach scene-1.0.dmg
cp -rf /Volumes/scene/scene.app ../resources/
hdiutil unmount /Volumes/scene
rm -rf scene-1.0.dmg

# Go back to root dir: code3dworld
cd ../
