#!/usr/bin/env bash

set -ex

version=`cat VERSION`
entry="docker run -v ${HOME}/.m2:/root/.m2 -v ${PWD}:/cli --workdir /cli gvm-native:19.0.0"
docker build -t gvm-native:19.0.0 .
${entry} ./mvnw jar:jar install:install -DgroupId=org.bouncycastle -DartifactId=bcprov-jdk15on -Dversion=1.62
${entry} native-image -cp /cli/target/titan-$version-jar-with-dependencies.jar\
    -H:Name=titan\
    -H:Class=io.titandata.titan.Cli\
    -H:+ReportUnsupportedElementsAtRuntime\
    -H:ReflectionConfigurationFiles=/cli/config/reflect-config.json\
    -H:ResourceConfigurationFiles=/cli/config/resource-config.json\
    -H:+AddAllCharsets\
    --initialize-at-run-time=org.bouncycastle.crypto.prng.SP800SecureRandom\
    --initialize-at-run-time=org.bouncycastle.jcajce.provider.drbg.DRBG$Default\
    --initialize-at-run-time=org.bouncycastle.jcajce.provider.drbg.DRBG$NonceAndIV\
    -J-Djava.security.properties=${PWD}/java.security.overrides\
    --allow-incomplete-classpath\
    --enable-http\
    --enable-https

${entry} mkdir -p /cli/releases/
${entry} tar -cvf /cli/releases/titan-cli-$version-linux_amd64.zip titan