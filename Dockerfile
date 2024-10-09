FROM openjdk:17
VOLUME /tmp
EXPOSE 8082
ADD ./target/mytube_channels-0.0.1-SNAPSHOT.war mytube_channels.war
ENTRYPOINT ["java", "-jar", "/mytube_channels.war"]
