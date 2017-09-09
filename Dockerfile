FROM anapsix/alpine-java:8

COPY target/api-server-*.jar /opt/api-server/api-server.jar

EXPOSE 8000

CMD ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.data.mongodb.host=mongo","-jar","/opt/api-server/api-server.jar"]