#!/bin/bash
cd backend-3d-scene
sh ./build.sh
cd ../

lein clean
lein release
rm -rf release-builds
electron-packager . --prune=true \
                    --out=release-builds \
                    --ignore="(.gitignore|.clj-kondo|.github|.shadow-cljs|.idea|.lein-repl-history)"\
                    --ignore="(dev|docs|imgs|\.md|\.iml|build.sh|pom.xml|karma.conf.js|target)" \
                    --ignore="(backend-3d-scene/)" \
                    --ignore="(project.clj|LICENSE)"
