#!/usr/bin/env bash

set -e  

name=kubeauction
modules=("notifications" "api")

cd KubeAuction

mvn clean package -DskipTests

cd ..

for i in "${modules[@]}"; do
    docker build -t "$name-$i" "./KubeAuction/$i"
done

docker compose down
docker compose up --build --pull missing

