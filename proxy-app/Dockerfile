# docker build -t maven-proxy:latest .
# docker build -t maven-proxy:latest -f service/Dockerfile .
# docker run [-d] --name maven-proxy --memory 64m --publish 7999:7999 --network host maven-proxy:latest
# docker start/stop maven-proxy;

# --network host: für Internetzugang

#FROM localhost:5083/openjdk:17
FROM openjdk:latest
 
MAINTAINER Thomas Freese <commercial@freese-home.de>
 
ARG JAR_FILE=app/*-spring.jar
ENV VM_PARAMS="-Xms32m -Xmx64m -Dmavenproxy.port=7999"

COPY ${JAR_FILE} /app/app.jar

RUN echo "Europe/Berlin" > /etc/timezone

WORKDIR /
VOLUME /tmp
EXPOSE 7999

ENTRYPOINT exec java \
 $VM_PARAMS \
 -XX:TieredStopAtLevel=1 \
 -Djava.security.egd=file:/dev/./urandom \
 -jar \
 /app/app.jar \
 $0 $@
 
#ENTRYPOINT ["java",\
# "-Xms256m",\
# "-Xmx256m",\
# "-XX:TieredStopAtLevel=1",\
# "-Djava.security.egd=file:/dev/./urandom",\
# "-Dspring.profiles.active=default",\
# "-jar",\
# "/app/app.jar"] 

#CMD /usr/bin/java -Xms64m -Xmx256m -XX:TieredStopAtLevel=1 -noverify -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=default -jar /app/app.jar
#RUN [“java”, “...”]
