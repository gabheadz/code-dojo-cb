FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY applications/app-service/build/libs/*.jar transfer-service.jar
ENV JAVA_OPTS=" -Xshareclasses:name=cacheapp,cacheDir=/cache,nonfatal -XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -jar transfer-service.jar" ]
