#!/usr/bin/env bash

os=$1

#Setup Linux
if [ $os = "ubuntu-18.04" ]; then
  echo $os
fi

#Setup OSX
if [ $os = "macOS-10.14" ]; then
  echo $os
  curl -L https://github.com/oracle/graal/releases/download/vm-19.0.0/graalvm-ce-darwin-amd64-19.0.0.tar.gz --output graal.tar.gz
  gunzip -c graal.tar.gz | tar xopf -
  export PATH=${PWD}/graalvm-ce-19.0.0/Contents/Home/bin:$PATH
  export JAVA_HOME=${PWD}/graalvm-ce-19.0.0/Contents/Home/jre
  gu install native-image
fi
