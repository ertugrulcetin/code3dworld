#!/bin/bash

echo "Building Editor..."

lein clean
lein release
lein less once
rm -rf release-builds
electron-packager . --prune=true \
                    --out=release-builds \
                    --overwrite=true \
                    --ignore="(.gitignore|.clj-kondo|.github|.shadow-cljs|.idea|.lein-repl-history)"\
                    --ignore="(dev|docs|imgs|\.md|\.iml|build.sh|pom.xml|karma.conf.js|target)" \
                    --ignore="(backend-3d-scene/)" \
                    --ignore="(project.clj|LICENSE)"
