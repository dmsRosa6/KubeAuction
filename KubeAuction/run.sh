#!/usr/bin/env bash

set -e  # Stop on error

name=kubeauction
modules=("notifications" "api")

mvn clean package -DskipTests

for i in "${modules[@]}"; do
    docker build -t "$name-$i" "./$i"
done

docker compose down
docker compose up --build --pull missing

