FROM openjdk:11
ARG JAR_FILE=build/libs/scan-server-*.jar
COPY ${JAR_FILE} app.jar
# "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
ENTRYPOINT ["java","-jar","/app.jar"]