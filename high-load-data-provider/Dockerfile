FROM openjdk:17-alpine3.14

WORKDIR /high-load-data-provider

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve

COPY src ./src

CMD ["./mvnw", "spring-boot:run"]
EXPOSE 8083