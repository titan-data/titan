#!/usr/bin/env bash

set -ex

version=`cat VERSION`
entry="docker run -v ${HOME}/.m2:/root/.m2 -v ${PWD}:/cli --workdir /cli gvm-native:19.3.0"
docker build -t gvm-native:19.3.0 .
${entry} native-image -cp /cli/target/titan-$version.jar\
    -H:Name=titan\
    -H:Class=io.titandata.titan.Cli\
    -H:+ReportUnsupportedElementsAtRuntime\
    -H:ReflectionConfigurationFiles=/cli/config/reflect-config.json\
    -H:ResourceConfigurationFiles=/cli/config/resource-config.json\
    -H:JNIConfigurationFiles=/cli/config/jni-config.json\
    -H:+AddAllCharsets\
    --allow-incomplete-classpath\
    --enable-http\
    --enable-https\
    --no-fallback

${entry} mkdir -p /cli/releases/
${entry} tar -cvf /cli/releases/titan-cli-$version-linux_amd64.tar titan