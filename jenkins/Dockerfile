FROM jenkins/jenkins:lts

USER root

# Java 17 설치
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk

# Docker CLI 설치
RUN apt-get install -y docker.io

# ✅ sudo 설치
RUN apt-get install -y sudo

# ✅ docker 그룹 생성 및 jenkins 유저 추가
RUN groupadd -g 1001 docker && usermod -aG docker jenkins

# ✅ jenkins 유저에게 sudo 권한 부여
RUN echo "jenkins ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

# 환경 변수
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

USER jenkins