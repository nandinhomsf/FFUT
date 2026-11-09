#!/bin/bash

# O aquecimento do Maven/PIT agora roda no build da imagem (ver Dockerfile),
# entao o boot do container so precisa subir a aplicacao.

# Limites de memória da aplicação: junto com as JVMs filhas do runner (ver
# ProcessRunnerEntity), tudo precisa caber nos 512 MB do plano grátis do Render.
cd / && exec java -Xmx160m -Xss512k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 \
    -XX:ReservedCodeCacheSize=48m -jar /app.jar
