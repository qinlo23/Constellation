#!/bin/bash
	
# for owl project 

cd ./rabbitmq
docker compose up -d
cd ..


cd ./redis
docker compose up -d
cd ..


cd ./nacos
docker compose up -d
cd ..


cd ./minio
docker compose up -d
cd ..


cd ./mysql
docker compose up -d
cd ..


docker ps -a



