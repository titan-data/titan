#!/usr/bin/env bash

set -ex

version=`cat VERSION`

cp ${PWD}/VERSION ${PWD}/src/main/resources

./mvnw versions:set -DnewVersion=$version
./mvnw versions:commit
./mvnw clean install