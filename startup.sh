#!/bin/bash

# Cache pitest

cd /warmup && mvn clean package -DskipTests

# Warmup pitest

cd /warmup && mvn clean package
cd /warmup && mvn org.pitest:pitest-maven:mutationCoverage

cd /warmup && mvn clean package
cd /warmup && mvn org.pitest:pitest-maven:mutationCoverage

cd /warmup && mvn clean package
cd /warmup && mvn org.pitest:pitest-maven:mutationCoverage

# Run the application

cd / && java -jar /app.jar --spring.config.location=file:/application.properties
