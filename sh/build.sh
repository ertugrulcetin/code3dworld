#!/bin/bash

echo "Generating the release build..."

sh ./sh/scene.sh
sh ./sh/editor.sh

if [[ $1 == "zip" ]]
then
  echo "zip is being generated..."
  cd release-builds/Code\ 3D\ World-darwin-x64
  ditto -c -k --sequesterRsrc --keepParent Code\ 3D\ World.app Code-3D-World-Mac-amd64.zip
fi
