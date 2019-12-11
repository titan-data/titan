#!/usr/bin/env bash

os=$1

#Setup Linux
if [ $os = "ubuntu-18.04" ]; then
  ${PWD}/scripts/build-linux.sh
fi

#Setup OSX
if [ $os = "macos-latest" ]; then
  export PATH=${PWD}/graalvm-ce-java11-19.3.0/Contents/Home/bin:$PATH
  export JAVA_HOME=${PWD}/graalvm-ce-java11-19.3.0/Contents/Home
  ${PWD}/scripts/build-osx.sh
fi
