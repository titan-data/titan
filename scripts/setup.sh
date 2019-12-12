#!/usr/bin/env bash

os=$1

#Setup Linux
if [ $os = "ubuntu-18.04" ]; then
  echo $os
fi

#Setup OSX
if [ $os = "macos-latest" ]; then
  echo $os
  curl -L https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-19.3.0/graalvm-ce-java11-darwin-amd64-19.3.0.tar.gz --output graal.tar.gz
  gunzip -c graal.tar.gz | tar xopf -
  export PATH=${PWD}/graalvm-ce-java11-19.3.0/Contents/Home/bin:$PATH
  export JAVA_HOME=${PWD}/graalvm-ce-java11-19.3.0/Contents/Home
  gu install native-image
fi
