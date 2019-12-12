#!/usr/bin/env bash

set -ex

version=`cat VERSION`

native-image -cp ${PWD}/target/titan-$version.jar\
    -H:Name=titan\
    -H:Class=io.titandata.titan.Cli\
    -H:+ReportUnsupportedElementsAtRuntime\
    -H:ReflectionConfigurationFiles=${PWD}/config/reflect-config.json\
    -H:ResourceConfigurationFiles=${PWD}/config/resource-config.json\
    -H:JNIConfigurationFiles=${PWD}/config/jni-config.json\
    -H:+AddAllCharsets\
    --allow-incomplete-classpath\
    --enable-http\
    --enable-https\
    --no-fallback

mkdir -p ${PWD}/releases/
zip ${PWD}/releases/titan-cli-$version-darwin_amd64.zip titan