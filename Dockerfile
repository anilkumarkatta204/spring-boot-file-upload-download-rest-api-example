FROM openjdk:8
EXPOSE 9090
ADD target/file-demo-0.0.1-SNAPSHOT.jar file-demo-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/file-demo-0.0.1-SNAPSHOT.jar"]