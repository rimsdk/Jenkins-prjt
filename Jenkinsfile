pipeline {
    agent any

    tools {
        maven 'Maven_3.8.5'
        jdk 'OpenJDK_17'
    }

    environment {
        DOCKER_IMAGE = "rimsdk/banking-app"
        DOCKER_TAG = "${BUILD_NUMBER}"
        DOCKER_CONFIG = "/tmp/.docker"
        SONAR_PROJECT_KEY = "banking-app"
    }

    stages {
        stage('Vérification Environnement') {
            steps {
                sh '''
                    echo "JAVA_HOME: $JAVA_HOME"
                    echo "PATH: $PATH"
                    java -version
                    mvn -version
                '''
            }
        }

        stage('Build') {
            steps {
                script {
                    // Définir explicitement JAVA_HOME avant de lancer Maven
                    withEnv(["JAVA_HOME=${tool 'OpenJDK_17'}", "PATH=${tool 'OpenJDK_17'}/bin:${env.PATH}"]) {
                        sh 'mvn clean package -DskipTests'
                    }
                }
            }
        }

        stage('Tests Unitaires') {
            steps {
                script {
                    withEnv(["JAVA_HOME=${tool 'OpenJDK_17'}", "PATH=${tool 'OpenJDK_17'}/bin:${env.PATH}"]) {
                        sh 'mvn test'
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java'
                    )
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")

                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh """
                            mkdir -p ${DOCKER_CONFIG}
                            echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin
                            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                            docker push ${DOCKER_IMAGE}:latest
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            sh """
                rm -rf ${DOCKER_CONFIG}
                docker rmi ${DOCKER_IMAGE}:${DOCKER_TAG} || true
                docker rmi ${DOCKER_IMAGE}:latest || true
            """
        }
    }
}
