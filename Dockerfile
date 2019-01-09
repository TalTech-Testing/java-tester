FROM openjdk:11-jdk
MAINTAINER Andres Antonen <andres.antonen@gmail.com>
LABEL Description="Hodor Java 11 with StudentTester"
COPY build/libs/studenttester-core-2.0.jar StudentTester.jar

ENTRYPOINT [ "java", "-jar", "StudentTester.jar" ]
