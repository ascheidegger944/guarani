FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/*.jar guarani.jar
ENTRYPOINT ["java","-jar","/guarani.jar"]