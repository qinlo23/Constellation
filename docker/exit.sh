#!/bin/bash

cd ./rabbitmq-owl
docker compose down
cd ..


cd ./redis-owl
docker compose down
cd ..


cd ./nacos-owl
docker compose down
cd ..


cd ./minio-owl
docker compose down
cd ..


cd ./mysql-owl
docker compose down
cd ..

docker ps -a
