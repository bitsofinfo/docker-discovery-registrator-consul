#!/bin/bash

cd ../
./gradlew clean fatJar
cp build/libs/docker-discovery-registrator-consul-1.0-RC1-fat.jar sample/sample.jar
cd sample/
docker build --rm=true -t docker-discovery-registrator-consul-sample .