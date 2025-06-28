#!/bin/bash
VERSION=0.0.1-SNAPSHOT

echo "freight-api version: $VERSION"
mvn clean package
docker build --build-arg JAR_FILE=target/freight-api-${VERSION}.jar -t freight-api:${VERSION} .