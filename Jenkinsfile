pipeline {
    agent any

    triggers {
        githubPush()
    }

    environment {
        PROJECT_NAME = "ex"
        JAR_FILE = "build/libs/*.jar"
        NGINX_CONF = "/etc/nginx/nginx.conf"
        GREEN_CONF = "nginx/nginx.green.conf"
        BLUE_CONF = "nginx/nginx.blue.conf"
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
                   def currentConf = sh(
                       script: "docker exec nginx cat /etc/nginx/nginx.conf",
                       returnStdout: true
                   ).trim()

                   echo "Current nginx config: ${currentConf}"

                   // spring-blue 또는 spring-green을 확인
                   if (currentConf.contains("spring-blue:8080")) {
                       env.CURRENT = "blue"
                       env.NEXT = "green"
                       env.NEXT_PORT = "8082"
                       env.NEXT_CONTAINER = "spring-green"
                       env.CONF_TO_USE = GREEN_CONF
                       echo "Detected blue is active, switching to green"
                   } else if (currentConf.contains("spring-green:8080")) {
                       env.CURRENT = "green"
                       env.NEXT = "blue"
                       env.NEXT_PORT = "8081"
                       env.NEXT_CONTAINER = "spring-blue"
                       env.CONF_TO_USE = BLUE_CONF
                       echo "Detected green is active, switching to blue"
                   } else {
                       // 기본값 설정 (첫 배포 시)
                       env.CURRENT = "none"
                       env.NEXT = "blue"
                       env.NEXT_PORT = "8081"
                       env.NEXT_CONTAINER = "spring-blue"
                       env.CONF_TO_USE = BLUE_CONF
                       echo "No active version detected, starting with blue"
                   }

                   echo "Current: ${env.CURRENT}, Next: ${env.NEXT}, Container: ${env.NEXT_CONTAINER}"
               }
           }
       }

        stage('Deploy NEXT') {
            steps {
                script {
                    // 새 버전 컨테이너 중지 및 제거
                    sh "docker stop ${env.NEXT_CONTAINER} || true"
                    sh "docker rm ${env.NEXT_CONTAINER} || true"

                    // 새 이미지 빌드
                    sh "docker build -t ${env.NEXT_CONTAINER}:latest ."

                    // 새 컨테이너 실행
                    sh """
                        docker run -d --name ${env.NEXT_CONTAINER} \
                        -p ${env.NEXT_PORT}:8080 \
                        --network ${PROJECT_NAME}_app \
                        -e SPRING_PROFILES_ACTIVE=${env.NEXT} \
                        ${env.NEXT_CONTAINER}:latest
                    """

                    // 컨테이너 시작 대기 (간단한 버전)
                    sh """
                        echo "Waiting for ${env.NEXT_CONTAINER} to start..."
                        sleep 10
                        echo "${env.NEXT_CONTAINER} should be ready now"
                    """
                }
            }
        }

        stage('Switch Nginx') {
            steps {
                script {
                    // nginx 설정 변경 (컨테이너 재시작 방식)
                    sh "cp ${env.CONF_TO_USE} ./nginx/nginx.conf"

                    // nginx 컨테이너 재시작으로 새 설정 적용
                    sh "docker restart nginx"

                    // 잠깐 대기
                    sh "sleep 5"

                    echo "Switched from ${env.CURRENT} to ${env.NEXT}"
                }
            }
        }

        stage('Cleanup Old Version') {
            steps {
                script {
                    // 잠시 대기 후 이전 버전 정리 (선택사항)
                    def oldContainer = (env.NEXT == "blue") ? "spring-green" : "spring-blue"
                    echo "Old container ${oldContainer} is still running for rollback purposes"
                    // 필요시 주석 해제하여 이전 버전 컨테이너 정리
                    // sh "sleep 30 && docker stop ${oldContainer} || true"
                }
            }
        }
    }
}