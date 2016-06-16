# Reactive Push Notifications Example

## Kafka

1. Install [Docker Toolbox](https://www.docker.com/products/docker-toolbox).
2. Setup Docker

    ```
    docker-machine create -d "virtualbox" kafka
    docker-machine ip kafka
    eval $(docker-machine env kafka)
    ```
3. Start Kafka

    ```
    docker-compose -f docker/docker-compose.yml up -d
    ```
4. Test Kafka/create a new topic (requires [Kafka binaries](http://kafka.apache.org/downloads.html))

    ```
    ./kafka-topics.sh --zookeeper 192.168.99.100:2181 --list
    ./kafka-topics.sh --zookeeper 192.168.99.100:2181 --create --topic notifications --partitions 1 --replication-factor 1
    ```

## API

##### Register a single device

Token must be a 64 chars hex string

```
curl -H "Content-Type: application/json" -X POST -d '{"token":"a77..."}' http://localhost:8080/devices
```

##### Bulk load devices

Generate dummy tokens:

```
hexdump -n 1000000 -v -e '/1 "%02X"' -e '/100 "\n"' /dev/urandom > tokens.dmp
```

Upload tokens:

```
curl -F "data=@tokens.dmp" http://localhost:8080/devices
```

##### Request a notify-all-devices task

```
curl -X POST http://localhost:8080/jobs
```
