#!/bin/bash

cd ./rabbitmq
docker compose down
cd ..


cd ./redis
docker compose down
cd ..


cd ./nacos
docker compose down
cd ..


cd ./minio
docker compose down
cd ..


cd ./mysql
docker compose down
cd ..

docker ps -a
