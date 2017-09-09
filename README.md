# JWT Integration with Spring Boot

## Docker

### Build docker image

Create the application jar

```
mvn clean package
```

Create the image

```
docker build -t jwt-server .
```

Run the container (a mongo container is mandatory)

```
docker run -p 8000:8000 --link mongo:mongo -d --name jwt-server jwt-server
```

### Run Docker Compose container

Create the application jar

```
mvn clean package
```

Run the containers

```
docker-compose up -d
```