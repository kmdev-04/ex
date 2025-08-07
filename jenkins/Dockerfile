FROM jenkins/jenkins:lts

USER root

# Java 17 설치
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk

# Docker CLI 설치
RUN apt-get install -y docker.io && \
    apt-get clean

# 환경 변수 설정
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

USER jenkins