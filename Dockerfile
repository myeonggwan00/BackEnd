FROM openjdk:21-jdk-slim
#CMD ["./gradlew", "clean", "build"]
# wait-for-it.sh를 docker-study 폴더 내에서 복사
COPY wait-for-it.sh /wait-for-it.sh
VOLUME /tmp
ARG JAR_FILE=build/libs/auction-site-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["/wait-for-it.sh", "db:3306", "--", "java", "-jar", "/app.jar"]