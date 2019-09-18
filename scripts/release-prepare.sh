#!/usr/bin/env bash

version=`cat VERSION`
osx=/releases/${version}/titan-cli-$version-darwin_amd64.zip

if [ ! -f "${PWD}/${osx}" ]; then
    echo "${osx} does not exist. Use './scripts/build-osx.sh' to create an OSX release."
    exit 1
fi
