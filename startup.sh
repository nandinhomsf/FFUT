#!/bin/bash

# O aquecimento do Maven/PIT agora roda no build da imagem (ver Dockerfile),
# entao o boot do container so precisa subir a aplicacao.

cd / && java -jar /app.jar
