#!/bin/bash

set -e -u

echo "Resolving private dependencies for version ${VERSION_PRPC}"

DEPS_DIR="$TRAVIS_BUILD_DIR/target/dependencies"

DEPS_DOWNLOAD_URL_VAR="DEPS_DOWNLOAD_URL_${VERSION_PRPC//\./_}"
DEPS_DOWNLOAD_URL=${!DEPS_DOWNLOAD_URL_VAR}

mkdir -p $DEPS_DIR

curl -L -s -o $DEPS_DIR/dependencies.zip $DEPS_DOWNLOAD_URL

unzip -P $DEPS_PASS -q -o -d $DEPS_DIR $DEPS_DIR/dependencies.zip

for dependency in $DEPS_DIR/*.jar; do
    ./mvnw -q install:install-file \
        -Dfile=${dependency} \
        -DgroupId="com.pega.prpc" \
        -DartifactId="$(basename -s .jar $dependency)" \
        -Dversion=$VERSION_PRPC \
        -Dpackaging=jar \
        -Dversion.prpc=$VERSION_PRPC
done
