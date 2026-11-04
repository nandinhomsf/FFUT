FROM maven:3-amazoncorretto-21-alpine AS build

LABEL org.opencontainers.image.source="https://github.com/yurinogueira/FFUT"

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM maven:3-amazoncorretto-21-alpine

LABEL maintainer="personal@yurinogueira.dev.br"
LABEL vendor="TUFF"

ARG JAR_FILE=/app/target/ffut-*.jar

COPY --from=build ${JAR_FILE} app.jar

COPY warmup warmup

# Aquece o cache local do Maven (~/.m2) com os plugins do Maven/PIT durante o
# build da imagem, nao a cada boot do container. Isso fica assim porque o
# cache fica gravado nesta camada da imagem; rodar isso no startup.sh fazia
# o container demorar minutos pra responder toda vez que acordava de um
# periodo parado (ex: plano free do Render).
RUN cd warmup \
    && mvn clean package -DskipTests \
    && mvn clean package \
    && mvn org.pitest:pitest-maven:mutationCoverage \
    && mvn clean package \
    && mvn org.pitest:pitest-maven:mutationCoverage \
    && mvn clean package \
    && mvn org.pitest:pitest-maven:mutationCoverage

COPY startup.sh startup.sh

ENTRYPOINT ["bash", "/startup.sh"]
