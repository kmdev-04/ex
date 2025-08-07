pipeline {
    agent any

    triggers {
        githubPush()
    }

    environment {
        JAR_FILE = "build/libs/*.jar"
        NGINX_CONF = "nginx/nginx.conf"
        GREEN_CONF = "nginx/nginx.green.conf"
        BLUE_CONF  = "nginx/nginx.blue.conf"
    }

    stages {
        stage('Git Pull') {
            steps {
                checkout scm
            }
        }

        stage('Build') {

            steps {
                sh 'chmod +x gradlew && ./gradlew clean build'
            }
        }

        stage('Detect Active Version') {
            steps {
                script {
                     def currentConf = sh(script: "docker exec nginx cat /etc/nginx/nginx.conf", returnStdout: true).trim()
                    if (currentConf.contains("spring-blue")) {
                        env.NEXT = "green"
                        env.PORT = "8082"
                        env.CONTAINER = "spring-green"
                        env.CONF_TO_USE = GREEN_CONF
                    } else {
                        env.NEXT = "blue"
                        env.PORT = "8081"
                        env.CONTAINER = "spring-blue"
                        env.CONF_TO_USE = BLUE_CONF
                    }
                }
            }
        }

        stage('Deploy NEXT') {
            steps {
                script {
                    sh "docker stop ${CONTAINER} || true"
                    sh "docker rm ${CONTAINER} || true"
                    sh "docker build -t ${CONTAINER} ."
                    sh """
                        docker run -d --name ${CONTAINER} \
                        -p ${PORT}:8080 ${CONTAINER} \
                        java -jar app.jar --spring.profiles.active=${NEXT}
                    """
                }
            }
        }

        stage('Switch Nginx') {
            steps {
                script {
                    sh "cp ${CONF_TO_USE} ${NGINX_CONF}"
                    sh "docker restart nginx"
                }
            }
        }
    }
}